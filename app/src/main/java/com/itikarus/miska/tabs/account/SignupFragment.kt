package com.itikarus.miska.tabs.account

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Toast
import com.itikarus.miska.R
import com.itikarus.miska.library.utils.ValidateInputs
import com.itikarus.miska.models.user_model.Nonce
import com.itikarus.miska.models.user_model.UserData
import com.itikarus.miska.network.APIClient
import kotlinx.android.synthetic.main.fragment_signup.view.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import java.io.IOException
import java.util.LinkedHashMap

class SignupFragment (context: Context) : LinearLayout(context) {

    private var registerNonce = ""

    var parentActivity : AccountActivity?= null

    init {
        parentActivity = context as AccountActivity
        parentActivity?.setTitle("Signup")
        parentActivity?.drawerMenuSetting()
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        layoutInflater.inflate(R.layout.fragment_signup, this, true)
        initView()
    }

    private fun initView(){
        getRegisterationNonce()

        signup_loginText.setOnClickListener {
            gotoLoginPage()
        }

        signupBtn.setOnClickListener {
            val isValidData = validateForm()
            if (isValidData) {
                if (!TextUtils.isEmpty(registerNonce))
                    processRegistration()

            }
        }
    }

    private fun gotoLoginPage(){
        parentActivity?.addFragment(LoginFragment(context))
    }

    private fun processRegistration() {

        parentActivity?.showProgressDialog("Registering...")

        val call = APIClient.getInstance()
            .processRegistration(
                "cool",
                user_firstname.text.toString().trim { it <= ' ' },
                user_lastname.text.toString().trim { it <= ' ' },
                username.text.toString().trim { it <= ' ' },
                user_email.text.toString().trim { it <= ' ' },
                user_password.text.toString().trim { it <= ' ' },
                user_identificationId.text.toString(),
                address_1.text.toString(),
                //address_2.text.toString(),
                city.text.toString(),
                //state.text.toString(),
                phone.text.toString(),
                registerNonce
            )

        call.enqueue(object : Callback<UserData> {
            override fun onResponse(call: Call<UserData>, response: retrofit2.Response<UserData>) {

                parentActivity?.hideProgressDialog()

                // Check if the Response is successful
                if (response.isSuccessful) {

                    if ("ok".equals(response.body()!!.status, ignoreCase = true)) {

                        gotoLoginPage()

                    } else if ("error".equals(response.body()!!.status, ignoreCase = true)) {
                        Toast.makeText(context, response.body()!!.error, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, context.getString(R.string.unexpected_response), Toast.LENGTH_SHORT).show()
                    }

                } else {
                    val converter : Converter<ResponseBody, UserData> = APIClient.retrofit.responseBodyConverter(UserData::class.java, arrayOfNulls(0))
                    var userData: UserData
                    try {
                        userData = converter.convert(response.errorBody()!!)
                    } catch (e: IOException) {
                        userData = UserData()
                    }

                    Toast.makeText(context, "Error : " + userData.error, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UserData>, t: Throwable) {
                parentActivity?.hideProgressDialog()
                Toast.makeText(context, "NetworkCallFailure : $t", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun getRegisterationNonce() {

        val params = LinkedHashMap<String, String>()
        params["controller"] = "AndroidAppUsers"
        params["method"] = "android_register"

        val call = APIClient.getInstance()
            .getNonce(
                params
            )

        call.enqueue(object : Callback<Nonce> {
            override fun onResponse(call: Call<Nonce>, response: retrofit2.Response<Nonce>) {

                // Check if the Response is successful
                if (response.isSuccessful) {

                    if (!TextUtils.isEmpty(response.body()!!.nonce)) {
                        registerNonce = response.body()!!.nonce
                    } else {
                        Toast.makeText(context, "Nonce is Empty", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Toast.makeText(context, "Nonce is Empty", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Nonce>, t: Throwable) {
                Toast.makeText(context, "NetworkCallFailure : $t", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun validateForm(): Boolean {
        if (!ValidateInputs.isValidName(user_firstname.text.toString().trim { it <= ' ' })) {
            user_firstname.error = context.getString(R.string.invalid_first_name)
            return false
        } else if (!ValidateInputs.isValidName(user_lastname.text.toString().trim { it <= ' ' })) {
            user_lastname.error = context.getString(R.string.invalid_last_name)
            return false
        }
//        else if (!ValidateInputs.isValidInput(username.text.toString().trim { it <= ' ' })) {
//            username.error = context.getString(R.string.invalid_username)
//            return false
//        }
        else if (!ValidateInputs.isValidInput(user_identificationId.text.toString().trim { it <= ' ' })) {
            user_identificationId.error = context.getString(R.string.required_field)
            return false
        }

        else if (!ValidateInputs.isValidEmail(user_email.text.toString().trim { it <= ' ' })) {
            user_email.error = context.getString(R.string.invalid_email)
            return false
        }

        else if (!ValidateInputs.isValidPassword(user_password.text.toString().trim { it <= ' ' })) {
            user_password.error = context.getString(R.string.invalid_password)
            return false
        }
//        else if (!ValidateInputs.isValidInput(address_1.text.toString().trim { it <= ' ' })) {
//            address_1.error = context.getString(R.string.required_field)
//            return false
//        }
//        else if (!ValidateInputs.isValidInput(city.text.toString().trim { it <= ' ' })) {
//            city.error = context.getString(R.string.required_field)
//            return false
//        }
        else if (!ValidateInputs.isValidInput(phone.text.toString().trim { it <= ' ' })) {
            phone.error = context.getString(R.string.required_field)
            return false
        }
        else {
            return true
        }
    }
}
