package com.coheser.app.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class StreamingViewerViewModel(
    private val context : Context
) :ViewModel() {


}
class StreamingViewerFactory(
    private val context: Context
) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StreamingViewerViewModel::class.java)){
            return StreamingViewerViewModel(context) as T
        }
        throw IllegalArgumentException()
    }
}