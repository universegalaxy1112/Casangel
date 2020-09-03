package com.itikarus.miska.tabs.account

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.itikarus.miska.R
import com.itikarus.miska.globals.LocalDB
import kotlinx.android.synthetic.main.fragment_profile.view.*

class ProfileFragment(context: Context) : LinearLayout(context) {

    var parentActivity : AccountActivity?= null

    init {
        parentActivity = context as AccountActivity
        parentActivity?.setTitle("MI CUENTA")
        parentActivity?.drawerMenuSetting()
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        layoutInflater.inflate(R.layout.fragment_profile, this, true)
        initView()
    }

    private fun initView(){
        val localDB = LocalDB.build(context)

        displayContent(tv_nameViewer, localDB.userDisplayName)
        displayContent(tv_usernameViewer, localDB.username)
        displayContent(tv_emailViewer, localDB.userEmail)
        displayContent(tv_phoneNumViewer, localDB.userDisplayName)
        displayContent(tv_address1Viewer, localDB.address1)
        displayContent(tv_address2Viewer, localDB.address2)
        displayContent(tv_phoneNumViewer, localDB.userPhone)


        ll_btnMYCreations.setOnClickListener {
            parentActivity?.getMainActivity()?.switchTab(2)
        }

        ll_btnMyOrders.setOnClickListener {
            parentActivity?.startActivity(OrdersActivity::class.java)
        }
    }

    private fun displayContent(viewer : TextView, str : String?){
        if(str.isNullOrEmpty()){
            viewer.visibility = View.GONE
        }else{
            viewer.text = str
        }
    }
}
