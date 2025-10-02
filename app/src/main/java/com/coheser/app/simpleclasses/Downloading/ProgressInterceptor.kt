package com.smusix.app.services

import okhttp3.Interceptor
import okhttp3.Response

class ProgressInterceptor(private val progressCallback: (Long, Long) -> Unit) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())
        val responseBody = originalResponse.body
        return originalResponse.newBuilder()
            .body(responseBody?.let { ProgressResponseBody(it, progressCallback) })
            .build()
    }
}
