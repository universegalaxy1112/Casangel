package com.itikarus.miska.tabs.home

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.itikarus.miska.R
import com.itikarus.miska.base.BaseActivity
import com.itikarus.miska.globals.GlobalStorage
import com.itikarus.miska.models.ArticleModel
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_trends.*


class TrendsActivity : BaseActivity() {

    private val articles = ArrayList<ArticleModel>()
    private lateinit var mAdapter: ArticleListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trends)

        mAdapter = ArticleListAdapter()
        lv_trends.adapter = mAdapter

        iv_btnBack.setOnClickListener {
            finish()
        }

        loadData()
    }

    private fun loadData() {

        articles.clear()

        GlobalStorage.loadTrendsInfos().forEach {
            val articleModel = ArticleModel()
            articleModel.imgUrl = it.imgUrl
            articleModel.title = it.title.rendered
            //articleModel.content = extractContent(it.content.rendered)
            articleModel.content = it.content.rendered
            articles.add(articleModel)
        }

        mAdapter.notifyDataSetChanged()
    }

    private inner class ArticleListAdapter : BaseAdapter() {

        var layoutInflater: LayoutInflater? = null

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            if (layoutInflater == null) layoutInflater = getLayoutInflater()
            val view: View
            val cvh: CustomViewHolder

            if (convertView == null) {
                view = layoutInflater!!.inflate(R.layout.item_article, parent, false)
                cvh = CustomViewHolder(view)
                view.tag = cvh
            } else {
                view = convertView
                cvh = view.tag as CustomViewHolder
            }

            val model = getItem(position)
            with(cvh) {
                if (model.imgUrl.isNullOrEmpty()) {
                    imgViewer.setImageResource(R.drawable.ic_placeholder)
                } else {
                    Picasso.get().load(model.imgUrl)
                        .fit()
                        .placeholder(R.drawable.ic_placeholder)
                        .into(imgViewer)
                }

                titleViewer.text = model.title
                contentViewer.text = stripHtml(model.content)
            }
            return view
        }

        override fun getItem(position: Int): ArticleModel {
            return articles[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return articles.size
        }

        inner class CustomViewHolder(view: View) {
            val imgViewer = view.findViewById<ImageView>(R.id.siv_imgViewer)
            val titleViewer = view.findViewById<TextView>(R.id.tv_titleViewer)
            val contentViewer = view.findViewById<TextView>(R.id.tv_content)
        }

    }

    private fun extractContent(rawString: String): String {
        return try {
            val startIdx = rawString.indexOf("<figcaption>")
            val endIdx = rawString.indexOf("</figcaption>")
            rawString.substring(startIdx + 12, endIdx)
        } catch (e: Exception) {
            ""
        }
    }

    private fun stripHtml(html: String): String {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            Html.fromHtml(html).toString()
        }
    }
}
