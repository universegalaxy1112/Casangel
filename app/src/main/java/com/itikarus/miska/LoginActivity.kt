package com.itikarus.miska

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.itikarus.miska.base.BaseActivity
import com.itikarus.miska.globals.LocalDB
import com.itikarus.miska.library.utils.ValidateInputs
import com.itikarus.miska.models.api_response_model.ErrorResponse
import com.itikarus.miska.models.user_model.UserData
import com.itikarus.miska.network.APIClient
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.dialog_input.view.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response
import java.io.IOException


class LoginActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginBtn.setOnClickListener {
            val isValidData = validateLogin()

            if (isValidData) {
                //Proceed User Login
                processLogin()
            }
        }

        login_signupText.setOnClickListener {
            finish()
        }

        iv_btnBack.setOnClickListener {
            finish()
        }

        forgot_password_text.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.dialog_input, null)
            dialog.setView(dialogView)
            dialog.setCancelable(true)

            val dialog_button = dialogView.dialog_button
            val dialog_input = dialogView.dialog_input

            dialog_button.text = getString(R.string.send)


            val alertDialog = dialog.create()
            alertDialog.show()

            dialog_button.setOnClickListener {
                if (ValidateInputs.isValidEmail(dialog_input.text.toString().trim { it <= ' ' })) {
                    // Request for Password Reset
                    processForgotPassword(dialog_input.text.toString())

                } else {
                    Snackbar.make(parentView, getString(R.string.invalid_email), Snackbar.LENGTH_LONG).show()
                }

                alertDialog.dismiss()
            }
        }
    }

    private fun processLogin() {

        showProgressDialog(getString(R.string.wait))

        val call = APIClient.getInstance()
            .processLogin(
                "cool",
                user_email.text.toString().trim { it <= ' ' },
                user_password.text.toString().trim { it <= ' ' }
            )

        call.enqueue(object : Callback<UserData> {
            override fun onResponse(call: Call<UserData>, response: Response<UserData>) {

                hideProgressDialog()

                if (response.isSuccessful) {

                    if ("ok".equals(
                            response.body()!!.status,
                            ignoreCase = true
                        ) && response.body()!!.cookie != null
                    ) {

                        // Get the User Details from Response
                        val userDetails = response.body()!!.userDetails
                        userDetails.cookie = response.body()!!.cookie

                        if (response.body()!!.id != null) {
                            userDetails.id = response.body()!!.id
                        } else {
                            userDetails.id = userDetails.id
                        }

                        if (response.body()!!.user_login != null) {
                            userDetails.username = response.body()!!.user_login
                        } else {
                            userDetails.username = userDetails.username
                        }

                        if (userDetails.name != null) {
                            userDetails.display_name = userDetails.name
                        }

                        val localDB = LocalDB.build(this@LoginActivity)

                        // Save necessary details in SharedPrefs
                        localDB.userID = userDetails.id
                        localDB.userCookie = userDetails.cookie
                        localDB.userEmail = userDetails.email
                        localDB.username = userDetails.username
                        localDB.userDisplayName = userDetails.display_name
                        localDB.address1 = ""
                        localDB.address2 = ""
                        localDB.userPhone = ""
                        localDB.userPicture = ""
                        localDB.company = ""
                        localDB.city = ""
                        localDB.state = ""
                        localDB.userFirstName = userDetails.firstName
                        localDB.userLastName = userDetails.lastName

                        if (userDetails.picture != null && userDetails.picture.data != null)
                            if (!TextUtils.isEmpty(userDetails.picture.data.url))
                                localDB.userPicture = userDetails.picture.data.url

                        if (userDetails.billing != null && userDetails.billing.phone != null)
                            if (!TextUtils.isEmpty(userDetails.billing.phone))
                                localDB.userPhone = userDetails.billing.phone

                        if (userDetails.shipping != null && userDetails.shipping.address1 != null)
                            if (!TextUtils.isEmpty(userDetails.shipping.address1))
                                localDB.address1 = userDetails.shipping.address1

                        if (userDetails.shipping != null && userDetails.shipping.address2 != null)
                            if (!TextUtils.isEmpty(userDetails.shipping.address2))
                                localDB.address2 = userDetails.shipping.address2

                        if (userDetails.shipping != null && userDetails.shipping.company != null)
                            if (!TextUtils.isEmpty(userDetails.shipping.company))
                                localDB.company = userDetails.shipping.company

                        if (userDetails.shipping != null && userDetails.shipping.city != null)
                            if (!TextUtils.isEmpty(userDetails.shipping.city))
                                localDB.city = userDetails.shipping.city

                        if (userDetails.shipping != null && userDetails.shipping.state != null)
                            if (!TextUtils.isEmpty(userDetails.shipping.state))
                                localDB.state = userDetails.shipping.state

                        localDB.hasLoginAuth = true

                        setResult(Activity.RESULT_OK)
                        finish()

                    } else if ("ok".equals(response.body()!!.status, ignoreCase = true)) {
                        if (response.body()!!.msg != null)
                            Snackbar.make(loginBtn, response.body()!!.msg, Snackbar.LENGTH_SHORT).show()
                    } else {
                        if (response.body()!!.error != null)
                            Snackbar.make(loginBtn, response.body()!!.error, Snackbar.LENGTH_SHORT).show()
                    }

                } else {
                    val converter : Converter<ResponseBody, UserData> =
                        APIClient.retrofit.responseBodyConverter(UserData::class.java, arrayOfNulls<Annotation>(0))
                    val userData: UserData
                    userData = try {
                        converter.convert(response.errorBody()!!)
                    } catch (e: IOException) {
                        UserData()
                    }

                    Toast.makeText(this@LoginActivity, "Error : " + userData.error, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserData>, t: Throwable) {
                hideProgressDialog()
                Toast.makeText(this@LoginActivity, "NetworkCallFailure : $t", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun validateLogin(): Boolean {
        return if (!ValidateInputs.isValidName(user_email.text.toString().trim { it <= ' ' })) {
            user_email.error = getString(R.string.invalid_username)
            false
        } else if (!ValidateInputs.isValidPassword(user_password.text.toString().trim { it <= ' ' })) {
            user_password.error = getString(R.string.invalid_password)
            false
        } else {
            true
        }
    }

    //*********** Proceed Forgot Password Request ********//

    private fun processForgotPassword(email: String) {

        showProgressDialog()

        val call = APIClient.getInstance()
            .processForgotPassword(
                "cool",
                email
            )

        call.enqueue(object : Callback<UserData> {
            override fun onResponse(call: Call<UserData>, response: Response<UserData>) {

                hideProgressDialog()

                if (response.isSuccessful) {
                    // Show the Response Message
                    if (response.body()!!.msg != null) {
                        Snackbar.make(parentView, response.body()!!.msg, Snackbar.LENGTH_LONG).show()
                    } else if (response.body()!!.error != null) {
                        Snackbar.make(parentView, response.body()!!.error, Snackbar.LENGTH_LONG).show()
                    }

                } else {
                    val converter : Converter<ResponseBody, ErrorResponse> = APIClient.retrofit.responseBodyConverter(ErrorResponse::class.java, arrayOfNulls(0))
                    var error: ErrorResponse
                    try {
                        error = converter.convert(response.errorBody()!!)
                    } catch (e: IOException) {
                        error = ErrorResponse()
                    }

                    Toast.makeText(this@LoginActivity, "Error : " + error.getError(), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserData>, t: Throwable) {
                hideProgressDialog()
                Toast.makeText(this@LoginActivity, "NetworkCallFailure : $t", Toast.LENGTH_LONG).show()
            }
        })
    }
}
