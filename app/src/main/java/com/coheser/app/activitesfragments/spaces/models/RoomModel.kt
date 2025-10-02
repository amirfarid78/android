package com.coheser.app.activitesfragments.spaces.models

import java.io.Serializable

class RoomModel : Serializable {
    @JvmField
    var id: String? = null
    @JvmField
    var adminId: String? = null
    @JvmField
    var title: String? = null
    @JvmField
    var privacyType: String? = null
    var created: String? = null
    @JvmField
    var userList: ArrayList<HomeUserModel>? = null

    @JvmField
    var topicModels: ArrayList<TopicModel>? = null
}
