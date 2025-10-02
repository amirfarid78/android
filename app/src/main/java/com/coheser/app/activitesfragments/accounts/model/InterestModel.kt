package com.coheser.activitiesfragments.accounts.model

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

class InterestModel() : Parcelable, Serializable {
    var secTitle: String = ""
    var userIntrest: ArrayList<Interest> = ArrayList()

    constructor(parcel: Parcel) : this() {
        secTitle = parcel.readString() ?: ""
        userIntrest = parcel.createTypedArrayList(Interest.CREATOR) ?: ArrayList()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(secTitle)
        parcel.writeList(userIntrest)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<InterestModel> {
        override fun createFromParcel(parcel: Parcel): InterestModel {
            return InterestModel(parcel)
        }

        override fun newArray(size: Int): Array<InterestModel?> {
            return arrayOfNulls(size)
        }
    }
}
