package com.itikarus.miska.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager

import com.itikarus.miska.R
import com.itikarus.miska.extentions.loadImage
import kotlinx.android.synthetic.main.layout_slide_img.view.*

import java.util.ArrayList

class ViewPagerAdapter(private val context: Context, private val images: ArrayList<String>) :
    PagerAdapter() {

    override fun getCount(): Int {
        return images.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_slide_img, null)

        view.imageview.loadImage(images[position], R.drawable.bg_landing_placeholder)

        val viewPager = container as ViewPager
        viewPager.addView(view, 0)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val viewPager = container as ViewPager
        val view = `object` as View
        viewPager.removeView(view)
    }
}
