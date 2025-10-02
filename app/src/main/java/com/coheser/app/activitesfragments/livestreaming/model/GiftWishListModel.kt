package com.coheser.app.activitesfragments.livestreaming.model

import android.os.Parcel
import android.os.Parcelable

class GiftWishListModel() :Parcelable {
    var giftPrice=""
    var giftImage=""
    var giftName=""
    var id=""
    var totalGiftReceived="0"
    var totalGiftWant=""
    var AllGiftUsers:ArrayList<GiftUsers>?=ArrayList()


    constructor(parcel: Parcel) : this() {
        giftPrice = parcel.readString().toString()
        giftImage = parcel.readString().toString()
        giftName = parcel.readString().toString()
        id = parcel.readString().toString()
        totalGiftReceived = parcel.readString().toString()
        totalGiftWant = parcel.readString().toString()
        AllGiftUsers=parcel.readArrayList(GiftUsers::class.java.classLoader) as ArrayList<GiftUsers>?
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(giftPrice)
        parcel.writeString(giftImage)
        parcel.writeString(giftName)
        parcel.writeString(id)
        parcel.writeString(totalGiftReceived)
        parcel.writeString(totalGiftWant)
        parcel.writeTypedList(AllGiftUsers)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GiftWishListModel> {
        override fun createFromParcel(parcel: Parcel): GiftWishListModel {
            return GiftWishListModel(parcel)
        }

        override fun newArray(size: Int): Array<GiftWishListModel?> {
            return arrayOfNulls(size)
        }
    }
}