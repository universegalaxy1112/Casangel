package com.itikarus.miska.tabs.home

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import com.itikarus.miska.R
import com.itikarus.miska.SearchActivity
import com.itikarus.miska.adapters.ViewPagerAdapter
import com.itikarus.miska.base.CameraActivity
import com.itikarus.miska.globals.GlobalStorage
import com.itikarus.miska.globals.KeyUtils.INTENT_KEY_IMG_PATH
import com.itikarus.miska.globals.KeyUtils.REQUEST_CODE_CAMERA
import com.itikarus.miska.library.utils.DialogUtils
import com.itikarus.miska.tabs.BaseTabActivity
import kotlinx.android.synthetic.main.activity_home.*



class HomeActivity : BaseTabActivity() {

    override fun onActivityLoad() {

    }

    private val CHANGE_BG_IMG_PERIOD = 8000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        getMainActivity().onActivityResultMap[javaClass.simpleName] = {requestCode, resultCode, data ->
            if(requestCode == REQUEST_CODE_CAMERA && resultCode == Activity.RESULT_OK){
                val imagePath = data?.getStringExtra(INTENT_KEY_IMG_PATH)
                val intent = Intent(this, CreateSpaceActivity::class.java)
                intent.putExtra(INTENT_KEY_IMG_PATH, imagePath)
                startActivity(intent)
            }
        }

        arrayOf(btnCreateSpace, btnTemplates, btnTrends, btnStore, civ_searchBtn).forEach {
            it.setOnClickListener {view->
                when(view.id){
                    R.id.btnCreateSpace->{

                        getMainActivity().requestPermission(arrayOf(
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )){
                            val intent = Intent(this, CameraActivity::class.java)
                            getMainActivity().startActivityForResult(intent, REQUEST_CODE_CAMERA)
                        }
                    }

                    R.id.btnTemplates->{
                        if(GlobalStorage.loadTemplatesCategories().isNotEmpty()){
                            startActivity(QuickCreateActivity::class.java)
                        }else{
                            DialogUtils.showOkayDialog(this@HomeActivity, "Sorry!",
                                "Server is repairing. Please try later.")
                        }
                    }

                    R.id.btnStore ->{
                        if(GlobalStorage.loadProductCategories().isNotEmpty()){
                            getMainActivity().switchTab(1)
                        }else{
                            DialogUtils.showOkayDialog(this@HomeActivity, "Sorry!", "Server is repairing. Please try later.")
                        }
                    }

                    R.id.btnTrends ->{
                        if(GlobalStorage.loadTrendsInfos().isNotEmpty()){
                            startActivity(TrendsActivity::class.java)
                        }else{
                            DialogUtils.showOkayDialog(this@HomeActivity, "Sorry!", "Server is repairing. Please try later.")
                        }
                    }

                    R.id.civ_searchBtn->{
                        startActivity(SearchActivity::class.java)
                    }
                }
            }
        }

        val imgUrls = ArrayList<String>()

        for(i in 0..100){
            /*GlobalStorage.loadLandingBackgroundImages().forEach {
                imgUrls.add(it)
            }*/

            imgUrls.add("https://casangel.com.co/wp-content/uploads/2019/08/hazloreal6.jpg");
            imgUrls.add("https://casangel.com.co/wp-content/uploads/2019/08/hazloreal4.jpg");
            imgUrls.add("https://casangel.com.co/wp-content/uploads/2019/08/hazloreal5.jpg");
        }

        imageCnt = imgUrls.size

        val imgSlideAdapter = ViewPagerAdapter(this, imgUrls)
        landingViewPager.adapter = imgSlideAdapter

        backgroundImgSetting()
    }

    private var currentBgImgIndex = 0
    private var imageCnt = 0
    private fun backgroundImgSetting(){

        val backgroundImgChangeHandler = object : Handler(mainLooper){
            override fun handleMessage(msg: Message?) {
                if(currentBgImgIndex >= imageCnt){
                    currentBgImgIndex = 0
                }

                landingViewPager.currentItem = currentBgImgIndex
                currentBgImgIndex++

                this.sendEmptyMessageDelayed(0, CHANGE_BG_IMG_PERIOD)
            }
        }

        backgroundImgChangeHandler.sendEmptyMessage(0)
    }

}
