package com.itikarus.miska.tabs

import android.os.Bundle
import android.os.PersistableBundle
import com.itikarus.miska.MainActivity
import com.itikarus.miska.base.BaseActivity

open abstract class BaseTabActivity : BaseActivity(){
    fun getMainActivity() : MainActivity{
        return parent as MainActivity
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mainActivity = getMainActivity()
        mainActivity.onLoadCallbackMap[javaClass.simpleName] = {
            onActivityLoad()
        }
    }

    abstract fun onActivityLoad()
}