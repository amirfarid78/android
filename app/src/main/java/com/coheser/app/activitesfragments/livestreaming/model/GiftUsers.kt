package com.coheser.app.activitesfragments.livestreaming.model

import android.os.Parcel
import android.os.Parcelable

class GiftUsers() :Parcelable {
    var count = 0
    var userId = ""
    var userName = ""
    var userPicture = ""

    constructor(parcel: Parcel) : this() {
        count = parcel.readInt()
        userId = parcel.readString().toString()
        userName = parcel.readString().toString()
        userPicture = parcel.readString().toString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(count)
        parcel.writeString(userId)
        parcel.writeString(userName)
        parcel.writeString(userPicture)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GiftUsers> {
        override fun createFromParcel(parcel: Parcel): GiftUsers {
            return GiftUsers(parcel)
        }

        override fun newArray(size: Int): Array<GiftUsers?> {
            return arrayOfNulls(size)
        }
    }
}