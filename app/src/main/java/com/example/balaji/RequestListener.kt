package com.example.balaji

import cz.msebera.android.httpclient.Header
import org.json.JSONObject

interface RequestListener {
    fun onFailure(paramInt: Int, paramArrayOfHeader: Array<Header?>?, paramJSONObject: JSONObject?)
    fun onFailureThrowable(
        paramInt: Int,
        paramArrayOfHeader: Array<Header?>?,
        paramString: String?,
        paramThrowable: Throwable?
    )

    fun onSuccess(paramInt: Int, paramArrayOfHeader: Array<Header?>?, paramJSONObject: JSONObject?)
}