package com.coheser.app.activitesfragments.spaces.models

import android.os.Parcel
import android.os.Parcelable
import com.coheser.app.models.UserModel

class HomeUserModel() : Parcelable {
    @JvmField
    var userModel: UserModel? = null
    @JvmField
    var userRoleType: String? = null
    @JvmField
    var mice: String?=null
    @JvmField
    var riseHand: String?=null
    @JvmField
    var online: String? =null

    constructor(parcel: Parcel) : this() {
        userModel = parcel.readParcelable(UserModel::class.java.classLoader)
        userRoleType = parcel.readString()
        mice = parcel.readString()
        riseHand = parcel.readString()
        online = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(userModel, flags)
        parcel.writeString(userRoleType)
        parcel.writeString(mice)
        parcel.writeString(riseHand)
        parcel.writeString(online)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HomeUserModel> {
        override fun createFromParcel(parcel: Parcel): HomeUserModel {
            return HomeUserModel(parcel)
        }

        override fun newArray(size: Int): Array<HomeUserModel?> {
            return arrayOfNulls(size)
        }
    }

}
