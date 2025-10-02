package com.coheser.app.activitesfragments.livestreaming.model

import android.os.Parcel
import android.os.Parcelable

class CameraRequestModel() : Parcelable {
    @JvmField
    var requestState: String? = null

    constructor(parcel: Parcel) : this() {
        requestState = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(requestState)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CameraRequestModel> {
        override fun createFromParcel(parcel: Parcel): CameraRequestModel {
            return CameraRequestModel(parcel)
        }

        override fun newArray(size: Int): Array<CameraRequestModel?> {
            return arrayOfNulls(size)
        }
    }
}
