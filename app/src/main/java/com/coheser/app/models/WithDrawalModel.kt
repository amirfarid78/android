package com.example.example

import com.google.gson.annotations.SerializedName


data class WithDrawalModel(

    @SerializedName("WithdrawRequest") var WithdrawRequest: WithdrawRequest? = WithdrawRequest(),

    )