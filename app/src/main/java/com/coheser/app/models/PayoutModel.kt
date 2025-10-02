package com.coheser.app.models

data class PayoutModel(
    val created: String,
    val id: String,
    val primary: String,
    val type: String,
    val user_id: String,
    val value: String
)