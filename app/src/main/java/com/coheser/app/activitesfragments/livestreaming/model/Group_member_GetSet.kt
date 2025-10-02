package com.coheser.app.activitesfragments.livestreaming.model

import android.os.Parcel
import android.os.Parcelable

class Group_member_GetSet() : Parcelable {
    var user_id: String? = null
    var user_name: String? = null
    var user_pic: String? = null
    var verified: String? = null
    var role: String? = "user"

    constructor(parcel: Parcel) : this() {
        user_id = parcel.readString()
        user_name = parcel.readString()
        user_pic = parcel.readString()
        verified = parcel.readString()
        role = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(user_id)
        parcel.writeString(user_name)
        parcel.writeString(user_pic)
        parcel.writeString(verified)
        parcel.writeString(role)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Group_member_GetSet> {
        override fun createFromParcel(parcel: Parcel): Group_member_GetSet {
            return Group_member_GetSet(parcel)
        }

        override fun newArray(size: Int): Array<Group_member_GetSet?> {
            return arrayOfNulls(size)
        }
    }

}
