package com.itikarus.miska

import android.content.Context
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.orm.SugarContext


class MyApp : MultiDexApplication() {

    override fun attachBaseContext(context: Context) {
        super.attachBaseContext(context)
        MultiDex.install(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        SugarContext.terminate()
    }

    override fun onCreate() {
        super.onCreate()

        SugarContext.init(this)
    }

    init {
        mInstance = this
    }

    companion object {
        private var mInstance: MyApp? = null
    }



}
