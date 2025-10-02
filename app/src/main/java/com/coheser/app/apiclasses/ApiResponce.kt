package com.coheser.app.apiclasses

sealed class ApiResponce<T>(
    val data: T? = null,
    val isRequestError:Boolean=false,
    val message: String? = null
) {
    class Success<T>(data: T) : ApiResponce<T>(data)
    class Error<T>(message: String,isRequestError: Boolean, data: T? = null) : ApiResponce<T>(data,isRequestError, message)
    class Loading<T> : ApiResponce<T>()
}