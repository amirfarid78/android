package com.coheser.app.activitesfragments.livestreaming.model

import android.os.Parcel
import android.os.Parcelable

class SetGoalStream() :Parcelable {
    var goalAmount: String? = null
    var goalDescription: String? = null
    var userId: String? = null
    var userName: String? = null
    var userPicture: String? = null

    constructor(parcel: Parcel) : this() {
        goalAmount = parcel.readString()
        goalDescription = parcel.readString()
        userId = parcel.readString()
        userName = parcel.readString()
        userPicture = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(goalAmount)
        parcel.writeString(goalDescription)
        parcel.writeString(userId)
        parcel.writeString(userName)
        parcel.writeString(userPicture)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SetGoalStream> {
        override fun createFromParcel(parcel: Parcel): SetGoalStream {
            return SetGoalStream(parcel)
        }

        override fun newArray(size: Int): Array<SetGoalStream?> {
            return arrayOfNulls(size)
        }
    }

}