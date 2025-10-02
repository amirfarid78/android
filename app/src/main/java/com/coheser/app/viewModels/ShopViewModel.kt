package com.coheser.app.viewModels

import androidx.lifecycle.LiveData

import androidx.lifecycle.MutableLiveData

import androidx.lifecycle.ViewModel


class ShopViewModel : ViewModel() {
    private val deletedItemId = MutableLiveData<String>()
    fun getDeletedItemPosition(): LiveData<String> {
        return deletedItemId
    }

    fun setDeletedItemPosition(position: String) {
        deletedItemId.value = position
    }
}

