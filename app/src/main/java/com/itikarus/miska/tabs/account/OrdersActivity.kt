package com.itikarus.miska.tabs.account

import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.itikarus.miska.R
import com.itikarus.miska.adapters.OrdersListAdapter
import com.itikarus.miska.base.BaseActivity
import com.itikarus.miska.extentions.verticalize
import com.itikarus.miska.globals.LocalDB
import com.itikarus.miska.models.order_model.OrderDetails
import com.itikarus.miska.network.APIClient
import kotlinx.android.synthetic.main.activity_orders.*
import retrofit2.Call
import retrofit2.Callback
import java.util.*
import kotlin.collections.ArrayList

class OrdersActivity : BaseActivity() {

    private var customerID = ""
    private var ordersList = ArrayList<OrderDetails>()
    private lateinit var ordersListAdapter: OrdersListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orders)

        customerID = LocalDB.build(this).userID

        empty_record.visibility = View.GONE
        ordersListAdapter = OrdersListAdapter(this, ordersList)
        orders_recycler.adapter = ordersListAdapter
        orders_recycler.verticalize(this)

        iv_btnBack.setOnClickListener {
            finish()
        }

        requestMyOrders()
    }

    //*********** Request User's Orders from the Server ********//

    fun requestMyOrders() {

        showProgressDialog("Loading order information...")

        val params = LinkedHashMap<String, String>()
        params["per_page"] = 100.toString()
        params["customer"] = customerID
        params["lang"] = Locale.getDefault().language


        val call = APIClient.getInstance()
            .getAllOrders(
                params
            )

        call.enqueue(object : Callback<List<OrderDetails>> {
            override fun onResponse(
                call: Call<List<OrderDetails>>,
                response: retrofit2.Response<List<OrderDetails>>
            ) {

                hideProgressDialog()

                // Check if the Response is successful
                if (response.isSuccessful) {

                    ordersList.addAll(response.body()!!)
                    ordersListAdapter.notifyDataSetChanged()


                    if (ordersListAdapter.itemCount < 1)
                        empty_record.visibility = View.VISIBLE

                } else {
                    Toast.makeText(this@OrdersActivity, response.message(), Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<List<OrderDetails>>, t: Throwable) {
                hideProgressDialog()
                Toast.makeText(this@OrdersActivity, "NetworkCallFailure : $t", Toast.LENGTH_LONG)
                    .show()
            }
        })
    }
}
