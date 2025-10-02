package com.coheser.app.activitesfragments.spaces.utils.RoomManager

import com.coheser.app.activitesfragments.spaces.models.HomeUserModel
import java.io.Serializable

class RoomJoinStatusModel : Serializable {
    @JvmField
    var roomId: String? = null
    @JvmField
    var myModel: HomeUserModel? = null
    @JvmField
    var userList: ArrayList<HomeUserModel>? = null
}
