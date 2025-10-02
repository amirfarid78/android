package com.coheser.app.activitesfragments.spaces.utils.RoomManager

import android.os.Parcel
import android.os.Parcelable

class StreamModel() : Parcelable {
    var id: String? = null
    var adminId: String? = null
    var title: String? = null
    var privacyType: String? = null
    var created: String? = null
    var riseHandRule: String="1"
    var riseHandCount: String="0"

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        adminId = parcel.readString()
        title = parcel.readString()
        privacyType = parcel.readString()
        created = parcel.readString()
        riseHandRule = parcel.readString().toString()
        riseHandCount = parcel.readString().toString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(adminId)
        parcel.writeString(title)
        parcel.writeString(privacyType)
        parcel.writeString(created)
        parcel.writeString(riseHandRule)
        parcel.writeString(riseHandCount)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<StreamModel> {
        override fun createFromParcel(parcel: Parcel): StreamModel {
            return StreamModel(parcel)
        }

        override fun newArray(size: Int): Array<StreamModel?> {
            return arrayOfNulls(size)
        }
    }


}
