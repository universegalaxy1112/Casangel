package com.itikarus.miska.tabs.idea

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.itikarus.miska.R
import com.itikarus.miska.dialog.ShowImgDialog
import com.itikarus.miska.extentions.horizontalize
import com.itikarus.miska.extentions.showDialog
import com.itikarus.miska.globals.GlobalStorage
import com.itikarus.miska.library.utils.DialogUtils
import com.itikarus.miska.models.InspirateModel
import com.itikarus.miska.models.api_response_model.ErrorResponse
import com.itikarus.miska.models.post_model.PostCategory
import com.itikarus.miska.models.post_model.PostDetails
import com.itikarus.miska.network.APIClient
import com.itikarus.miska.tabs.BaseTabActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_inspirate.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Converter
import java.io.IOException
import java.util.*

class InspirateActivity : BaseTabActivity() {

    override fun onActivityLoad() {

    }

    private val categories = ArrayList<PostCategory>()
    private val inspirates = ArrayList<PostDetails>()

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var inspirateAdapter: InspirateListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inspirate)

        categories.clear()
        categories.addAll(GlobalStorage.loadInspirateCategories())
        rv_categoryList.horizontalize(this)
        categoryAdapter = CategoryAdapter()
        rv_categoryList.adapter = categoryAdapter
        inspirateAdapter = InspirateListAdapter()
        sgv_inspirateList.adapter = inspirateAdapter
        loadInspirateData(categories[0].id.toString())

    }

    override fun onBackPressed() {
        getMainActivity().switchTab(0)
    }

    private inner class CategoryAdapter :
        androidx.recyclerview.widget.RecyclerView.Adapter<CategoryAdapter.CustomViewHolder>() {

        var layoutInflater: LayoutInflater? = null

        val backgrounds = arrayOf(
            R.drawable.background_round_2,
            R.drawable.background_round_3,
            R.drawable.background_round_4,
            R.drawable.background_round_5
        )

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            if (layoutInflater == null) layoutInflater = LayoutInflater.from(this@InspirateActivity)
            val view = layoutInflater!!.inflate(R.layout.item_ins_category, parent, false)
            return CustomViewHolder(view)
        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            val model = categories[position]

            holder.categoryNameViewer?.text = model.name
            holder.categoryNameViewer?.setBackgroundResource(backgrounds[position % 4])
            holder.categoryNameViewer.setOnClickListener {
                loadInspirateData(model.id.toString())
            }
        }

        override fun getItemCount(): Int {
            return categories.size
        }

        inner class CustomViewHolder(view: View) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
            val categoryNameViewer = view.findViewById<TextView>(R.id.tv_CategoryNameViewer)
        }

    }

    private inner class InspirateListAdapter : BaseAdapter() {

        var layoutInflater: LayoutInflater? = null

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            if (layoutInflater == null) layoutInflater = getLayoutInflater()

            val view: View
            val cvh: CustomViewHolder

            if (convertView == null) {
                view = layoutInflater!!.inflate(R.layout.item_inspirate, parent, false)
                cvh = CustomViewHolder(view)
                view.tag = cvh
            } else {
                view = convertView
                cvh = view.tag as CustomViewHolder
            }

            val model = getItem(position)

            with(cvh) {

                val imgUrl: String? = model.imgUrl
                val content = model.title.rendered

                if (imgUrl.isNullOrEmpty()) {
                    imgViewer.setImageResource(R.drawable.ic_placeholder)
                } else {
                    Picasso.get().load(imgUrl)
                        .fit()
                        .placeholder(R.drawable.ic_placeholder)
                        .into(imgViewer)
                }
                contentViewer.text = content

                if (position == 1) {
                    space.visibility = View.VISIBLE
                } else {
                    space.visibility = View.GONE
                }

                imgViewer.setOnClickListener {
                    val imgViewerDialog = ShowImgDialog(this@InspirateActivity, imgUrl, content)
                    imgViewerDialog.showDialog(this@InspirateActivity)
                }
            }

            return view
        }

        override fun getItem(position: Int): PostDetails {
            return inspirates[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return inspirates.size
        }

        inner class CustomViewHolder(view: View) {
            val imgViewer = view.findViewById<ImageView>(R.id.riv_imgViewer)
            val contentViewer = view.findViewById<TextView>(R.id.tv_content)
            val space = view.findViewById<View>(R.id.topSpace)
        }

    }

    private fun loadInspirateData(categoryId: String) {
        inspirates.clear()
        inspirateAdapter.notifyDataSetChanged()

        aiv_loading.visibility = View.VISIBLE

        val params = LinkedHashMap<String, Any>()
        params["page"] = "1"
        params["_embed"] = "true"
        params["lang"] = Locale.getDefault().language
        params["categories"] = categoryId

        val call = APIClient.getInstance()
            .getAllPosts(
                params
            )

        call.enqueue(object : retrofit2.Callback<List<PostDetails>> {
            override fun onResponse(
                call: Call<List<PostDetails>>,
                response: retrofit2.Response<List<PostDetails>>
            ) {
                aiv_loading.hide()
                if (response.isSuccessful) {

                    val inspiratesPosts: List<PostDetails> = response.body()!!
                    inspirates.clear()
                    inspirates.addAll(inspiratesPosts)
                    inspirateAdapter.notifyDataSetChanged()
                } else {
                    val converter: Converter<ResponseBody, ErrorResponse> =
                        APIClient.retrofit.responseBodyConverter(
                            ErrorResponse::class.java,
                            arrayOfNulls<Annotation>(0)
                        )
                    val error: ErrorResponse
                    error = try {
                        converter.convert(response.errorBody()!!)
                    } catch (e: IOException) {
                        ErrorResponse()
                    }
                    DialogUtils.showOkayDialog(
                        this@InspirateActivity,
                        "Sorry!",
                        "Error : ${error.message}"
                    )
                }
            }

            override fun onFailure(call: Call<List<PostDetails>>, t: Throwable) {
                aiv_loading.hide()
                DialogUtils.showOkayDialog(
                    this@InspirateActivity,
                    "Sorry!",
                    "NetworkCallFailure : $t"
                )
            }
        })
    }

}
