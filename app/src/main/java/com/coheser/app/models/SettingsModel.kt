package com.coheser.app.models

import android.os.Parcel
import android.os.Parcelable

class SettingsModel(
    val created: String = "",
    val id :String = "",
    val type: String = "",
    val value: String = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
        created = parcel.readString() ?: "",
        id = parcel.readString() ?:"",
        type = parcel.readString() ?: "",
        value = parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(created)
        parcel.writeString(id)
        parcel.writeString(type)
        parcel.writeString(value)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SettingsModel> {
        override fun createFromParcel(parcel: Parcel): SettingsModel {
            return SettingsModel(parcel)
        }

        override fun newArray(size: Int): Array<SettingsModel?> {
            return arrayOfNulls(size)
        }
    }
}
