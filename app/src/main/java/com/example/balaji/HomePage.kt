package com.example.balaji

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.example.balaji.databinding.ActivityHomePageBinding
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.journeyapps.barcodescanner.CaptureActivity
import cz.msebera.android.httpclient.Header
import org.json.JSONArray
import org.json.JSONObject

class HomePage : AppCompatActivity(), View.OnClickListener {
    private val CAMERA_REQUEST_CODE = 101
    private lateinit var binding: ActivityHomePageBinding
    private var modelNumber = ""
    private var retailPrice = ""
    private var stockGroupname = ""
    private var stockItemName = ""
    private var tapCount: Int = 0
    private var wholesalePrice = ""

    private fun changeValueOnDoubleTap() {
        binding.tvRetailPrice.text = wholesalePrice
        Handler().postDelayed({
            binding.tvRetailPrice.text = retailPrice
        }, 5000)
    }

    private fun hideBlurr() {
        binding.overlayFrame.visibility = View.GONE
    }

    private fun makeBlurr() {
        binding.overlayFrame.visibility = View.VISIBLE
    }

    private fun requestCameraPermission() {
        val activity: Activity = this
        ActivityCompat.requestPermissions(activity, arrayOf("android.permission.CAMERA"), CAMERA_REQUEST_CODE)
    }

    private fun scanNewQrCode() {
        if (ActivityCompat.checkSelfPermission(this, "android.permission.CAMERA") == 0) {
            val intentIntegrator = IntentIntegrator(this)
            intentIntegrator.setCaptureActivity(CaptureActivity::class.java)
            intentIntegrator.setOrientationLocked(true)
            intentIntegrator.setPrompt("Scan a QR code")
            intentIntegrator.initiateScan()
        } else {
            requestCameraPermission()
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.ivScanCode -> scanNewQrCode()
            R.id.tvRetailPrice -> {
                tapCount++
                if (tapCount == 2) {
                    changeValueOnDoubleTap()
                    tapCount = 0
                }
            }
            R.id.tvClear -> binding.svMainScrollView.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()
        binding.ivScanCode.setOnClickListener(this)
        binding.tvRetailPrice.setOnClickListener(this)
        binding.tvClear.setOnClickListener(this)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == 0) {
                scanNewQrCode()
            } else {
                Toast.makeText(this, "Camera permission is required to scan QR codes", Toast.LENGTH_SHORT).show()
            }
        }
    }
// Add this to your HomePage class

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
            } else {
                // Here, you can call getScannedDetails(result.contents) to handle the scanned data
                getScannedDetails(result.contents)
            }
        }
    }

    fun getScannedDetails(modelNumber: String) {
        this.modelNumber = modelNumber
        makeBlurr()
        val jSONObject = JSONObject().apply {
            put("apiKey", PrefUtils.getApiKey(this@HomePage))
            put("modelNumbers", JSONArray().put(modelNumber))
        }
        val asyncRequestHandler = AsyncRequestHandler.getInstance()
        asyncRequestHandler.makeRequest(jSONObject, Universal.generateApiLink(Universal.getItemDetails), this, object : RequestListener {
            override fun onFailure(
                paramInt: Int,
                paramArrayOfHeader: Array<Header?>?,
                paramJSONObject: JSONObject?
            ) {
                hideBlurr()
            }

            override fun onFailureThrowable(
                paramInt: Int,
                paramArrayOfHeader: Array<Header?>?,
                paramString: String?,
                paramThrowable: Throwable?
            ) {
                hideBlurr()
            }

            override fun onSuccess(
                paramInt: Int,
                paramArrayOfHeader: Array<Header?>?,
                response: JSONObject?
            ) {
                binding.svMainScrollView.visibility = View.VISIBLE
                hideBlurr()
                parseApiResponse(response)
            }
        })
    }

    private fun parseApiResponse(jsonObject: JSONObject?) {
        if (jsonObject != null && jsonObject.has("items")) {
            val itemsArray = jsonObject.getJSONArray("items")
            if (itemsArray.length() > 0) {
                val item = itemsArray.getJSONObject(0)
                stockItemName = item.optString("stockItem", "")
                stockGroupname = item.optString("stockGroup", "")
                modelNumber = item.optString("modelNumber", "")
                retailPrice = item.optString("retailRate", "")
                wholesalePrice = item.optString("wholesaleRate", "")
                setUpUiData()
            }
        }
    }

    private fun setUpUiData() {
        binding.tvStockItemValue.text = stockItemName
        binding.tvStockGroupValue.text = stockGroupname
        binding.tvModelNumberValue.text = modelNumber
        binding.tvRetailPrice.text = retailPrice
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
    }
}

