package com.coheser.app.activitesfragments.spaces.models

import com.coheser.app.models.UserModel
import java.io.Serializable

class UserSuggestionModel : Serializable {
    @JvmField
    var userModel: UserModel? = null
}
