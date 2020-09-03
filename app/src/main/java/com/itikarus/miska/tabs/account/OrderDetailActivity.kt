package com.itikarus.miska.tabs.account

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.itikarus.miska.R
import com.itikarus.miska.adapters.CouponsAdapter
import com.itikarus.miska.adapters.OrderedProductsListAdapter
import com.itikarus.miska.base.BaseActivity
import com.itikarus.miska.extentions.convertPriceString
import com.itikarus.miska.extentions.verticalize
import com.itikarus.miska.globals.GlobalStorage
import com.itikarus.miska.models.api_response_model.ErrorResponse
import com.itikarus.miska.models.coupons_model.CouponDetails
import com.itikarus.miska.models.order_model.OrderDetails
import com.itikarus.miska.models.order_model.OrderProducts
import com.itikarus.miska.models.product_model.ProductDetails
import com.itikarus.miska.network.APIClient
import kotlinx.android.synthetic.main.activity_order_detail.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import java.io.IOException
import java.security.AccessController.getContext
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class OrderDetailActivity : BaseActivity() {

    private lateinit var orderID: String
    //private lateinit var orderDetails : OrderDetails

    private var couponsList = ArrayList<CouponDetails>()
    private var productsList = ArrayList<ProductDetails>()
    private var orderProductsList = ArrayList<OrderProducts>()

    private lateinit var couponsAdapter: CouponsAdapter
    private lateinit var orderedProductsAdapter: OrderedProductsListAdapter
    private var cancel_order_hours = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_detail)
        checkout_items_recycler.isNestedScrollingEnabled = false
        checkout_coupons_recycler.isNestedScrollingEnabled = false

        //orderDetails = GlobalStorage.loadOrderDetails()
        orderID = intent.getStringExtra("orderID")

        RequestOrderDetail(orderID)

        iv_btnBack.setOnClickListener {
            finish()
        }
    }

    //*********** Adds Product's Details to the Views ********//

    @SuppressLint("SetTextI18n")
    private fun setOrderDetails(orderDetails: OrderDetails) {

        couponsList = orderDetails.orderCoupons as ArrayList<CouponDetails>
        orderProductsList = orderDetails.orderProducts as ArrayList<OrderProducts>

        var subTotal = 0.0
        var noOfProducts = 0
        for (i in orderProductsList.indices) {
            subTotal += java.lang.Double.parseDouble(orderProductsList[i].total)
            noOfProducts += orderProductsList[i].quantity
        }


        order_products_count.text = noOfProducts.toString()
        payment_method.text = orderDetails.paymentMethodTitle
        order_status.text = orderDetails.status
        order_date.text = orderDetails.dateCreated.replace("[a-zA-Z]", " ")

        if (orderDetails.orderShippingMethods.size > 0)
            shipping_method.text = orderDetails.orderShippingMethods[0].methodTitle

        checkout_subtotal.text = "$" + subTotal.convertPriceString()
        checkout_tax.text = "$" + orderDetails.totalTax.toDouble().convertPriceString()

        checkout_shipping.text = "$" + orderDetails.shippingTotal.toDouble().convertPriceString()
        checkout_discount.text = "$" + orderDetails.discountTotal.toDouble().convertPriceString()
        checkout_total.text = "$" + orderDetails.total.toDouble().convertPriceString()
        order_price.text = "$" + orderDetails.total.toDouble().convertPriceString()

        billing_name.text = orderDetails.billing.firstName
        billing_address.text = orderDetails.billing.city
        billing_street.text = orderDetails.billing.address1
        shipping_name.text = orderDetails.shipping.firstName
        shipping_address.text = orderDetails.shipping.city
        shipping_street.text = orderDetails.shipping.address1


        if (orderDetails.customerNote != null && !TextUtils.isEmpty(orderDetails.customerNote)) {
            buyer_comments_card.visibility = View.VISIBLE
            buyer_comments.text = orderDetails.customerNote
        } else {
            buyer_comments_card.visibility = View.GONE
        }

        seller_comments_card.visibility = View.GONE



        couponsAdapter = CouponsAdapter(this, couponsList, false)

        checkout_coupons_recycler.verticalize(this)
        checkout_coupons_recycler.adapter = couponsAdapter


        val getOrderedProducts = GetOrderedProducts(orderProductsList)
        getOrderedProducts.execute()


        orderedProductsAdapter = OrderedProductsListAdapter(this, productsList)

        checkout_items_recycler.adapter = orderedProductsAdapter
        checkout_items_recycler.verticalize(this)
        checkout_items_recycler.addItemDecoration(
            DividerItemDecoration(
                this,
                LinearLayoutManager.VERTICAL
            )
        )

    }

    fun getcancelOrdeHour(orderDetails: OrderDetails): Int {

        var cancelHours = 0

        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm:ss")

        val currentDateTime = simpleDateFormat.format(Date())
        val orderDateTime = orderDetails.dateCreated.replace("[a-zA-Z]", " ")
        val replace = orderDateTime.replace("-".toRegex(), "/")
        var initDate: Date? = null
        var orderDateTimeFinal: String? = null
        try {
            initDate = SimpleDateFormat("yyyy/MM/dd hh:mm:ss").parse(replace)

            orderDateTimeFinal = simpleDateFormat.format(initDate)
        } catch (e: ParseException) {
            e.printStackTrace()
        }


        try {
            val date1 = simpleDateFormat.parse(orderDateTimeFinal)
            val date2 = simpleDateFormat.parse(currentDateTime)

            cancelHours = printDifference(date1, date2)

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return cancelHours

        /// End Function
    }

    //1 minute = 60 seconds
    //1 hour = 60 x 60 = 3600
    //1 day = 3600 x 24 = 86400
    fun printDifference(startDate: Date, endDate: Date): Int {
        //milliseconds
        var different = endDate.time - startDate.time

        println("startDate : $startDate")
        println("endDate : $endDate")
        println("different : $different")

        val secondsInMilli: Long = 1000
        val minutesInMilli = secondsInMilli * 60
        val hoursInMilli = minutesInMilli * 60
        val daysInMilli = hoursInMilli * 24

        val elapsedDays = different / daysInMilli
        different %= daysInMilli

        val elapsedHours = different / hoursInMilli
        different %= hoursInMilli

        val elapsedMinutes = different / minutesInMilli
        different %= minutesInMilli

        val elapsedSeconds = different / secondsInMilli

        System.out.printf(
            "%d days, %d hours, %d minutes, %d seconds%n",
            elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds
        )

        return elapsedHours.toInt()

    }

    /*********** LoadMoreTask Used to Load more Products from the Server in the Background Thread using AsyncTask  */

    @SuppressLint("StaticFieldLeak")
    private inner class GetOrderedProducts constructor(
        internal var orderProducts: List<OrderProducts>
    ) :
        AsyncTask<String, Void, String>() {


        //*********** Runs on the UI thread before #doInBackground() ********//

        override fun onPreExecute() {
            super.onPreExecute()
        }


        //*********** Performs some Processes on Background Thread and Returns a specified Result  ********//

        override fun doInBackground(vararg params: String): String {

            for (i in orderProducts.indices) {

                val call = APIClient.getInstance()
                    .getSingleProduct(
                        orderProducts[i].productId.toString()
                    )

                try {

                    val response = call.execute()

                    if (response.isSuccessful) {

                        addOrderProduct(response.body())

                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }

            return "All Done!"
        }


        //*********** Runs on the UI thread after #doInBackground() ********//

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            orderedProductsAdapter.notifyDataSetChanged()
        }
    }

    //*********** Request Product Details from the Server based on productID ********//

    fun addOrderProduct(productDetails: ProductDetails?) {

        for (i in orderProductsList.indices) {
            if (orderProductsList[i].productId == productDetails!!.id) {
                productDetails.price = orderProductsList[i].price
                productDetails.productsFinalPrice = orderProductsList[i].subtotal
                productDetails.totalPrice = orderProductsList[i].total
                productDetails.customersBasketQuantity = orderProductsList[i].quantity

                if (productDetails.images != null && !TextUtils.isEmpty(productDetails.images[0].src)) {
                    productDetails.image = productDetails.images[0].src
                } else {
                    productDetails.image = ""
                }

                productsList.add(productDetails)
            }
        }
    }

    //*********** Request Order Details from the Server based on orderID ********//

    fun RequestOrderDetail(orderID: String) {

        showProgressDialog("Loading order details...")

        val call = APIClient.getInstance()
            .getSingleOrder(
                orderID
            )

        call.enqueue(object : Callback<OrderDetails> {
            override fun onResponse(
                call: Call<OrderDetails>,
                response: retrofit2.Response<OrderDetails>
            ) {

                hideProgressDialog()


                if (response.isSuccessful) {

                    setOrderDetails(response.body()!!)

                } else {
                    val converter: Converter<ResponseBody, ErrorResponse> =
                        APIClient.retrofit.responseBodyConverter(
                            ErrorResponse::class.java,
                            arrayOfNulls(0)
                        )
                    var error: ErrorResponse
                    try {
                        error = converter.convert(response.errorBody()!!)
                    } catch (e: IOException) {
                        error = ErrorResponse()
                    }

                    Toast.makeText(
                        this@OrderDetailActivity,
                        "Error : " + error.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<OrderDetails>, t: Throwable) {
                hideProgressDialog()
                Toast.makeText(
                    this@OrderDetailActivity,
                    "NetworkCallFailure : $t",
                    Toast.LENGTH_LONG
                ).show()
            }
        })

    }
}
