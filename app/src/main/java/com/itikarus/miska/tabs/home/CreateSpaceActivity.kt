package com.itikarus.miska.tabs.home

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.itikarus.miska.R
import com.itikarus.miska.adapters.CategoryListAdapter
import com.itikarus.miska.adapters.ProductDetailImgAdapter
import com.itikarus.miska.base.BaseActivity
import com.itikarus.miska.base.CameraActivity
import com.itikarus.miska.base.PermissionCheckActivity
import com.itikarus.miska.extentions.*
import com.itikarus.miska.globals.GlobalData
import com.itikarus.miska.globals.GlobalStorage
import com.itikarus.miska.globals.KeyUtils
import com.itikarus.miska.library.utils.DateTimeUtils
import com.itikarus.miska.library.utils.DialogUtils
import com.itikarus.miska.library.utils.ImageUtils
import com.itikarus.miska.models.api_response_model.ErrorResponse
import com.itikarus.miska.models.category_model.CategoryDetails
import com.itikarus.miska.models.product_model.ProductDetails
import com.itikarus.miska.models.product_model.ProductImages
import com.itikarus.miska.network.APIClient
import com.itikarus.miska.workspace.ProjectInfo
import com.itikarus.miska.workspace.SaveModule
import com.munon.turboimageview.ImageObject
import com.munon.turboimageview.MultiTouchObject
import com.munon.turboimageview.TurboImageView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_create_space.*
import kotlinx.android.synthetic.main.item_product.view.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.round

class CreateSpaceActivity : PermissionCheckActivity() {

    override fun requiredPermissions(): Array<String> {
        return arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    private var categories = ArrayList<CategoryDetails>()
    private var products = ArrayList<ProductDetails>()
    private var productDetailImages = ArrayList<ProductImages>()

    private var selectedProductModel : ProductDetails? = null

    private lateinit var categoryAdapter : CategoryListAdapter
    private lateinit var productAdapter : ProductListAdapter
    private lateinit var productDetailImageAdapter : ProductDetailImgAdapter
    private var imagePath = ""
    private var projectID = -1

    private var pageNumber = 1
    private var order = "desc"
    private var sortBy = "date"

    private var rotateDegree = 0
    private var currentCategoryId = ""
    private var isLoadMore = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_create_space)

        categoryAdapter = CategoryListAdapter(this, categories)
        categoryAdapter.onItemSelected = {categoryModel ->  categorySelected(categoryModel)}
        productAdapter = ProductListAdapter()
        productDetailImageAdapter = ProductDetailImgAdapter(this, productDetailImages){

            val onLoadImageBitmap : (imageObject : ImageObject) -> Unit = {
                runOnUiThread {
                    it.productModel = selectedProductModel
                    tiv_turboView.addObject(this@CreateSpaceActivity,it)
                    hideProgressDialog()
                }
            }

            showProgressDialog(getString(R.string.loading))
            Thread{
                val bitmap = it.src.getBitmapFromUrl()
                val imageObject = ImageObject(bitmap, it.src, resources)
                onLoadImageBitmap(imageObject)
            }.start()

        }

        rv_categoryList.horizontalize(this)
        rv_categoryList.adapter = categoryAdapter


        rv_productList.horizontalize(this)
        rv_productList.adapter = productAdapter

        productDetailImgListView.horizontalize(this)
        productDetailImgListView.adapter = productDetailImageAdapter


        arrayOf(iv_btnClose, iv_btnSave, iv_btnDone, iv_rotateLeft, iv_rotateRight, iv_btnBack).forEach {view->
            view.setOnClickListener {
                when(it.id){
                    R.id.iv_btnClose-> closeInstructionDialog()
                    R.id.iv_btnSave-> selectSaveMethod()
                    R.id.iv_btnDone-> actionNext()
                    R.id.iv_rotateLeft->rotateBgImageToLeft()
                    R.id.iv_rotateRight->rotateBgImageToRight()
                    R.id.iv_btnBack-> finish()
                }
            }
        }

        if(SaveModule.build(this).isShowedInstructionForWorkspace) rl_dialogInstruction.visibility = View.GONE

        loadProject()
        loadCategories()

        tiv_turboView.turboImageViewEventListener = TurboImageView.TurboImageViewEventListener {
            productDetailView.gone()
        }
    }

    private fun actionNext() {

        val productList = ArrayList<ProductDetails>()
        tiv_turboView.objects.forEach {multiTouchObject->
            val imgObject  = multiTouchObject as ImageObject
            val productModel = imgObject.productModel
            if(productModel != null) productList.add(productModel)
        }

        if(productList.isEmpty()){
            Toast.makeText(this, getString(R.string.select_items), Toast.LENGTH_LONG).show()
            return
        }

        GlobalStorage.addProductsToCart(productList)
        startActivity(ShoppingCartActivity::class.java)

    }

    private fun categorySelected(category : CategoryDetails){
        isLoadMore = false;
        loadProducts(category.id.toString())
    }

    private fun loadProject(){

        projectID = intent.getIntExtra(KeyUtils.INTENT_KEY_CREATE_SPACE, -1)
        when {
            projectID > 0 -> {
                val projectInfo = SaveModule.build(this).getFindProductByID(projectID) ?: return
                rotateDegree = projectInfo.rotationDegree
                iv_bgViewer.rotate(rotateDegree)
                imagePath = projectInfo.backgroundDrawable
                iv_bgViewer.setImageBitmap(ImageUtils.getSafeDecodeBitmap(imagePath, 2500))

                val onLoadImageBitmap : ((images : ArrayList<MultiTouchObject>) -> Unit) = {
                    runOnUiThread {
                        tiv_turboView.loadObjects(it)
                        hideProgressDialog()
                    }
                }

                showProgressDialog(getString(R.string.loading))
                Thread{
                    val imageObjects = ArrayList<MultiTouchObject>()
                    projectInfo.projectModels.forEach {
                        val bitmap = it.imageUrl.getBitmapFromUrl()
                        val imageObject = ImageObject(bitmap, it.imageUrl, resources)
                        imageObject.restoreInfoFromModel(it)
                        imageObjects.add(imageObject)
                        onLoadImageBitmap(imageObjects)
                    }
                }.start()
            }
            projectID == -2 ->{
                imagePath  = intent.getStringExtra(KeyUtils.INTENT_KEY_IMG_PATH)
                Picasso.get().load(imagePath).fit().placeholder(R.drawable.ic_placeholder).into(iv_bgViewer)
            }
            projectID == -1 -> {
                imagePath = intent.getStringExtra(KeyUtils.INTENT_KEY_IMG_PATH)
                iv_bgViewer.setImageBitmap(ImageUtils.getSafeDecodeBitmap(imagePath, 500))
            }
        }
    }

    private fun closeInstructionDialog() {
        rl_dialogInstruction.visibility = View.GONE
        SaveModule.build(this).isShowedInstructionForWorkspace = true
    }

    private fun selectSaveMethod(){

        /*
        if(projectID > 0){

            DialogUtils.showOkayDialog(this, getString(R.string.save_your_project), ""){
                launchInputDialog()
            }

//            DialogUtils.showOkayNoDialog(this, "Save your project", "Please choose your save method.",
//                    object : DialogUtils.OnOkayNoEvent{
//                override fun onOkay() {
//                    launchInputDialog()
//                }
//
//                override fun onNo() {
//                    saveProject(null)
//                }
//            }, "New - Save", "Re - Save")
        }else{
            launchInputDialog()
        }
        */

        permissionCheck {
            launchInputDialog()
        }
    }

    private fun launchInputDialog(){
        MaterialDialog.Builder(this)
                .title(getString(R.string.input_name))
                .input(getString(R.string.type_space_name), "", false) { _, input ->
                    saveProject(input.toString()) }
                .positiveText(getString(R.string.ok))
                .canceledOnTouchOutside(false)
                .positiveColorRes(R.color.colorPrimary)
                .theme(Theme.LIGHT)
                .show()
    }

    private fun saveProject(projectName : String?){

        showCustomProgressDialog(getString(R.string.saving))

        if(!GlobalData.getAppCreationsDirectory().exists()){
            GlobalData.getAppCreationsDirectory().mkdirs()
        }

        val projectInfo = ProjectInfo()

        projectInfo.rotationDegree = rotateDegree

        if(projectName == null){ // Edit and Save
            val currentProject = SaveModule.build(this).getFindProductByID(projectID)!!
            projectInfo.projectName = currentProject.projectName
            SaveModule.build(this).removeProjectByID(projectID)
        }else{// New Save
            projectInfo.projectName = projectName
        }

        val thumbFileName = "${GlobalData.APP_CREATIONS_DIRECTORY_PATH}/${projectName}_thumb_${DateTimeUtils.getCurrentTimeStampForFileName()}"

        rl_workspace.getBitMap().saveToFile(thumbFileName)
        projectInfo.thumbFileName = thumbFileName

        val bgFileName = "${GlobalData.APP_CREATIONS_DIRECTORY_PATH}/${projectName}_bg_${DateTimeUtils.getCurrentTimeStampForFileName()}"

        iv_bgViewer.getBitMap().saveToFile(bgFileName)
        projectInfo.backgroundDrawable = bgFileName

        tiv_turboView.objects.forEach {multiTouchObject->
            val projectModel  = multiTouchObject.cloneProjectModel
            projectInfo.projectModels.add(projectModel)
        }
        projectInfo.projectDt = Date().convertString()
        projectInfo.projectId = SaveModule.build(this).getNextUniqueID()
        SaveModule.build(this).addProject(projectInfo)
        hideProgressDialog()
        Toast.makeText(this, getString(R.string.save_space_success), Toast.LENGTH_LONG).show()

    }

    private inner class ProductListAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<CustomViewHolder2>(){

        var layoutInflater : LayoutInflater? = null
        var selectedItemId = -1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder2 {
            if (layoutInflater == null) layoutInflater = LayoutInflater.from(this@CreateSpaceActivity)
            val view = layoutInflater!!.inflate(R.layout.item_product, parent, false)
            return CustomViewHolder2(view)
        }

        override fun getItemCount(): Int {
            return products.size
        }

        override fun onBindViewHolder(holder1: CustomViewHolder2, position: Int) {
            val model = products[position]

            @Suppress("NAME_SHADOWING")
            holder1.let { holder->
                Picasso.get().load(model.images[0].src).fit().placeholder(R.drawable.ic_placeholder).into(holder.iconViewer)
                holder.nameViewer.text = model.name
                holder.priceViewer.text = model.price.toDouble().convertPriceString()

                val displayMetrics = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(displayMetrics)
                val screenWidth = displayMetrics.widthPixels

                val layoutParam = holder.view.layoutParams
                layoutParam.width = screenWidth / 3

                holder.view.layoutParams = layoutParam

                if(selectedItemId == position){
                    holder.view.setBackgroundResource(R.drawable.outline_selected)
                }else{
                    holder.view.setBackgroundResource(0)
                }

                if(!model.isInStock){
                    holder.view.gone()
                }else{
                    holder.view.setOnClickListener {
                        if(!model.isInStock) return@setOnClickListener
                        selectedItemId = position
                        selectedProductModel = model

                        productDetailView.visiable()
                        productShortDescriptionViewer.setHtmlText(model.shortDescription)

                        productDetailImages.clear()
                        productDetailImages.addAll(model.images)
                        productDetailImageAdapter.notifyDataSetChanged()

                        notifyDataSetChanged()
                    }
                }
            }

        }

        override fun getItemViewType(position: Int): Int {
            return position
        }
    }

    private inner class CustomViewHolder2(itemView : View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView){
        var view : View = itemView.ll_itemView
        var iconViewer : ImageView = itemView.iv_iconViewer
        var nameViewer : TextView = itemView.tv_productNameViewer
        var priceViewer : TextView = itemView.tv_productPriceViewer
    }

    private fun loadCategories(){
        categories.addAll(GlobalStorage.loadProductCategories())
        categoryAdapter.notifyDataSetChanged()
        if(categories.isNotEmpty()){
            isLoadMore = false;
            loadProducts(categories[0].id.toString())
        }
    }


    private fun loadProducts(categoryID : String){
        currentCategoryId = categoryID
        if(!isLoadMore) {
            products.clear()
            productAdapter.notifyDataSetChanged()
            pageNumber = 1
            aiv_loading.visibility = View.VISIBLE
        }
        val params = LinkedHashMap<String, Any>()
        params["category"] = categoryID
        params["page"] = pageNumber
        params["per_page"] = 100
        params["order"] = order
        params["orderby"] = sortBy
        params["lang"] = Locale.getDefault().displayLanguage

        val call = APIClient.getInstance()
            .getAllProducts(
                params
            )

        call.enqueue(object : Callback<List<ProductDetails>> {
            override fun onResponse(
                call: Call<List<ProductDetails>>,
                response: retrofit2.Response<List<ProductDetails>>
            ) {


                if (response.isSuccessful) {
                    if(categoryID == currentCategoryId) {
                        if(!isLoadMore)
                            products.clear()
                        response.body()?.forEach {
                            if (it.isInStock) products.add(it)
                        }
                        productAdapter.notifyDataSetChanged()
                        // Hide the ProgressBar
                        aiv_loading.hide()

                        if((products.size % 100 > 80 || products.size % 100 == 0) && response.body()?.size!! > 0) {
                            isLoadMore = true
                            pageNumber++
                            loadProducts(currentCategoryId)
                        }

                    }
                } else {
                    val converter : Converter<ResponseBody, ErrorResponse> = APIClient.retrofit.responseBodyConverter(ErrorResponse::class.java, arrayOfNulls<Annotation>(0))
                    val error: ErrorResponse
                    error = try {
                        converter.convert(response.errorBody()!!)
                    } catch (e: IOException) {
                        ErrorResponse()
                    }

                    Toast.makeText(this@CreateSpaceActivity, "Error : " + error.message, Toast.LENGTH_SHORT).show()
                }

            }

            override fun onFailure(call: Call<List<ProductDetails>>, t: Throwable) {
                aiv_loading.hide()
                Toast.makeText(this@CreateSpaceActivity, "NetworkCallFailure : $t", Toast.LENGTH_LONG).show()
            }
        })
    }


    private fun rotateBgImageToRight(){
        rotateDegree += 90
        iv_bgViewer.rotate(rotateDegree)
    }

    private fun rotateBgImageToLeft(){
        rotateDegree -= 90
        iv_bgViewer.rotate(rotateDegree)
    }

}
