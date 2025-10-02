package com.coheser.app.models

import android.os.Parcel
import android.os.Parcelable

data class StripeModel(
    var id: String,
    var url: String,
    var success_url: String,
    var cancel_url: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun describeContents(): Int {
        return 0 // Always return 0 unless you have special file descriptors
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(url)
        dest.writeString(success_url)
        dest.writeString(cancel_url)
    }

    companion object CREATOR : Parcelable.Creator<StripeModel> {
        override fun createFromParcel(parcel: Parcel): StripeModel {
            return StripeModel(parcel)
        }

        override fun newArray(size: Int): Array<StripeModel?> {
            return arrayOfNulls(size)
        }
    }
}
