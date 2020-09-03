package com.itikarus.miska

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.webkit.*
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.itikarus.miska.base.BaseActivity
import com.itikarus.miska.globals.GlobalStorage
import com.itikarus.miska.library.utils.DialogUtils
import com.itikarus.miska.models.order_model.OrderDetails
import com.itikarus.miska.models.order_model.PostOrder
import com.itikarus.miska.network.APIClient
import kotlinx.android.synthetic.main.activity_checkout.*
import retrofit2.Call
import retrofit2.Callback
import java.util.HashMap

class CheckoutActivity : BaseActivity() {


    //private var cartItemsList: List<CartDetails> = ArrayList()
    private lateinit var orderDetails: OrderDetails
    private var ORDER_ID: String = ""
    private var ORDER_RECEIVED = "order-received"
    private var CHECKOUT_URL = APIClient.BASE_URL + "/android-mobile-checkout"
    private lateinit var postOrder: PostOrder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        orderDetails = GlobalStorage.loadOrderDetails()


        iv_btnBack.setOnClickListener {
            finish()
        }

        prepareDataForPlaceOrder()

    }

    private fun prepareDataForPlaceOrder() {
        ORDER_ID = ""
        postOrder = PostOrder()

        postOrder.orderProducts = orderDetails.orderProducts
        postOrder.orderCoupons = orderDetails.orderCoupons


        val params = HashMap<String, Any>()
        params["token"] = orderDetails.token
        params["billing_info"] = orderDetails.billing
        params["shipping_info"] = orderDetails.shipping
        params["products"] = orderDetails.orderProducts
        params["coupons"] = orderDetails.orderCoupons
        params["customer_id"] = orderDetails.customerId
        params["one_page"] = "1"
        params["platform"] = "Android"

        val gson = Gson()

        val jsonData = gson.toJson(params)

        Log.i("VC_Shop", "order_json_data= $jsonData")


        PlaceOrder(jsonData)
    }

    //*********** Place the Order on the Server ********//

    fun PlaceOrder(jsonData: String) {

        showProgressDialog(getString(R.string.just_a_moment))


        val call = APIClient.getInstance()
            .placeOrder(
                "cool",
                jsonData
            )

        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: retrofit2.Response<String>) {

                if (response.isSuccessful) {

                    if (response.body() != null && !TextUtils.isEmpty(response.body())) {
                        ORDER_ID = response.body().toString()
                        LoadCheckoutPage(ORDER_ID)
                    } else {
                        hideProgressDialog()
                        Snackbar.make(rootView, getString(R.string.unexpected_response), Snackbar.LENGTH_SHORT).show()
                    }
                } else {
                    hideProgressDialog()
                    Toast.makeText(this@CheckoutActivity, "Error : " + response.message(), Toast.LENGTH_SHORT).show()
                }

            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                hideProgressDialog()
                Toast.makeText(this@CheckoutActivity, "NetworkCallFailure : $t", Toast.LENGTH_LONG).show()
            }
        })
    }

    //*********** Place the Order on the Server ********//

    @SuppressLint("SetJavaScriptEnabled")
    fun LoadCheckoutPage(order_id: String) {

        val url = "$CHECKOUT_URL?order_id=$order_id"

        Log.i("VC_Shop", "url= $url")


        checkout_webView.webViewClient = object : WebViewClient() {
            override fun onLoadResource(view: WebView, url: String) {
                super.onLoadResource(view, url)
                Log.i("order", "onPageStarted: url=$url")
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Log.i("VC_Shop", "onPageStarted: url= $url")

                if (url.contains(ORDER_RECEIVED)) {
                    view.stopLoading()

                    DialogUtils.showOkayDialog(this@CheckoutActivity, getString(R.string.thank_you), getString(R.string.thank_for_shopping)){
                        GlobalStorage.clearCart()
                        restartActivity(MainActivity::class.java)
                    }
                }
            }

            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                hideProgressDialog()
                Log.i("VC_Shop", "onPageFinished: url= $url")
            }

            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                super.onReceivedError(view, request, error)
                hideProgressDialog()
                Log.i("VC_Shop", "onReceivedError: error= $error")
            }
        }

        checkout_webView.isVerticalScrollBarEnabled = false
        checkout_webView.isHorizontalScrollBarEnabled = false
        checkout_webView.setBackgroundColor(Color.TRANSPARENT)
        checkout_webView.settings.javaScriptEnabled = true
        @Suppress("DEPRECATION")
        checkout_webView.settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
        checkout_webView.loadUrl(url)

    }
}
