package com.itikarus.miska.adapters

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.itikarus.miska.R
import com.itikarus.miska.models.CategoryModel
import com.itikarus.miska.models.category_model.CategoryDetails
import com.squareup.picasso.Picasso

class CategoryListAdapter(ctx: Context, models: ArrayList<CategoryDetails>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<CustomViewHolder>() {

    private val categories = models
    private val context = ctx
    private var layoutInflater: LayoutInflater? = null
    private var selectedItemId = 0
    var onItemSelected: ((categoryModel: CategoryDetails) -> Unit)? = null

    var selectedItemColor = context.resources.getColor(R.color.colorPrimary)
    var unSelectedItemColor = context.resources.getColor(R.color.white)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        if (layoutInflater == null) layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater!!.inflate(R.layout.item_category, parent, false)
        return CustomViewHolder(view)
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val model = categories[position]

        @Suppress("NAME_SHADOWING")
        holder.let { holder ->
            val categoryIcon: String? = model.image?.src
            if (categoryIcon == null) {
                holder.iconViewer.setImageResource(R.drawable.ic_category_place_holder)
            } else {
                Picasso.get().load(model.image.src).fit().placeholder(R.drawable.ic_category_place_holder)
                    .into(holder.iconViewer)
            }
            holder.labelViewer.text = model.name
            if (selectedItemId == position) {
                holder.iconViewer.setColorFilter(selectedItemColor, PorterDuff.Mode.SRC_IN)
                holder.labelViewer.setTextColor(selectedItemColor)
            } else {
                holder.iconViewer.setColorFilter(unSelectedItemColor, PorterDuff.Mode.SRC_IN)
                holder.labelViewer.setTextColor(unSelectedItemColor)
            }

            holder.view.setOnClickListener {
                if (selectedItemId == position) return@setOnClickListener
                selectedItemId = position
                notifyDataSetChanged()
                onItemSelected?.invoke(model)
            }
        }

    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

}

class CustomViewHolder(itemView: View) :
    androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
    var view = itemView
    var iconViewer: ImageView = itemView.findViewById(R.id.iv_iconViewer)
    var labelViewer: TextView = itemView.findViewById(R.id.tv_labelViewer)
}