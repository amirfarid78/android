package com.coheser.app.activitesfragments.livestreaming.model

import android.os.Parcel
import android.os.Parcelable

class PkInvitation() : Parcelable {
    @JvmField
    var action: String? = null

    @JvmField
    var pkStreamingId: String? = null

    @JvmField
    var receiverStreamingId: String? = null

    @JvmField
    var receiverId: String? = null

    @JvmField
    var receiverName: String? = null
    var receiverPic: String? = null

    @JvmField
    var senderId: String? = null

    @JvmField
    var senderName: String? = null
    var senderPic: String? = null

    @JvmField
    var senderStreamingId: String? = null
    var pkStreamingTime: String? = null

    @JvmField
    var senderCoins = 0

    @JvmField
    var receiverCoins = 0

    @JvmField
    var timeStamp: Long = 0

    constructor(parcel: Parcel) : this() {
        action = parcel.readString()
        pkStreamingId = parcel.readString()
        receiverStreamingId = parcel.readString()
        receiverId = parcel.readString()
        receiverName = parcel.readString()
        receiverPic = parcel.readString()
        senderId = parcel.readString()
        senderName = parcel.readString()
        senderPic = parcel.readString()
        senderStreamingId = parcel.readString()
        pkStreamingTime = parcel.readString()
        senderCoins = parcel.readInt()
        receiverCoins = parcel.readInt()
        timeStamp = parcel.readLong()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(action)
        parcel.writeString(pkStreamingId)
        parcel.writeString(receiverStreamingId)
        parcel.writeString(receiverId)
        parcel.writeString(receiverName)
        parcel.writeString(receiverPic)
        parcel.writeString(senderId)
        parcel.writeString(senderName)
        parcel.writeString(senderPic)
        parcel.writeString(senderStreamingId)
        parcel.writeString(pkStreamingTime)
        parcel.writeInt(senderCoins)
        parcel.writeInt(receiverCoins)
        parcel.writeLong(timeStamp)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PkInvitation> {
        override fun createFromParcel(parcel: Parcel): PkInvitation {
            return PkInvitation(parcel)
        }

        override fun newArray(size: Int): Array<PkInvitation?> {
            return arrayOfNulls(size)
        }
    }
}
