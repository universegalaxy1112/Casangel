package com.itikarus.miska


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.itikarus.miska.base.BaseActivity
import com.itikarus.miska.library.utils.ValidateInputs
import com.itikarus.miska.models.user_model.Nonce
import com.itikarus.miska.models.user_model.UserData
import com.itikarus.miska.network.APIClient
import kotlinx.android.synthetic.main.activity_signup.*
import kotlinx.android.synthetic.main.activity_signup.address_1
import kotlinx.android.synthetic.main.activity_signup.address_2
import kotlinx.android.synthetic.main.activity_signup.city
import kotlinx.android.synthetic.main.activity_signup.phone
import kotlinx.android.synthetic.main.activity_signup.signupBtn
import kotlinx.android.synthetic.main.activity_signup.signup_loginText
import kotlinx.android.synthetic.main.activity_signup.state
import kotlinx.android.synthetic.main.activity_signup.user_email
import kotlinx.android.synthetic.main.activity_signup.user_firstname
import kotlinx.android.synthetic.main.activity_signup.user_identificationId
import kotlinx.android.synthetic.main.activity_signup.user_lastname
import kotlinx.android.synthetic.main.activity_signup.user_password
import kotlinx.android.synthetic.main.activity_signup.username
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import java.io.IOException
import java.util.LinkedHashMap

class SignupActivity : BaseActivity() {

    private var registerNonce = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

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

        iv_btnBack.setOnClickListener {
            finish()
        }
    }

    private fun gotoLoginPage() {
        val loginIntent = Intent(this, LoginActivity::class.java)
        startActivityForResult(loginIntent, 1005)
    }

    private fun processRegistration() {

        showProgressDialog(getString(R.string.wait))

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

                hideProgressDialog()

                // Check if the Response is successful
                if (response.isSuccessful) {

                    if ("ok".equals(response.body()!!.status, ignoreCase = true)) {

                        gotoLoginPage()

                    } else if ("error".equals(response.body()!!.status, ignoreCase = true)) {
                        Toast.makeText(
                            this@SignupActivity,
                            response.body()!!.error,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@SignupActivity,
                            getString(R.string.unexpected_response),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } else {
                    val converter: Converter<ResponseBody, UserData> =
                        APIClient.retrofit.responseBodyConverter(
                            UserData::class.java, arrayOfNulls(0)
                        )
                    var userData: UserData
                    try {
                        userData = converter.convert(response.errorBody()!!)
                    } catch (e: IOException) {
                        userData = UserData()
                    }

                    Toast.makeText(
                        this@SignupActivity,
                        "Error : " + userData.error,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<UserData>, t: Throwable) {
                hideProgressDialog()
                Toast.makeText(this@SignupActivity, "NetworkCallFailure : $t", Toast.LENGTH_LONG)
                    .show()
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
                        Toast.makeText(this@SignupActivity, "Nonce is Empty", Toast.LENGTH_SHORT)
                            .show()
                    }

                } else {
                    Toast.makeText(this@SignupActivity, "Nonce is Empty", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Nonce>, t: Throwable) {
                Toast.makeText(this@SignupActivity, "NetworkCallFailure : $t", Toast.LENGTH_LONG)
                    .show()
            }
        })
    }

    private fun validateForm(): Boolean {
        if (!ValidateInputs.isValidName(user_firstname.text.toString().trim { it <= ' ' })) {
            user_firstname.error = getString(R.string.invalid_first_name)
            return false
        } else if (!ValidateInputs.isValidName(user_lastname.text.toString().trim { it <= ' ' })) {
            user_lastname.error = getString(R.string.invalid_last_name)
            return false
        } else if (!ValidateInputs.isValidInput(username.text.toString().trim { it <= ' ' })) {
            username.error = getString(R.string.invalid_username)
            return false
        } else if (!ValidateInputs.isValidEmail(user_email.text.toString().trim { it <= ' ' })) {
            user_email.error = getString(R.string.invalid_email)
            return false
        } else if (!ValidateInputs.isValidPassword(
                user_password.text.toString().trim { it <= ' ' })
        ) {
            user_password.error = getString(R.string.invalid_password)
            return false
        } else if (!ValidateInputs.isValidInput(
                user_identificationId.text.toString().trim { it <= ' ' })
        ) {
            user_identificationId.error = getString(R.string.required_field)
            return false
        }
//        else if (!ValidateInputs.isValidInput(address_1.text.toString().trim { it <= ' ' })) {
//            address_1.error = getString(R.string.required_field)
//            return false
//        }
//        else if (!ValidateInputs.isValidInput(city.text.toString().trim { it <= ' ' })) {
//            city.error = getString(R.string.required_field)
//            return false
//        }
        else if (!ValidateInputs.isValidInput(phone.text.toString().trim { it <= ' ' })) {
            phone.error = getString(R.string.required_field)
            return false
        } else {
            return true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1005 && resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }
}
