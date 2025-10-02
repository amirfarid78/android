package com.coheser.activitiesfragments.accounts.model

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

class Interest() : Parcelable, Serializable {
    var id: String = ""
    var title: String = ""
    var selected:String ="0"
    var selection:Boolean = false

    constructor(parcel: Parcel) : this() {
        id = parcel.readString() ?: ""
        title = parcel.readString() ?: ""
        selected = parcel.readString() ?: "0"
        selection = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(selected)
        parcel.writeByte(if (selection) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Interest> {
        override fun createFromParcel(parcel: Parcel): Interest {
            return Interest(parcel)
        }

        override fun newArray(size: Int): Array<Interest?> {
            return arrayOfNulls(size)
        }
    }


}
