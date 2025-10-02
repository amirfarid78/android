package com.coheser.app.activitesfragments.livestreaming.model

import java.io.Serializable

class ContactsDataModel : Serializable {
    var username: String? = null
    var uid: String? = null
    var email: String? = null
    var userId: String? = null
    var picture: String? = null
    @JvmField
    var firstName: String? = null
    @JvmField
    var lastName: String? = null
    @JvmField
    var verified: Int? = null
    var online: Boolean? = null
    @JvmField
    var isexits: Boolean? = null
    @JvmField
    var imagecolor: Int = 0
}
