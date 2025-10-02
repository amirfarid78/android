package com.coheser.app.activitesfragments.livestreaming.model

import android.os.Parcel
import android.os.Parcelable

class LiveCoinsModel() : Parcelable {

    var giftPic: String? = ""
    @JvmField
    var sendedCoins: Double = 0.0
    @JvmField
    var userId: String? = ""
    @JvmField
    var userName: String? = ""
    @JvmField
    var userPicture: String? = ""

    constructor(parcel: Parcel) : this() {
        giftPic = parcel.readString()
        sendedCoins = parcel.readDouble()
        userId = parcel.readString()
        userName = parcel.readString()
        userPicture = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(giftPic)
        parcel.writeDouble(sendedCoins)
        parcel.writeString(userId)
        parcel.writeString(userName)
        parcel.writeString(userPicture)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LiveCoinsModel> {
        override fun createFromParcel(parcel: Parcel): LiveCoinsModel {
            return LiveCoinsModel(parcel)
        }

        override fun newArray(size: Int): Array<LiveCoinsModel?> {
            return arrayOfNulls(size)
        }
    }
}
