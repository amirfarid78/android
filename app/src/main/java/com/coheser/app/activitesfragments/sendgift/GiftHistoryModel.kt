package com.coheser.app.activitesfragments.sendgift

import android.os.Parcel
import android.os.Parcelable
import com.coheser.app.models.UserModel
import com.google.gson.annotations.SerializedName

class GiftHistoryModel() : Parcelable {
    var count: Int=1

    @SerializedName("GiftSend")
    var giftSend = GiftSend()

    @SerializedName("Gift")
    var gift = GiftModel()

    @SerializedName("User")
    var user=UserModel()

    constructor(parcel: Parcel) : this() {
        count = parcel.readInt()
        giftSend = parcel.readParcelable(GiftSend::class.java.classLoader)!!
        gift = parcel.readParcelable(GiftModel::class.java.classLoader)!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(count)
        parcel.writeParcelable(giftSend, flags)
        parcel.writeParcelable(gift, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GiftHistoryModel> {
        override fun createFromParcel(parcel: Parcel): GiftHistoryModel {
            return GiftHistoryModel(parcel)
        }

        override fun newArray(size: Int): Array<GiftHistoryModel?> {
            return arrayOfNulls(size)
        }
    }
}