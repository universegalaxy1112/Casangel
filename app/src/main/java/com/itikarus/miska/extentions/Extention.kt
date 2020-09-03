package com.itikarus.miska.extentions

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.text.Html
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import com.itikarus.miska.library.utils.DateTimeUtils
import com.itikarus.miska.library.utils.ImageUtils
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.wang.avi.AVLoadingIndicatorView
import java.io.IOException
import java.lang.Exception
import java.net.URL
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*


fun androidx.recyclerview.widget.RecyclerView.horizontalize(context: Context) {
    this.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
        context,
        androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL,
        false
    )
}

fun RecyclerView.verticalize(context: Context) {
    this.layoutManager = LinearLayoutManager(
        context, RecyclerView.VERTICAL,
        false
    )
}

fun Drawable.getBitmap(): Bitmap {
    return ImageUtils.getBitmap(this)
}

fun View.getBitMap(): Bitmap {
    return ImageUtils.loadBitmapFromView(this)
}

fun Bitmap.saveToFile(filePath: String) {
    ImageUtils.saveBitmapToFile(this, filePath, Bitmap.CompressFormat.PNG)
}

fun String.getBitmapFromUrl(): Bitmap? {
    val url = URL(this)
    return try {
        BitmapFactory.decodeStream(url.openConnection().getInputStream())
    } catch (e: IOException) {
        null
    }
}

fun Date.convertString(): String {
    return DateTimeUtils.getDateSpanishString(this, "dd MMMM yyyy")
}

fun ListView.setListViewHeightBasedOnChildren() {
    val listAdapter = this.adapter ?: return

    val desiredWidth = View.MeasureSpec.makeMeasureSpec(this.width, View.MeasureSpec.UNSPECIFIED)
    var totalHeight = 0
    var view: View? = null
    for (i in 0 until listAdapter.count) {
        view = listAdapter.getView(i, view, this)
        if (i == 0)
            view!!.layoutParams =
                ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT)

        view!!.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED)
        totalHeight += view.measuredHeight
    }
    val params = this.layoutParams
    params.height = totalHeight + this.dividerHeight * (listAdapter.count - 1)
    this.layoutParams = params
}

fun Activity.isNetworkAvailable(): Boolean {
    val connectivity = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

    if (connectivity != null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            val networks = connectivity.allNetworks
            var networkInfo: NetworkInfo

            for (mNetwork in networks) {

                networkInfo = connectivity.getNetworkInfo(mNetwork)

                if (networkInfo.state == NetworkInfo.State.CONNECTED) {
                    return true
                }
            }

        } else {
            val info = connectivity.allNetworkInfo

            if (info != null) {
                for (i in info.indices) {
                    if (info[i].state == NetworkInfo.State.CONNECTED) {
                        return true
                    }
                }
            }
        }
    }

    return false
}

fun TextView.setHtmlText(htmlString: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        this.text = Html.fromHtml(htmlString, Html.FROM_HTML_MODE_COMPACT)
    } else {
        this.text = Html.fromHtml(htmlString)
    }
}

fun ImageView.loadImage(imageURL: String, placeholder: Int) {

    Picasso.get().load(imageURL)
        .fit()
        .placeholder(placeholder ?: 0)
        .into(this, object : Callback {
            override fun onSuccess() {
                Log.d("Picasso", "Success!")
            }

            override fun onError(e: Exception?) {
                Log.e("Picasso", e?.message)
            }
        })

}

fun Dialog.showDialog(context: Context) {
    val metrics = context.resources.displayMetrics
    val width = metrics.widthPixels
    val height = metrics.heightPixels
    this.window!!.setLayout(6 * width / 7, 4 * height / 5)
    this.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    this.window!!.requestFeature(Window.FEATURE_NO_TITLE)
    this.show()
}

fun View.invisiable() {
    this.visibility = View.INVISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.visiable() {
    this.visibility = View.VISIBLE
}

fun Double.convertPriceString(): String {
    val fString: String
    fString = try {
        val nf = NumberFormat.getNumberInstance(Locale.US)
        val formatter = nf as DecimalFormat
        formatter.applyPattern("#,###,###")
        formatter.format(this)
    } catch (e: NumberFormatException) {
        e.printStackTrace()
        ""
    }

    return fString
}

fun ImageView.rotate(degree: Int) {
    this.rotation = degree.toFloat()
}