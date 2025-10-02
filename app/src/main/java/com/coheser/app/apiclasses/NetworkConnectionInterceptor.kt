package com.coheser.app.apiclasses

import android.content.Context
import okhttp3.Interceptor
import kotlin.Throws
import android.net.ConnectivityManager
import com.coheser.app.Constants
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables
import okhttp3.Response
import java.io.IOException

class NetworkConnectionInterceptor(private val mContext: Context) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!isConnected) {
            throw NoConnectivityException()
        }
            val builder = chain.request().newBuilder()

          //  builder.header("Content-Type", "application/json")
            builder.header("Api-Key", Constants.API_KEY)
            Functions.getSharedPreference(mContext).getString(Variables.AUTH_TOKEN, "null")
                ?.let { authToken ->
                    if (Functions.isStringHasValue(authToken)) {
                        builder.header("Auth-Token", authToken)
                    }
                }
            return chain.proceed(builder.build())
    }

    private val isConnected: Boolean
        get() {
            val connectivityManager = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val netInfo = connectivityManager.activeNetworkInfo
            return netInfo != null && netInfo.isConnected
        }

    inner class NoConnectivityException : IOException() {
        override val message: String
            get() = "No Internet Connection"
    }


}