package com.itikarus.miska.tabs.account

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.itikarus.miska.R
import com.itikarus.miska.extentions.invisiable
import com.itikarus.miska.extentions.visiable
import com.itikarus.miska.globals.LocalDB
import com.itikarus.miska.tabs.BaseTabActivity
import kotlinx.android.synthetic.main.activity_account.*
import kotlinx.android.synthetic.main.layout_drawer.*

class AccountActivity : BaseTabActivity() {

    override fun onActivityLoad() {
        rl_fragment.removeAllViews()
        if (LocalDB.build(this).hasLoginAuth) {
            addFragment(ProfileFragment(this))
        } else {
            addFragment(LoginFragment(this))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)
    }

    fun setTitle(title: String) {
        tv_titleViewer.text = title
    }

    fun drawerMenuSetting() {
        if (LocalDB.build(this).hasLoginAuth) {
            iv_btnLogout.visiable()

            iv_btnLogout.setOnClickListener { logout() }
        } else {
            iv_btnLogout.invisiable()
        }
    }

    fun addFragment(fragment: View) {
        rl_fragment.removeAllViews()
        rl_fragment.addView(fragment)
    }

    private fun logout() {
        drawer_layout.closeDrawers()
        LocalDB.build(this).clearDB()
        Handler().postDelayed({ runOnUiThread { addFragment(LoginFragment(this)) } }, 300)
    }
}
