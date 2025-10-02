package com.coheser.app.models

import java.io.Serializable

data class UserRegisterModel(
    var firebaseUID: String = "",
    var socailId: String = "",
    var socailType: String = "",
    var fname: String = "",
    var lname: String = "",
    var email: String = "",
    var dateOfBirth: String = "",
    var phoneNo: String = "",
    var password: String = "",
    var username: String = "",
    var googleTokon: String = "",
    var picture: String = "",
    var referalCode: String = "",
    var lat: String = "",
    var lng: String = "",
    var apt_suite: String = "",
    var company_name: String = "",
    var store_name: String = "",
    var store_type: String = ""
) : Serializable