package com.example.example

import com.google.gson.annotations.SerializedName


data class WithdrawRequest(

    @SerializedName("id") var id: Int? = null,
    @SerializedName("user_id") var userId: Int? = null,
    @SerializedName("amount") var amount: Double? = null,
    @SerializedName("status") var status: Int? = null,
    @SerializedName("updated") var updated: String? = null,
    @SerializedName("coin") var coin: Int? = null,
    @SerializedName("email") var email: String? = null,
    @SerializedName("created") var created: String? = null

)