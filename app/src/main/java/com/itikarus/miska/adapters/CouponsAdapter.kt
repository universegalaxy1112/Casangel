package com.itikarus.miska.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.itikarus.miska.R
import com.itikarus.miska.models.coupons_model.CouponDetails

import java.text.DecimalFormat


/**
 * CouponsAdapter is the adapter class of RecyclerView holding List of Coupons in Checkout and Order_Details
 */

class CouponsAdapter(
    internal var context: Context,
    internal var couponsList: List<CouponDetails>,
    internal var isRemovable: Boolean
) : RecyclerView.Adapter<CouponsAdapter.MyViewHolder>() {


    //********** Called to Inflate a Layout from XML and then return the Holder *********//

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // Inflate the custom layout
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_card_coupons, parent, false)

        return MyViewHolder(itemView)
    }


    //********** Called by RecyclerView to display the Data at the specified Position *********//

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // Get the data model based on Position
        val coupon = couponsList[position]

        holder.coupon_code.text = coupon.code
        holder.coupon_discount.text =
            "$" + DecimalFormat("#0.00").format(java.lang.Double.parseDouble(coupon.discount))

        if (coupon.discountType != null) {
            holder.coupon_type_layout.visibility = View.VISIBLE
            holder.coupon_type.text = coupon.discountType
        } else {
            holder.coupon_type_layout.visibility = View.GONE
        }


        if (coupon.amount != null) {
            holder.coupon_amount_layout.visibility = View.VISIBLE

            if (coupon.discountType.equals(
                    "fixed_cart",
                    ignoreCase = true
                ) || coupon.discountType.equals("fixed_product", ignoreCase = true)
            ) {
                holder.coupon_amount.text =
                    "$" + DecimalFormat("#0.00").format(java.lang.Double.parseDouble(coupon.amount))
            } else if (coupon.discountType.equals(
                    "percent",
                    ignoreCase = true
                ) || coupon.discountType.equals("percent_product", ignoreCase = true)
            ) {
                holder.coupon_amount.text = coupon.amount + "%"
            }

        } else {
            holder.coupon_amount_layout.visibility = View.GONE
        }



        if (isRemovable) {
            holder.coupon_delete.visibility = View.VISIBLE

            holder.coupon_delete.setOnClickListener { }

        } else {
            holder.coupon_delete.visibility = View.GONE
        }

    }


    //********** Returns the total number of items in the data set *********//

    override fun getItemCount(): Int {
        return couponsList.size
    }


    /********** Custom ViewHolder provides a direct reference to each of the Views within a Data_Item  */

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val coupon_delete: ImageButton
        val coupon_code: TextView
        val coupon_type: TextView
        val coupon_amount: TextView
        val coupon_discount: TextView
        val coupon_code_layout: RelativeLayout
        val coupon_type_layout: RelativeLayout
        val coupon_amount_layout: RelativeLayout
        val coupon_discount_layout: RelativeLayout

        init {

            coupon_code = itemView.findViewById<View>(R.id.coupon_code) as TextView
            coupon_type = itemView.findViewById<View>(R.id.coupon_type) as TextView
            coupon_amount = itemView.findViewById<View>(R.id.coupon_amount) as TextView
            coupon_discount = itemView.findViewById<View>(R.id.coupon_discount) as TextView
            coupon_delete = itemView.findViewById<View>(R.id.coupon_delete) as ImageButton
            coupon_code_layout =
                itemView.findViewById<View>(R.id.coupon_code_layout) as RelativeLayout
            coupon_type_layout =
                itemView.findViewById<View>(R.id.coupon_type_layout) as RelativeLayout
            coupon_amount_layout =
                itemView.findViewById<View>(R.id.coupon_amount_layout) as RelativeLayout
            coupon_discount_layout =
                itemView.findViewById<View>(R.id.coupon_discount_layout) as RelativeLayout

        }
    }
}

