package com.coheser.app.apiclasses;

import android.content.Context;

import com.coheser.app.Constants;
import com.coheser.app.simpleclasses.Functions;
import com.coheser.app.simpleclasses.Variables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit;

    public static Retrofit getRetrofitInstance(Context context) {


        if (retrofit == null) {

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

            httpClient.connectTimeout(30, TimeUnit.MINUTES)
                    .readTimeout(30, TimeUnit.MINUTES)
                    .writeTimeout(30, TimeUnit.MINUTES)
                    .build();
            httpClient.addInterceptor(chain -> {
                Request.Builder requestBuilder = chain.request().newBuilder();
               // requestBuilder.header("Content-Type", "application/json");
                requestBuilder.header("API-KEY", Constants.API_KEY);
                if(Functions.isStringHasValue(Functions.getSharedPreference(context).getString(Variables.AUTH_TOKEN, "null"))) {
                    requestBuilder.header("Auth-Token", Functions.getSharedPreference(context).getString(Variables.AUTH_TOKEN, "null"));
                }
                return chain.proceed(requestBuilder.build());
            });

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(Constants.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }
}
