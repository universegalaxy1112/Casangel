package com.itikarus.miska.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.itikarus.miska.R
import com.itikarus.miska.base.BaseActivity
import com.itikarus.miska.dialog.ProductDescriptionDialog
import com.itikarus.miska.extentions.convertPriceString
import com.itikarus.miska.extentions.gone
import com.itikarus.miska.extentions.showDialog
import com.itikarus.miska.globals.GlobalStorage
import com.itikarus.miska.models.product_model.ProductDetails
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_product2.view.*

class ProductGridAdapter(
    val activity: BaseActivity,
    val productModels: ArrayList<ProductDetails>,
    val addToCart: (() -> Unit)? = null
) : BaseAdapter() {
    private var layoutInflater: LayoutInflater? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        if (layoutInflater == null) layoutInflater = activity.layoutInflater

        val view: View
        val cvh: CustomViewHolder

        if (convertView == null) {
            view = layoutInflater!!.inflate(R.layout.item_product2, parent, false)
            cvh = CustomViewHolder(view)
            view.tag = cvh
        } else {
            view = convertView
            cvh = view.tag as CustomViewHolder
        }

        val model = getItem(position)

        if (model.isInStock) {
            with(cvh) {
                Picasso.get().load(model.images[0].src).fit().placeholder(R.drawable.ic_placeholder)
                    .into(iconViewer)
                nameViewer.text = model.name

                try {
                    priceViewer.text = model.price.toDouble().convertPriceString()
                } catch (e: Exception) {
                    priceViewer.text = "???"
                }

                itemBackgroundView.setOnClickListener {
                    val productDescriptionDialog =
                        ProductDescriptionDialog(activity, model, addToCart)
                    productDescriptionDialog.showDialog(activity)
                }
            }
        } else {
            view.gone()
        }

        return view
    }

    override fun getItem(position: Int): ProductDetails {
        return productModels[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return productModels.size
    }

    private inner class CustomViewHolder(itemView: View) {
        var btnAddToCart: View = itemView.btn_addCart
        var iconViewer: ImageView = itemView.iv_iconViewer
        var nameViewer: TextView = itemView.tv_productNameViewer
        var priceViewer: TextView = itemView.tv_productPriceViewer
        var itemBackgroundView = itemView.ll_itemView
    }
}