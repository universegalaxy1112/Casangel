package com.itikarus.miska.tabs.home

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.itikarus.miska.R
import com.itikarus.miska.base.BaseActivity
import com.itikarus.miska.extentions.horizontalize
import com.itikarus.miska.globals.GlobalStorage
import com.itikarus.miska.globals.KeyUtils.INTENT_KEY_CREATE_SPACE
import com.itikarus.miska.globals.KeyUtils.INTENT_KEY_IMG_PATH
import com.itikarus.miska.library.utils.DialogUtils
import com.itikarus.miska.models.api_response_model.ErrorResponse
import com.itikarus.miska.models.post_model.PostCategory
import com.itikarus.miska.models.post_model.PostDetails
import com.itikarus.miska.network.APIClient
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_quick_create.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Converter
import java.io.IOException
import java.lang.Exception
import java.util.*

class QuickCreateActivity : BaseActivity() {

    private var tempModels = ArrayList<PostDetails>()
    private lateinit var categoryListAdapter : CategoryListAdapter
    private lateinit var templateGridAdapter : TemplateGridAdapter
   // private lateinit var templateRecyclerAdapter : TemplateRecyclerAdapter

    private var selectedItemPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quick_create)

        // Category horizontal list setting
        categoryListAdapter = CategoryListAdapter(GlobalStorage.loadTemplatesCategories())
        categoryListAdapter.unSelectedItemColor = Color.BLACK
        categoryListAdapter.onItemSelected = {categoryModel -> categorySelected(categoryModel)}

        rv_categoryList.horizontalize(this)
        rv_categoryList.adapter = categoryListAdapter

        //template grid setting
        templateGridAdapter = TemplateGridAdapter()
        //gv_templateGrid.adapter = templateGridAdapter
        gv_templateGrid.adapter = templateGridAdapter

        iv_btnDone.setOnClickListener {
            if(selectedItemPosition < 0){
                Toast.makeText(this, "Please select your template", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val intent = Intent(this, CreateSpaceActivity::class.java)
            intent.putExtra(INTENT_KEY_CREATE_SPACE, -2)
            intent.putExtra(INTENT_KEY_IMG_PATH, tempModels[selectedItemPosition].imgUrl)

            startActivity(intent)
        }

        iv_btnBack.setOnClickListener {
            finish()
        }

        loadTemplates(GlobalStorage.loadTemplatesCategories()[0].id.toString())
    }

    private fun categorySelected(categoryModel: PostCategory){
        loadTemplates(categoryModel.id.toString())
    }

    private fun loadTemplates(categoryID : String){

        tempModels.clear()
        templateGridAdapter.notifyDataSetChanged()

        aiv_loading.visibility = View.VISIBLE

        val params = LinkedHashMap<String, Any>()
        params["page"] = "1"
        params["_embed"] = "true"
        params["lang"] = Locale.getDefault().language
        params["categories"] = categoryID

        val call = APIClient.getInstance()
            .getAllPosts(
                params
            )

        call.enqueue(object : retrofit2.Callback<List<PostDetails>> {
            override fun onResponse(call: Call<List<PostDetails>>, response: retrofit2.Response<List<PostDetails>>) {
                aiv_loading.hide()
                if (response.isSuccessful) {

                    val quickPosts : List<PostDetails> = response.body()!!
                    tempModels.clear()
                    tempModels.addAll(quickPosts)
                    templateGridAdapter.notifyDataSetChanged()
                } else {
                    val converter : Converter<ResponseBody, ErrorResponse> =
                        APIClient.retrofit.responseBodyConverter(ErrorResponse::class.java, arrayOfNulls<Annotation>(0))
                    val error: ErrorResponse
                    error = try {
                        converter.convert(response.errorBody()!!)
                    } catch (e: IOException) {
                        ErrorResponse()
                    }
                    DialogUtils.showOkayDialog(this@QuickCreateActivity, "Sorry!", "Error : ${error.message}")
                }
            }

            override fun onFailure(call: Call<List<PostDetails>>, t: Throwable) {
                aiv_loading.hide()
                DialogUtils.showOkayDialog(this@QuickCreateActivity, "Sorry!", "NetworkCallFailure : $t")
            }
        })
    }

    private inner class TemplateGridAdapter : BaseAdapter(){
        private var layoutInflater : LayoutInflater? = null

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            if(layoutInflater == null) layoutInflater = getLayoutInflater()

            val view : View
            val cvh : CustomViewHolder

            if(convertView == null){
                view = layoutInflater!!.inflate(R.layout.item_template, parent, false)
                cvh = CustomViewHolder(view)
                view.tag = cvh
            }else{
                view = convertView
                cvh = view.tag as CustomViewHolder
            }

            val model = getItem(position)
            with(cvh){

                val imgUrl = model.imgUrl

                if(imgUrl.isNullOrEmpty()){
                    imgViewer.setImageResource(R.drawable.ic_placeholder)
                }else{
                    Picasso.get().load(imgUrl).fit().placeholder(R.drawable.ic_placeholder).into(imgViewer, object : Callback{
                        override fun onSuccess() {
                            if(selectedItemPosition == position){
                                imgViewer.drawable.setColorFilter(0x50FF0000,android.graphics.PorterDuff.Mode.SRC_ATOP)
                            }else{
                                imgViewer.drawable.clearColorFilter()
                            }
                        }

                        override fun onError(e: Exception?) {

                        }
                    })
                }

                imgViewer.setOnClickListener {
                    selectedItemPosition = position
                    //imgViewer.drawable.setColorFilter(0x50FF0000,android.graphics.PorterDuff.Mode.SRC_ATOP)
                    notifyDataSetChanged()
                }
            }
            return view
        }

        override fun getItem(position: Int): PostDetails {
            return tempModels[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return tempModels.size
        }

        inner class CustomViewHolder(view : View){
            val imgViewer = view.findViewById<ImageView>(R.id.siv_imgViewer)
        }

    }
 /*   private inner class TemplateRecyclerAdapter: RecyclerView.Adapter<TemplateRecyclerAdapterCustomViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateRecyclerAdapter.CustomViewHolder {
            TODO("Not yet implemented")
        }

        override fun getItemCount(): Int {
            TODO("Not yet implemented")
        }

        override fun onBindViewHolder(holder: TemplateRecyclerAdapter.CustomViewHolder, position: Int) {
            TODO("Not yet implemented")
        }



    }*/


    private inner class CategoryListAdapter(models : ArrayList<PostCategory>) : androidx.recyclerview.widget.RecyclerView.Adapter<CustomViewHolder>(){

        private val categories = models
        private var layoutInflater : LayoutInflater? = null
        private var selectedItemId = 0
        var onItemSelected : ((categoryModel : PostCategory) -> Unit)? = null

        var selectedItemColor = resources.getColor(R.color.colorPrimary)
        var unSelectedItemColor = resources.getColor(R.color.white)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            if (layoutInflater == null) layoutInflater = LayoutInflater.from(this@QuickCreateActivity)
            val view = layoutInflater!!.inflate(R.layout.item_category, parent, false)
            return CustomViewHolder(view)
        }

        override fun getItemCount(): Int {
            return categories.size
        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            val model = categories[position]

            @Suppress("NAME_SHADOWING")
            holder.let { holder->
                val categoryIcon : String? = model.description
                if(categoryIcon.isNullOrEmpty()){
                    holder.iconViewer.setImageResource(R.drawable.ic_category_place_holder)
                }else{
                    Picasso.get().load(categoryIcon).fit().placeholder(R.drawable.ic_category_place_holder).into(holder.iconViewer)
                }
                holder.labelViewer.text = model.name
                if(selectedItemId == position){
                    holder.iconViewer.setColorFilter(selectedItemColor, PorterDuff.Mode.SRC_IN)
                    holder.labelViewer.setTextColor(selectedItemColor)
                }else{
                    holder.iconViewer.setColorFilter(unSelectedItemColor, PorterDuff.Mode.SRC_IN)
                    holder.labelViewer.setTextColor(unSelectedItemColor)
                }

                holder.view.setOnClickListener {
                    if(selectedItemId == position) return@setOnClickListener
                    selectedItemId = position
                    notifyDataSetChanged()
                    onItemSelected?.invoke(model)
                }
            }

        }

        override fun getItemViewType(position: Int): Int {
            return position
        }

    }

    class CustomViewHolder(itemView : View) : ViewHolder(itemView){
        var view = itemView
        var iconViewer : ImageView = itemView.findViewById(R.id.iv_iconViewer)
        var labelViewer : TextView = itemView.findViewById(R.id.tv_labelViewer)
    }
}
