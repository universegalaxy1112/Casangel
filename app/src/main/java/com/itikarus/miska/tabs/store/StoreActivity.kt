package com.itikarus.miska.tabs.store

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.itikarus.miska.R
import com.itikarus.miska.adapters.CategoryListAdapter
import com.itikarus.miska.adapters.ProductGridAdapter
import com.itikarus.miska.extentions.horizontalize
import com.itikarus.miska.globals.GlobalStorage
import com.itikarus.miska.globals.KeyUtils
import com.itikarus.miska.models.api_response_model.ErrorResponse
import com.itikarus.miska.models.category_model.CategoryDetails
import com.itikarus.miska.models.product_model.ProductDetails
import com.itikarus.miska.network.APIClient
import com.itikarus.miska.tabs.BaseTabActivity
import com.itikarus.miska.tabs.home.ShoppingCartActivity
import kotlinx.android.synthetic.main.activity_create_space.*
import kotlinx.android.synthetic.main.activity_store.*
import kotlinx.android.synthetic.main.activity_store.aiv_loading
import kotlinx.android.synthetic.main.activity_store.rv_categoryList
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import java.io.IOException
import java.util.*

class StoreActivity : BaseTabActivity() {

    override fun onActivityLoad() {
        GlobalStorage.clearCart()
        notifyCartBadge()
        getMainActivity().onActivityResultMap[javaClass.simpleName] = {_, _, _->
            notifyCartBadge()
        }
    }

    private var categories = ArrayList<CategoryDetails>()
    private var productModels = ArrayList<ProductDetails>()
    private lateinit var categoryListAdapter : CategoryListAdapter
    private lateinit var productGridAdapter : ProductGridAdapter
    private var pageNumber = 1
    private val order = "desc"
    private val sortBy = "date"

    private var currentCategoryId = ""
    private var isLoadMore = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store)

        // Category horizontal list setting
        categoryListAdapter = CategoryListAdapter(this, categories)
        categoryListAdapter.unSelectedItemColor = Color.BLACK
        categoryListAdapter.onItemSelected = {categoryModel -> categorySelected(categoryModel)}

        rv_categoryList.horizontalize(this)
        rv_categoryList.adapter = categoryListAdapter

        //template grid setting
        productGridAdapter = ProductGridAdapter(this, productModels) {
            Toast.makeText(this, R.string.msg_add_to_cart, Toast.LENGTH_SHORT).show()
            notifyCartBadge()
        }
        gv_templateGrid.adapter = productGridAdapter

        ibv_cartIcon.setOnClickListener {
            if(GlobalStorage.getCartItems().isEmpty()){
                Crouton(getString(R.string.cart_empty), false)
                return@setOnClickListener
            }
            val intent = Intent(this, ShoppingCartActivity::class.java)
            getMainActivity().startActivityForResult(intent, KeyUtils.REQUEST_CODE_SHOPPING_CART)
        }

        loadCategories()
    }

    private fun notifyCartBadge(){
        ibv_cartIcon.badgeValue = GlobalStorage.getCartItems().size
    }

    private fun categorySelected(categoryModel: CategoryDetails){
        isLoadMore = false;
        loadProducts(categoryModel.id.toString())
    }

    override fun onBackPressed() {
        getMainActivity().switchTab(0)
    }

    private fun loadCategories(){

        categories.addAll(GlobalStorage.loadProductCategories())
        categoryListAdapter.notifyDataSetChanged()
        if(categories.isNotEmpty()){
            isLoadMore = false;
            loadProducts(categories[0].id.toString())
        }
    }

    private fun loadProducts(categoryID : String){

        currentCategoryId = categoryID
        if(!isLoadMore) {
            productModels.clear()
            productGridAdapter.notifyDataSetChanged()
            pageNumber = 1
            aiv_loading.visibility = View.VISIBLE
        }

        val params = LinkedHashMap<String, Any>()
        params["category"] = categoryID
        params["page"] = pageNumber.toString()
        params["per_page"] = 100
        params["order"] = order
        params["orderby"] = sortBy
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

                if (response.isSuccessful) {

                    if(categoryID == currentCategoryId) {
                        if(!isLoadMore)
                            productModels.clear()
                        response.body()?.forEach {
                            if (it.isInStock) productModels.add(it)
                        }
                        productGridAdapter.notifyDataSetChanged()
                        // Hide the ProgressBar
                        aiv_loading.hide()

                        if(productModels.size % 100 > 80 && response.body()?.size!! > 0) {
                            isLoadMore = true
                            pageNumber++
                            loadProducts(currentCategoryId)
                        }

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

                    Toast.makeText(this@StoreActivity, "Error : " + error.message, Toast.LENGTH_SHORT).show()
                }

            }

            override fun onFailure(call: Call<List<ProductDetails>>, t: Throwable) {
                aiv_loading.hide()
                Toast.makeText(this@StoreActivity, "NetworkCallFailure : $t", Toast.LENGTH_LONG).show()
            }
        })
    }
}
