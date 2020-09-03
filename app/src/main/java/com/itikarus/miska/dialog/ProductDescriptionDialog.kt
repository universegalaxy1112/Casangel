package com.itikarus.miska.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.itikarus.miska.R
import com.itikarus.miska.adapters.ProductDetailImgAdapter
import com.itikarus.miska.extentions.loadImage
import com.itikarus.miska.extentions.setHtmlText
import com.itikarus.miska.extentions.showDialog
import com.itikarus.miska.extentions.verticalize
import com.itikarus.miska.globals.GlobalStorage
import com.itikarus.miska.models.product_model.ProductDetails
import com.itikarus.miska.models.product_model.ProductImages
import kotlinx.android.synthetic.main.dialog_product_description.*

class ProductDescriptionDialog(
    context: Context,
    productDetails: ProductDetails,
    val addToCart: (() -> Unit)? = null
) : Dialog(context) {

    private val product = productDetails

    private val productImgUrls = ArrayList<ProductImages>()

    private var detailImgUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.dialog_product_description)

        productImgUrls.addAll(product.images)

        productDetailImgListView.verticalize(context)
        productDetailImgListView.adapter = ProductDetailImgAdapter(context, productImgUrls) {
            detailImgUrl = it.src
            showDetailImg()
        }

        detailImgUrl = productImgUrls[0].src
        showDetailImg()

        productNameViewer.text = product.name
        productDescriptionViewer.setHtmlText(product.description)

        productImgViewer.setOnClickListener {
            ShowImgDialog(context, detailImgUrl).showDialog(context)
        }

        btnAddToCart.setOnClickListener {
            GlobalStorage.addProductToCart(product)
            addToCart?.invoke()
            dismiss()
        }

    }

    private fun showDetailImg() {
        productImgViewer.loadImage(detailImgUrl, R.drawable.ic_placeholder)
    }
}