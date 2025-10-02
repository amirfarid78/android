package com.coheser.app.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

class PushNotificationModel() : Parcelable {

    @SerializedName("id"              ) var id             : Int? = null
    @SerializedName("likes"           ) var likes          : Int? = null
    @SerializedName("comments"        ) var comments       : Int? = null
    @SerializedName("new_followers"   ) var newFollowers   : Int? = null
    @SerializedName("mentions"        ) var mentions       : Int? = null
    @SerializedName("direct_messages" ) var directMessages : Int? = null
    @SerializedName("video_updates"   ) var videoUpdates   : Int? = null

    constructor(parcel: Parcel) : this() {
        id = parcel.readValue(Int::class.java.classLoader) as? Int
        likes = parcel.readValue(Int::class.java.classLoader) as? Int
        comments = parcel.readValue(Int::class.java.classLoader) as? Int
        newFollowers = parcel.readValue(Int::class.java.classLoader) as? Int
        mentions = parcel.readValue(Int::class.java.classLoader) as? Int
        directMessages = parcel.readValue(Int::class.java.classLoader) as? Int
        videoUpdates = parcel.readValue(Int::class.java.classLoader) as? Int
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeValue(likes)
        parcel.writeValue(comments)
        parcel.writeValue(newFollowers)
        parcel.writeValue(mentions)
        parcel.writeValue(directMessages)
        parcel.writeValue(videoUpdates)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PushNotificationModel> {
        override fun createFromParcel(parcel: Parcel): PushNotificationModel {
            return PushNotificationModel(parcel)
        }

        override fun newArray(size: Int): Array<PushNotificationModel?> {
            return arrayOfNulls(size)
        }
    }

}