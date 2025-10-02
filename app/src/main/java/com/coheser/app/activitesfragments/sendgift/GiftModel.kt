package com.coheser.app.activitesfragments.sendgift

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

class GiftModel() : Parcelable {
    @SerializedName("coin")
    var coin: Int? = null

    @SerializedName("created")
    var created: String?= null

    @SerializedName("featured")
    var featured: Int?= null

    @SerializedName("icon")
    var icon: String?= null

    @SerializedName("id")
    var id: Int?= null

    @SerializedName("image")
    var image: String?= null

    @SerializedName("position")
    var position: String?= null

    @SerializedName("time")
    var time: Int?= null

    @SerializedName("title")
    var title: String?= null

    @SerializedName("type")
    var type: String?= null

    @JvmField
    var isSelected: Boolean = false
    @JvmField
    var count: Int = 1

    constructor(parcel: Parcel) : this() {
        coin = parcel.readValue(Int::class.java.classLoader) as? Int
        created = parcel.readString()
        featured = parcel.readValue(Int::class.java.classLoader) as? Int
        icon = parcel.readString()
        id = parcel.readValue(Int::class.java.classLoader) as? Int
        image = parcel.readString()
        position = parcel.readString()
        time = parcel.readValue(Int::class.java.classLoader) as? Int
        title = parcel.readString()
        type = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(coin)
        parcel.writeString(created)
        parcel.writeValue(featured)
        parcel.writeString(icon)
        parcel.writeValue(id)
        parcel.writeString(image)
        parcel.writeString(position)
        parcel.writeValue(time)
        parcel.writeString(title)
        parcel.writeString(type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GiftModel> {
        override fun createFromParcel(parcel: Parcel): GiftModel {
            return GiftModel(parcel)
        }

        override fun newArray(size: Int): Array<GiftModel?> {
            return arrayOfNulls(size)
        }
    }




}
