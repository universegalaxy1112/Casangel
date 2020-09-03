package com.itikarus.miska.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.itikarus.miska.R
import com.itikarus.miska.extentions.loadImage
import com.itikarus.miska.models.product_model.ProductImages
import kotlinx.android.synthetic.main.item_product3.view.*

class ProductDetailImgAdapter(
    context: Context,
    productImages: List<ProductImages>,
    imageSelected: ((image: ProductImages) -> Unit)? = null
) : RecyclerView.Adapter<ProductDetailImgAdapter.CustomViewHolder>() {

    val mContext = context
    val productImgUrls = productImages
    val onImageSelected = imageSelected

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        return CustomViewHolder(
            LayoutInflater.from(mContext).inflate(R.layout.item_product3, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return productImgUrls.size
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val productImgURL = productImgUrls[position]
        with(holder) {
            productdetailImgViewer.loadImage(productImgURL.src, R.drawable.ic_placeholder)

            productdetailImgViewer.setOnClickListener {
                onImageSelected?.invoke(productImgURL)
            }
        }
    }

    inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productdetailImgViewer = itemView.imgViewer
    }
}