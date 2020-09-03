package com.itikarus.miska

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.itikarus.miska.base.BaseActivity
import com.itikarus.miska.extentions.isNetworkAvailable
import com.itikarus.miska.globals.GlobalStorage
import com.itikarus.miska.models.api_response_model.ErrorResponse
import com.itikarus.miska.models.category_model.CategoryDetails
import com.itikarus.miska.models.post_model.PostCategory
import com.itikarus.miska.models.post_model.PostDetails
import com.itikarus.miska.network.APIClient
import kotlinx.android.synthetic.main.activity_splash.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response
import java.io.IOException
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.collections.ArrayList

class SplashActivity : BaseActivity() {

    private lateinit var myTask: MyTask
    private var countdownLatch: CountDownLatch? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({

            myTask = MyTask()
            myTask.execute()
        }, 1000)
    }

    @SuppressLint("StaticFieldLeak")
    private inner class MyTask : AsyncTask<String, Void, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: String): String {

            // Check for Internet Connection from the static method of Helper class
            if (this@SplashActivity.isNetworkAvailable()) {

                // Call the method of StartAppRequests class to process App Startup Requests
                countdownLatch = CountDownLatch(5)
                loadInitialData()
                countdownLatch?.await()
                return "1"
            } else {
                return "0"
            }
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)

            if (result.equals("0", ignoreCase = true)) {

                progressBar.visibility = View.GONE

                showInternetErrorMsg()

            } else {
                if (GlobalStorage.loadProductCategories().isNotEmpty() &&
                    GlobalStorage.loadInspirateCategories().isNotEmpty() &&
                    GlobalStorage.loadTrendsInfos().isNotEmpty()/* &&
                    GlobalStorage.loadLandingBackgroundImages().isNotEmpty()*/ &&
                    GlobalStorage.loadTemplatesCategories().isNotEmpty()
                ) {
                    restartActivity(MainActivity::class.java)
                } else {
                    progressBar.visibility = View.GONE
                    showInternetErrorMsg()
                }
            }
        }

    }

    private fun showInternetErrorMsg() {
        // No Internet Connection
        @Suppress("DEPRECATION")
        Snackbar.make(
            progressBar,
            Html.fromHtml("<font color=\"#ffffff\">${getString(R.string.no_internet)}</font>"),
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(getString(R.string.retry)) {
                progressBar.visibility = View.VISIBLE

                // Restart MyTask after 3 seconds
                Handler().postDelayed({
                    myTask = MyTask()
                    myTask.execute()
                }, 1000)
            }
            .show()
    }

    private fun loadInitialData() {
        loadProductCategories()
        loadInspirateCategories()
        loadTemplatesCategories()
        loadTrendsData()
        loadLandingPageImages()
    }

    private fun loadProductCategories() {
        val params = LinkedHashMap<String, String>()

        params["page"] = "1"
        params["per_page"] = "100"
        params["lang"] = Locale.getDefault().language

        val call = APIClient.getInstance()
            .getAllCategories(params)

        try {

            val response = call.execute()

            if (response.isSuccessful) {
                val categoryList = response.body() as ArrayList<CategoryDetails>
                categoryList.sort()
                GlobalStorage.addProductCategories(categoryList)
            }
            countdownLatch?.countDown()
        } catch (e: IOException) {
            e.printStackTrace()
            countdownLatch?.countDown()
        }
    }

    private fun loadLandingPageImages() {


        val call = APIClient.getInstance()
            .getSinglePost(
                "10462" // 7494 is post id of the post that has name as "Landing page Images" in wp admin page.
            )

        call.enqueue(object : Callback<PostDetails> {
            override fun onResponse(call: Call<PostDetails>, response: Response<PostDetails>) {
                if (response.isSuccessful) {

                    val postDetail = response.body()!!

                    val contentString = postDetail.content.rendered

                    val splits = contentString.split("<img src=\"")

                    val landingBackgroundImgUrls = ArrayList<String>()
                    splits.forEach {
                        if (it.startsWith("https:")) {
                            val subSplit = it.split("?resize=")
                            landingBackgroundImgUrls.add(subSplit[0])
                        }
                    }

                    GlobalStorage.addLandingBackgroundImages(landingBackgroundImgUrls)

                } else {
                    val converter: Converter<ResponseBody, ErrorResponse> =
                        APIClient.retrofit.responseBodyConverter(
                            ErrorResponse::class.java,
                            arrayOfNulls(0)
                        )

                    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
                    var error: ErrorResponse
                    try {
                        error = converter.convert(response.errorBody()!!)
                    } catch (e: IOException) {
                        error = ErrorResponse()
                    }
                }
                countdownLatch?.countDown()
            }

            override fun onFailure(call: Call<PostDetails>, t: Throwable) {
                t.printStackTrace()
                countdownLatch?.countDown()
            }
        })
    }

    private fun loadInspirateCategories() {
        val params = LinkedHashMap<String, String>()
        params["page"] = "1"
        params["parent"] = "329"  // inspirate category parent category id
        params["lang"] = Locale.getDefault().language
        params["per_page"] = "100"

        val call = APIClient.getInstance()
            .getPostCategories(
                params
            )

        call.enqueue(object : Callback<List<PostCategory>> {
            override fun onResponse(
                call: Call<List<PostCategory>>,
                response: retrofit2.Response<List<PostCategory>>
            ) {

                if (response.isSuccessful) {

                    val inspirateCategories: List<PostCategory> = response.body()!!

                    GlobalStorage.addInspirateCategories(inspirateCategories)
                } else {
                    val converter: Converter<ResponseBody, ErrorResponse> =
                        APIClient.retrofit.responseBodyConverter(
                            ErrorResponse::class.java,
                            arrayOfNulls<Annotation>(0)
                        )

                    @Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE")
                    val error: ErrorResponse = try {
                        converter.convert(response.errorBody()!!)
                    } catch (e: IOException) {
                        ErrorResponse()
                    }
                    //DialogUtils.showOkayDialog(this@HomeActivity, "Sorry!", "Error : ${error.message}")
                }

                countdownLatch?.countDown()
            }

            override fun onFailure(call: Call<List<PostCategory>>, t: Throwable) {
                //DialogUtils.showOkayDialog(this@HomeActivity, "Sorry!", "NetworkCallFailure : $t")
                countdownLatch?.countDown()
            }
        })
    }

    private fun loadTemplatesCategories() {

        val params = LinkedHashMap<String, String>()
        params["page"] = "1"
        params["parent"] = "328" // Template category parent category id
        params["lang"] = Locale.getDefault().language
        params["per_page"] = "100"

        val call = APIClient.getInstance()
            .getPostCategories(
                params
            )

        call.enqueue(object : Callback<List<PostCategory>> {
            override fun onResponse(
                call: Call<List<PostCategory>>,
                response: retrofit2.Response<List<PostCategory>>
            ) {

                if (response.isSuccessful) {


                    val templates: List<PostCategory> = response.body()!!

                    GlobalStorage.addTemplatesCategories(templates)

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
                    //DialogUtils.showOkayDialog(this@HomeActivity, "Sorry!", "Error : ${error.message}")
                }

                countdownLatch?.countDown()
            }

            override fun onFailure(call: Call<List<PostCategory>>, t: Throwable) {
                //DialogUtils.showOkayDialog(this@HomeActivity, "Sorry!", "NetworkCallFailure : $t")
                countdownLatch?.countDown()
            }
        })
    }

    private fun loadTrendsData() {

        val params = LinkedHashMap<String, Any>()
        params["page"] = "1"
        params["_embed"] = "true"
        params["lang"] = Locale.getDefault().language
        params["categories"] = "332"

        val call = APIClient.getInstance()
            .getAllPosts(
                params
            )

        call.enqueue(object : Callback<List<PostDetails>> {
            override fun onResponse(
                call: Call<List<PostDetails>>,
                response: retrofit2.Response<List<PostDetails>>
            ) {

                if (response.isSuccessful) {

                    val trends: List<PostDetails> = response.body()!!

                    GlobalStorage.addTrendsInfo(trends)


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
                    //DialogUtils.showOkayDialog(this@HomeActivity, "Sorry!", "Error : ${error.message}")
                }
                countdownLatch?.countDown()
            }

            override fun onFailure(call: Call<List<PostDetails>>, t: Throwable) {
                //DialogUtils.showOkayDialog(this@HomeActivity, "Sorry!", "NetworkCallFailure : $t")
                countdownLatch?.countDown()
            }
        })
    }
}
