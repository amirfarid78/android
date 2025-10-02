package com.coheser.app.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

class PrivacySettingModel() : Parcelable {

    @SerializedName("id"              ) var id             : Int?    = null
    @SerializedName("videos_download" ) var videosDownload : Int?    = null
    @SerializedName("direct_message"  ) var directMessage  : String? = null
    @SerializedName("duet"            ) var duet           : String? = null
    @SerializedName("liked_videos"    ) var likedVideos    : String? = null
    @SerializedName("video_comment"   ) var videoComment   : String? = null
    @SerializedName("order_history"   ) var orderHistory   : String? = null

    constructor(parcel: Parcel) : this() {
        id = parcel.readValue(Int::class.java.classLoader) as? Int
        videosDownload = parcel.readValue(Int::class.java.classLoader) as? Int
        directMessage = parcel.readString()
        duet = parcel.readString()
        likedVideos = parcel.readString()
        videoComment = parcel.readString()
        orderHistory = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeValue(videosDownload)
        parcel.writeString(directMessage)
        parcel.writeString(duet)
        parcel.writeString(likedVideos)
        parcel.writeString(videoComment)
        parcel.writeString(orderHistory)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PrivacySettingModel> {
        override fun createFromParcel(parcel: Parcel): PrivacySettingModel {
            return PrivacySettingModel(parcel)
        }

        override fun newArray(size: Int): Array<PrivacySettingModel?> {
            return arrayOfNulls(size)
        }
    }

}
