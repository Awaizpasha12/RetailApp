package com.example.balaji
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.balaji.databinding.ActivityMainBinding
import cz.msebera.android.httpclient.Header
import org.json.JSONObject
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityMainBinding
    private var countDownTimer: CountDownTimer? = null
    private var otpEntered = ""

    private fun collectOtp(): String {
        return binding.etOtp1.text.toString() +
                binding.etOtp2.text.toString() +
                binding.etOtp3.text.toString() +
                binding.etOtp4.text.toString()
    }

    private fun hideBlurr() {
        binding.overlayFrame.visibility = View.GONE
    }

    private fun makeBlurr(paramString: String) {
        binding.overlayFrame.visibility = View.VISIBLE
    }

    private fun setupOtpInputs() {
        otpEntered = ""
        val genericTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    if (it.length == 1) {
                        when {
                            binding.etOtp1.isFocused -> {
                                binding.etOtp2.requestFocus()
                                otpEntered = collectOtp()
                            }
                            binding.etOtp2.isFocused -> {
                                binding.etOtp3.requestFocus()
                                otpEntered = collectOtp()
                            }
                            binding.etOtp3.isFocused -> {
                                binding.etOtp4.requestFocus()
                                otpEntered = collectOtp()
                            }
                            binding.etOtp4.isFocused -> otpEntered = collectOtp()
                        }
                    }
                }
            }
        }
        binding.etOtp1.addTextChangedListener(genericTextWatcher)
        binding.etOtp2.addTextChangedListener(genericTextWatcher)
        binding.etOtp3.addTextChangedListener(genericTextWatcher)
        binding.etOtp4.addTextChangedListener(genericTextWatcher)
    }


    override fun onClick(view: View?) {
        view?.let {
            val id = it.id
            when (id) {
                R.id.loginButton -> validateOtp()
                R.id.getOtpButton -> sendOTPToMail()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()
        setupOtpInputs()
        if (PrefUtils.isLoggedIn(applicationContext)) {
            openHomePage()
        }
        binding.getOtpButton.setOnClickListener(this)
        binding.loginButton.setOnClickListener(this)
        setButtonEnabled(binding.loginButton, false)
        setButtonEnabled(binding.getOtpButton, true)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

    private fun openHomePage() {
        startActivity(Intent(this, HomePage::class.java))
        finish()
    }

    private fun sendOTPToMail() {
        val email = binding.emailEditText.text.toString()
        if (email.isNotEmpty() && Universal.isValidEmail(email)) {
            makeBlurr("Generating OTP")
            setButtonEnabled(binding.getOtpButton, false)
            val jsonObject = JSONObject().apply { put("email", email) }
            AsyncRequestHandler.getInstance().makeRequest(
                jsonObject,
                Universal.generateApiLink(Universal.sendOtp),
                applicationContext,
                object : RequestListener {
                    override fun onSuccess(statusCode: Int, headers: Array<Header?>?, response: JSONObject?) {
                        hideBlurr()
                        displayToastMessage("OTP sent successfully")
                        setButtonEnabled(binding.loginButton, true)
                    }

                    override fun onFailure(statusCode: Int, headers: Array<Header?>?, response: JSONObject?) {
                        hideBlurr()
                        displayToastMessage("Error in sending please retry")
                        setButtonEnabled(binding.getOtpButton, true)
                    }

                    override fun onFailureThrowable(statusCode: Int, headers: Array<Header?>?, response: String?, throwable: Throwable?) {
                        hideBlurr()
                        displayToastMessage("Error in sending please retry")
                        setButtonEnabled(binding.getOtpButton, true)
                    }




                }
            )
        } else {
            displayToastMessage("Enter valid emailId")
            setButtonEnabled(binding.getOtpButton, true)
        }
    }

    private fun validateOtp() {
        if (otpEntered.isNotEmpty() && otpEntered.length == 4) {
            setButtonEnabled(binding.loginButton, false)
            val jsonObject = JSONObject().apply {
                put("email", binding.emailEditText.text.toString())
                put("otp", otpEntered)
            }
            AsyncRequestHandler.getInstance().makeRequest(
                jsonObject,
                Universal.generateApiLink(Universal.validateOtp),
                applicationContext,
                object : RequestListener {
                    override fun onFailure(
                        paramInt: Int,
                        paramArrayOfHeader: Array<Header?>?,
                        response: JSONObject?
                    ) {
                        hideBlurr()
                        val error = response?.optString("error") ?: ""
                        displayToastMessage(error)
                        setButtonEnabled(binding.loginButton, true)
                    }

                    override fun onFailureThrowable(
                        paramInt: Int,
                        paramArrayOfHeader: Array<Header?>?,
                        paramString: String?,
                        paramThrowable: Throwable?
                    ) {
                        hideBlurr()
                        displayToastMessage("Error in validating please retry")
                        setButtonEnabled(binding.loginButton, true)
                    }

                    override fun onSuccess(
                        paramInt: Int,
                        paramArrayOfHeader: Array<Header?>?,
                        response: JSONObject?
                    ) {
                        hideBlurr()
                        displayToastMessage("OTP Validated successfully")
                        val apikey = response?.optString("apikey") ?: ""
                        PrefUtils.setLoggedIn(applicationContext, true, apikey)
                        openHomePage()
                    }
                }
            )
        } else {
            displayToastMessage("Enter 4 digit otp")
        }
    }

    private fun setButtonEnabled(button: Button, enabled: Boolean) {
        if (enabled) {
            button.setBackgroundResource(R.drawable.button_background)
            button.isEnabled = true
            button.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            button.setBackgroundResource(R.drawable.disabled_button_background)
            button.isEnabled = false
        }
    }

    private fun setupToolbar() {
//        val toolbar: Toolbar = findViewById(R.id.toolbar)
//        setSupportActionBar(toolbar)
//        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun displayToastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//
//class MainActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//    }
//}