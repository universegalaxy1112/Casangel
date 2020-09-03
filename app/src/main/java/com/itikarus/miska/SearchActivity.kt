package com.itikarus.miska

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import com.itikarus.miska.adapters.ProductGridAdapter
import com.itikarus.miska.base.BaseActivity
import com.itikarus.miska.extentions.invisiable
import com.itikarus.miska.extentions.visiable
import com.itikarus.miska.globals.GlobalStorage
import com.itikarus.miska.models.api_response_model.ErrorResponse
import com.itikarus.miska.models.product_model.ProductDetails
import com.itikarus.miska.network.APIClient
import com.itikarus.miska.tabs.home.ShoppingCartActivity
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.activity_search.aiv_loading
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class SearchActivity : BaseActivity() {

    private var searchedProductsList =  ArrayList<ProductDetails>()
    private lateinit var productGridAdapter : ProductGridAdapter

    private val order = "desc"
    private val sortBy = "date"
    private var FLAG_SEARCHED = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        //template grid setting
        productGridAdapter = ProductGridAdapter(this, searchedProductsList){
            ibv_cartIcon.badgeValue = GlobalStorage.getCartItems().size
            Toast.makeText(this, R.string.msg_add_to_cart, Toast.LENGTH_SHORT).show()
        }

        gv_products.adapter = productGridAdapter

        GlobalStorage.clearCart()
        ibv_cartIcon.setOnClickListener {

            if(GlobalStorage.getCartItems().isEmpty()){
                Crouton(getString(R.string.cart_empty), false)
                return@setOnClickListener
            }

            val intent = Intent(this, ShoppingCartActivity::class.java)
            startActivityForResult(intent, 1004)
        }

        iv_btnBack.setOnClickListener { finish() }

        search_editText.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {
                searchProduct(query)
                FLAG_SEARCHED = true
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if(newText.isEmpty()) {
                    searchedProductsList.clear()
                    productGridAdapter.notifyDataSetChanged()
                    empty_record.visiable()
                    FLAG_SEARCHED = false
                    return false
                }else if(newText.length > 2 && FLAG_SEARCHED){
                    searchProduct(newText)
                }
                return false
            }
        })
    }

    private fun searchProduct(searchValue : String){
        searchedProductsList.clear()
        productGridAdapter.notifyDataSetChanged()

        aiv_loading.visibility = View.VISIBLE
        empty_record.invisiable()

        val params = LinkedHashMap<String, Any>()
        params["per_page"] = 100
        params["order"] = order
        params["orderby"] = sortBy
        params["search"] = searchValue
        params["lang"] = Locale.getDefault().language

        val call = APIClient.getInstance()
            .getAllProducts(
                params
            )

        call.enqueue(object : Callback<List<ProductDetails>> {
            override fun onResponse(
                call: Call<List<ProductDetails>>,
                response: retrofit2.Response<List<ProductDetails>>
            ) {

                // Hide the ProgressBar
                aiv_loading.hide()

                if (response.isSuccessful) {
                    response.body()?.forEach {
                        if (it.isInStock) searchedProductsList.add(it)
                    }

                    if(searchedProductsList.isEmpty()){
                        empty_record.visiable()
                    }else{
                        empty_record.invisiable()
                        productGridAdapter.notifyDataSetChanged()
                    }
                } else {
                    val converter : Converter<ResponseBody, ErrorResponse> = APIClient.retrofit.responseBodyConverter(
                        ErrorResponse::class.java, arrayOfNulls<Annotation>(0))
                    val error: ErrorResponse
                    error = try {
                        converter.convert(response.errorBody()!!)
                    } catch (e: IOException) {
                        ErrorResponse()
                    }

                    Toast.makeText(this@SearchActivity, "Error : " + error.message, Toast.LENGTH_SHORT).show()
                }

            }

            override fun onFailure(call: Call<List<ProductDetails>>, t: Throwable) {
                aiv_loading.hide()
                Toast.makeText(this@SearchActivity, "NetworkCallFailure : $t", Toast.LENGTH_LONG).show()
            }
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == 1004){
            ibv_cartIcon.badgeValue = GlobalStorage.getCartItems().size
        }
    }
}
