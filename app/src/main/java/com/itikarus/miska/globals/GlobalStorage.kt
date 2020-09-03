package com.itikarus.miska.globals

import android.text.TextUtils
import com.itikarus.miska.models.cart_model.CartDetails
import com.itikarus.miska.models.category_model.CategoryDetails
import com.itikarus.miska.models.order_model.OrderDetails
import com.itikarus.miska.models.post_model.PostCategory
import com.itikarus.miska.models.post_model.PostDetails
import com.itikarus.miska.models.product_model.ProductAttributes
import com.itikarus.miska.models.product_model.ProductDetails
import com.itikarus.miska.models.product_model.ProductMetaData

object GlobalStorage {
    private val cartItemsList = ArrayList<CartDetails>()
    fun addProductsToCart(products: ArrayList<ProductDetails>) {
        this.cartItemsList.clear()
        products.forEach {
            convertProductToCartItem(it)
        }
    }

    fun addProductToCart(product: ProductDetails) {
        convertProductToCartItem(product)
    }

    fun getCartItems(): ArrayList<CartDetails> {
        return cartItemsList
    }

    // Product categories
    private var categoriesList = ArrayList<CategoryDetails>()
    fun addProductCategories(infos: List<CategoryDetails>) {
        categoriesList.clear()
        categoriesList.addAll(infos)
    }

    fun loadProductCategories(): ArrayList<CategoryDetails> {
        return categoriesList
    }

    private val landingBackgroundImages = ArrayList<String>()
    fun addLandingBackgroundImages(urls: ArrayList<String>) {
        landingBackgroundImages.clear()
        landingBackgroundImages.addAll(urls)
    }

    fun loadLandingBackgroundImages(): ArrayList<String> {
        return landingBackgroundImages
    }


    private var trendsInfos = ArrayList<PostDetails>() // for trends page
    fun addTrendsInfo(infos: List<PostDetails>) {
        trendsInfos.clear()
        trendsInfos.addAll(infos)
    }

    fun loadTrendsInfos(): ArrayList<PostDetails> {
        return trendsInfos
    }

    // For Quick start page
    private var templatesCategories = ArrayList<PostCategory>()
    fun addTemplatesCategories(infos: List<PostCategory>) {
        templatesCategories.clear()
        templatesCategories.addAll(infos)
    }

    fun loadTemplatesCategories(): ArrayList<PostCategory> {
        return templatesCategories
    }

    // For Inspirate page
    private var inspirateCategories = ArrayList<PostCategory>()
    fun addInspirateCategories(infos: List<PostCategory>) {
        inspirateCategories.clear()

        inspirateCategories.addAll(infos)
    }

    fun loadInspirateCategories(): ArrayList<PostCategory> {
        return inspirateCategories
    }

    private var orderDetails = OrderDetails()
    fun addOrderDetails(info: OrderDetails) {
        orderDetails = info
    }

    fun loadOrderDetails(): OrderDetails {
        return orderDetails
    }

    fun clearCart() {
        cartItemsList.clear()
    }

    //********** Adds the Product to User's Cart *********//

    private fun convertProductToCartItem(product: ProductDetails) {

        val cartDetails = CartDetails()

        var productBasePrice = 0.0

        if (product.price != null && !TextUtils.isEmpty(product.price))
            productBasePrice = java.lang.Double.parseDouble(product.price)


        val productMetaData = java.util.ArrayList<ProductMetaData>()
        val selectedAttributes = java.util.ArrayList<ProductAttributes>()


        // Set Product's Price and Quantity
        product.customersBasketQuantity = 1
        product.selectedVariationID = product.selectedVariationID
        product.productsFinalPrice = productBasePrice.toString()
        product.totalPrice = productBasePrice.toString()

        // Set Customer's Basket Product and selected Attributes Info
        cartDetails.cartProduct = product
        cartDetails.cartProductMetaData = productMetaData
        cartDetails.cartProductAttributes = selectedAttributes


        // Add the Product to User's Cart with the help of static method of My_Cart class
        cartItemsList.add(cartDetails)

    }

}