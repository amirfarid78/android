package com.coheser.app.activitesfragments.livestreaming.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by yurr on 3/5/2019.
 */
class LiveCommentModel() : Parcelable {
    @JvmField
    var key: String? = ""
    @JvmField
    var userId: String? = ""
    @JvmField
    var userName: String? = ""
    @JvmField
    var userPicture: String? = ""
    @JvmField
    var comment: String? = ""
    @JvmField
    var commentTime: String? = ""


    @JvmField
    var giftCount: String? = ""

    @JvmField
    var giftPic: String? = ""

    @JvmField
    var giftName: String? = ""

    @JvmField
    var giftIcon: String? = ""

    @JvmField
    var type: String? = ""
    @JvmField
    var giftId: String? = ""

    @JvmField
    var time: String? = "0"

    constructor(parcel: Parcel) : this() {
        key = parcel.readString().toString()
        userId = parcel.readString().toString()
        userName = parcel.readString().toString()
        userPicture = parcel.readString().toString()
        comment = parcel.readString().toString()
        commentTime = parcel.readString().toString()
        giftCount = parcel.readString().toString()
        giftPic = parcel.readString().toString()
        giftName = parcel.readString().toString()
        type = parcel.readString().toString()
        giftId = parcel.readString().toString()
        giftIcon = parcel.readString().toString()
        time = parcel.readString().toString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(key)
        parcel.writeString(userId)
        parcel.writeString(userName)
        parcel.writeString(userPicture)
        parcel.writeString(comment)
        parcel.writeString(commentTime)
        parcel.writeString(giftCount)
        parcel.writeString(giftPic)
        parcel.writeString(giftName)
        parcel.writeString(type)
        parcel.writeString(giftId)
        parcel.writeString(giftIcon)
        parcel.writeString(time)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LiveCommentModel> {
        override fun createFromParcel(parcel: Parcel): LiveCommentModel {
            return LiveCommentModel(parcel)
        }

        override fun newArray(size: Int): Array<LiveCommentModel?> {
            return arrayOfNulls(size)
        }
    }


}
