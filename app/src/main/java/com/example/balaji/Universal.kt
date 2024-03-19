package com.example.balaji

import android.util.Patterns
import kotlin.jvm.internal.Intrinsics
object Universal {

         val getItemDetails: String = "items/details"
         val ipLink: String = "http://43.227.184.17/"
          val sendOtp = "auth/send-otp"
          val validateOtp = "auth/verify-otp"

        @JvmStatic
        fun generateApiLink(paramString: String): String {
            Intrinsics.checkNotNullParameter(paramString, "suffix")
            return ipLink + paramString
        }

        @JvmStatic
        fun isValidEmail(paramCharSequence: CharSequence?): Boolean {
            Intrinsics.checkNotNullParameter(paramCharSequence, "email")
            return Patterns.EMAIL_ADDRESS.matcher(paramCharSequence).matches()
        }
}