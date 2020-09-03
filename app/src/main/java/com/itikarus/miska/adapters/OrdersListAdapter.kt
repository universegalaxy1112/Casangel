package com.itikarus.miska.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.itikarus.miska.R
import com.itikarus.miska.extentions.convertPriceString
import com.itikarus.miska.models.order_model.OrderDetails
import com.itikarus.miska.tabs.account.OrderDetailActivity
import kotlinx.android.synthetic.main.item_card_orders.view.*

import java.text.DecimalFormat


/**
 * OrdersListAdapter is the adapter class of RecyclerView holding List of Orders in My_Orders
 */

class OrdersListAdapter(
    internal var context: Context,
    internal var ordersList: List<OrderDetails>
) :
    RecyclerView.Adapter<OrdersListAdapter.MyViewHolder>() {


    //********** Called to Inflate a Layout from XML and then return the Holder *********//

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // Inflate the custom layout
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_card_orders, parent, false)

        return MyViewHolder(itemView)
    }


    //********** Called by RecyclerView to display the Data at the specified Position *********//

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        // Get the data model based on Position
        val orderDetails = ordersList[position]

        var noOfProducts = 0
        for (i in 0 until orderDetails.orderProducts.size) {
            // Count no of Products
            noOfProducts += orderDetails.orderProducts[i].quantity
        }

        holder.order_id.text = orderDetails.id.toString()
        holder.order_status.text = orderDetails.status
        holder.order_date.text = orderDetails.dateCreated.replace("[a-zA-Z]".toRegex(), " ")
        holder.order_product_count.text = noOfProducts.toString()
        holder.order_price.text = "$" + orderDetails.total.toDouble().convertPriceString()


        // Check Order's status
        when {
            orderDetails.status.equals(
                "processing",
                ignoreCase = true
            ) -> holder.order_status.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.dark_blue_color
                )
            )
            orderDetails.status.equals(
                "on-hold",
                ignoreCase = true
            ) -> holder.order_status.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.dark_blue_color
                )
            )
            orderDetails.status.equals(
                "completed",
                ignoreCase = true
            ) -> holder.order_status.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.dark_green
                )
            )
            orderDetails.status.equals(
                "cancelled",
                ignoreCase = true
            ) -> holder.order_status.setTextColor(ContextCompat.getColor(context, R.color.dark_red))
            orderDetails.status.equals(
                "refunded",
                ignoreCase = true
            ) -> holder.order_status.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.dark_green
                )
            )
            orderDetails.status.equals(
                "failed",
                ignoreCase = true
            ) -> holder.order_status.setTextColor(ContextCompat.getColor(context, R.color.dark_red))
            else -> // pending
                holder.order_status.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.dark_blue_color
                    )
                )
        }



        holder.order_view_btn.setOnClickListener {
            // Get Order Info
            val itemInfo = Bundle()
            itemInfo.putInt("orderID", orderDetails.id)

            val intent = Intent(context, OrderDetailActivity::class.java)
            intent.putExtra("orderID", orderDetails.id.toString())
            context.startActivity(intent)
        }

    }


    //********** Returns the total number of items in the data set *********//

    override fun getItemCount(): Int {
        return ordersList.size
    }


    /********** Custom ViewHolder provides a direct reference to each of the Views within a Data_Item  */

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val order_view_btn: Button
        val order_id: TextView
        val order_product_count: TextView
        val order_status: TextView
        val order_price: TextView
        val order_date: TextView

        init {
            order_view_btn = itemView.order_view_btn
            order_id = itemView.order_id
            order_product_count = itemView.order_products_count
            order_status = itemView.order_status
            order_price = itemView.order_price
            order_date = itemView.order_date
        }
    }
}

