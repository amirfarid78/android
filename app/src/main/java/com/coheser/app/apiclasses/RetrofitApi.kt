package com.coheser.app.apiclasses

import com.google.gson.GsonBuilder
import com.coheser.app.Constants
import com.coheser.app.simpleclasses.Functions.getSharedPreference
import com.coheser.app.simpleclasses.Functions.isStringHasValue
import com.coheser.app.simpleclasses.TicTicApp
import com.coheser.app.simpleclasses.Variables
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitApi {

    lateinit var retrofit: Retrofit
   fun getRetrofitInstance(): Retrofit {


        if (!::retrofit.isInitialized) {

            val httpClient =OkHttpClient.Builder()
//            val httpClient: OkHttpClient.Builder = OkHttpClient.Builder()
//                .addInterceptor(NetworkConnectionInterceptor(TicTicApp.appLevelContext!!))

            httpClient.connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .build()

            httpClient.addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val requestBuilder = chain.request().newBuilder()
                 requestBuilder.header("Content-Type", "application/json");
                requestBuilder.header("API-KEY", Constants.API_KEY)
                if (isStringHasValue(
                        getSharedPreference(
                            TicTicApp.appLevelContext!!
                        ).getString(Variables.AUTH_TOKEN, "null")
                    )
                ) {
                    requestBuilder.header("Auth-Token",
                        getSharedPreference(TicTicApp.appLevelContext!!).getString(Variables.AUTH_TOKEN, "null")!!
                    )
                }
                chain.proceed(requestBuilder.build())
            })

            val gson = GsonBuilder()
                .setLenient()
                .create()
            retrofit = Retrofit.Builder().baseUrl(ApiLinks.API_BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient.build())
                .build()
        }

        return retrofit
    }


}