package com.coheser.app.models

import android.os.Parcel
import android.os.Parcelable

class StreamJoinModel() : Parcelable {
    @JvmField
    var userId: String? = null

    @JvmField
    var userName: String? = null

    @JvmField
    var followersCount: String? = null

    @JvmField
    var followingCount: String? = null

    var userPic: String? = null

    constructor(parcel: Parcel) : this() {
        userId = parcel.readString()
        userName = parcel.readString()
        followersCount = parcel.readString()
        followingCount = parcel.readString()
        userPic = parcel.readString()
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userId)
        parcel.writeString(userName)
        parcel.writeString(followersCount)
        parcel.writeString(followingCount)
        parcel.writeString(userPic)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<StreamJoinModel> {
        override fun createFromParcel(parcel: Parcel): StreamJoinModel {
            return StreamJoinModel(parcel)
        }

        override fun newArray(size: Int): Array<StreamJoinModel?> {
            return arrayOfNulls(size)
        }
    }
}
