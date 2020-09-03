@file:Suppress("DEPRECATION")

package com.itikarus.miska

import android.app.LocalActivityManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TabHost
import android.widget.TabWidget
import android.widget.TextView
import com.itikarus.miska.base.PermissionCheckActivity
import com.itikarus.miska.globals.KeyUtils
import com.itikarus.miska.tabs.account.AccountActivity
import com.itikarus.miska.tabs.creations.CreationsActivity
import com.itikarus.miska.tabs.home.HomeActivity
import com.itikarus.miska.tabs.idea.InspirateActivity
import com.itikarus.miska.tabs.store.StoreActivity


class MainActivity : PermissionCheckActivity() {

    private var permissions : Array<String> = arrayOf()

    override fun requiredPermissions(): Array<String> {
        return permissions
    }

    lateinit var tabHost: TabHost
    lateinit var tabWidget: TabWidget
    lateinit var mlam: LocalActivityManager

    val onActivityResultMap = HashMap<String, ((requestCode : Int, resultCode : Int, data : Intent?) -> Unit)>()
    val onLoadCallbackMap = HashMap<String, () -> Unit>()

    private val tabIconResUn = intArrayOf(
        R.drawable.ic_home_u,
        R.drawable.ic_store_u,
        R.drawable.ic_creations_u,
        R.drawable.ic_idea_u,
        R.drawable.ic_account_u
    )

    private val tabIconResSelect = intArrayOf(
        R.drawable.ic_home_s,
        R.drawable.ic_store_s,
        R.drawable.ic_creations_s,
        R.drawable.ic_idea_s,
        R.drawable.ic_account_s
    )

    private lateinit var tabLabels : Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tabLabels = arrayOf(
            getString(R.string.home),
            getString(R.string.store),
            getString(R.string.my_creations),
            getString(R.string.ideas),
            getString(R.string.account)
        )

        tabHost = findViewById(R.id.tabHost)

        mlam = LocalActivityManager(this, false)
        mlam.dispatchCreate(savedInstanceState)
        tabHost.setup(mlam)

        tabWidget = findViewById(android.R.id.tabs)

        // Tab for Home
        val homeSpec = tabHost.newTabSpec("HOME")
        val tabIndicator0 = LayoutInflater.from(this).inflate(R.layout.tab_indicator, tabWidget, false)
        homeSpec.setIndicator(tabIndicator0)
        val intent0 = Intent(this, HomeActivity::class.java)
        homeSpec.setContent(intent0)

        // Tab for Store
        val postSpec = tabHost.newTabSpec("STORE")
        val tabIndicator1 = LayoutInflater.from(this).inflate(R.layout.tab_indicator, tabWidget, false)
        postSpec.setIndicator(tabIndicator1)
        val intent1 = Intent(this, StoreActivity::class.java)
        postSpec.setContent(intent1)

        // Tab for CREATIONS
        val questionSpec = tabHost.newTabSpec("CREATIONS")
        val tabIndicator2 = LayoutInflater.from(this).inflate(R.layout.tab_indicator, tabWidget, false)
        questionSpec.setIndicator(tabIndicator2)
        val intent2 = Intent(this, CreationsActivity::class.java)
        questionSpec.setContent(intent2)

        // Tab for Notification
        val notificationSpec = tabHost.newTabSpec("IDEAS")
        val tabIndicator3 = LayoutInflater.from(this).inflate(R.layout.tab_indicator, tabWidget, false)
        notificationSpec.setIndicator(tabIndicator3)
        val intent3 = Intent(this, InspirateActivity::class.java)
        notificationSpec.setContent(intent3)


        // Tab for Profile
        val profileSpec = tabHost.newTabSpec("ACCOUNT")
        val tabIndicator4 = LayoutInflater.from(this).inflate(R.layout.tab_indicator, tabWidget, false)
        profileSpec.setIndicator(tabIndicator4)
        val intent4 = Intent(this, AccountActivity::class.java)
        profileSpec.setContent(intent4)

        tabHost.addTab(homeSpec)
        tabHost.addTab(postSpec)
        tabHost.addTab(questionSpec)
        tabHost.addTab(notificationSpec)
        tabHost.addTab(profileSpec)

        tabHost.setOnTabChangedListener {
            val tab = tabHost.currentTab
            for (i in 0 until tabHost.tabWidget.childCount) {
                val icon = tabHost.tabWidget.getChildAt(i).findViewById<ImageView>(R.id.tabIcon)
                val label = tabHost.tabWidget.getChildAt(i).findViewById<TextView>(R.id.tabLabel)
                label.text = tabLabels[i].toUpperCase()
                tabHost.tabWidget.getChildAt(i).setBackgroundColor(Color.parseColor("#444242"))

                if (i == tab) {
                    icon.setImageResource(tabIconResSelect[i])
                    label.setTextColor(resources.getColor(R.color.colorPrimary))
                } else {
                    icon.setImageResource(tabIconResUn[i])
                    label.setTextColor(Color.WHITE)
                }
            }

            when(tab){
                0->onLoadCallbackMap[HomeActivity::class.java.simpleName]?.invoke()
                1->onLoadCallbackMap[StoreActivity::class.java.simpleName]?.invoke()
                2->onLoadCallbackMap[CreationsActivity::class.java.simpleName]?.invoke()
                3->onLoadCallbackMap[InspirateActivity::class.java.simpleName]?.invoke()
                4->onLoadCallbackMap[AccountActivity::class.java.simpleName]?.invoke()
            }
        }

        tabHost.currentTab = 1
        tabHost.currentTab = 0
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode){
            KeyUtils.REQUEST_CODE_CAMERA->onActivityResultMap[HomeActivity::class.java.simpleName]?.invoke(requestCode, resultCode, data)
            KeyUtils.REQUEST_CODE_CAMERA_FROM_CREATION_PAGE->onActivityResultMap[CreationsActivity::class.java.simpleName]?.invoke(requestCode, resultCode, data)
            KeyUtils.REQUEST_CODE_CREATE_SPACE->onActivityResultMap[CreationsActivity::class.java.simpleName + "2"]?.invoke(requestCode, resultCode, data)
            KeyUtils.REQUEST_CODE_SHOPPING_CART->onActivityResultMap[StoreActivity::class.java.simpleName]?.invoke(requestCode, resultCode, data)
        }
    }

    fun requestPermission(perms : Array<String>, action : (() -> Unit)? = null){

        permissions = perms
        permissionCheck {
            action?.invoke()
        }
    }

    fun switchTab(i : Int){
        tabHost.currentTab = i
    }
}
