package com.coheser.app.models

import android.os.Parcel
import android.os.Parcelable
import com.coheser.app.activitesfragments.location.AddressPlacesModel

class UploadVideoModel() : Parcelable {

    @JvmField
    var userId: String? = null

    @JvmField
    var soundId: String? = null

    @JvmField
    var description: String? = null

    @JvmField
    var privacyPolicy: String? = null
    @JvmField
    var allowComments: String? = null
    @JvmField
    var allowDuet: String? = ""
    @JvmField
    var hashtagsJson: String? = ""
    @JvmField
    var usersJson: String? = ""
    @JvmField
    var product_json: String? = ""
    @JvmField
    var videoId: String? = null
    @JvmField
    var duet: String? = null
    @JvmField
    var videoType: String? = null
    @JvmField
    var width: String? = ""
    @JvmField
    var height: String? = ""


    var tagStoreId=""

    var default_thumbnail=""

    var user_thumbnail=""

    var placesModel: AddressPlacesModel? = null


    constructor(parcel: Parcel) : this() {
        userId = parcel.readString()
        soundId = parcel.readString()
        description = parcel.readString()
        privacyPolicy = parcel.readString()
        allowComments = parcel.readString()
        allowDuet = parcel.readString()
        hashtagsJson = parcel.readString()
        usersJson = parcel.readString()
        product_json = parcel.readString()
        videoId = parcel.readString()
        duet = parcel.readString()
        videoType = parcel.readString()
        width = parcel.readString()
        height = parcel.readString()
        tagStoreId = parcel.readString().toString()
        default_thumbnail = parcel.readString().toString()
        user_thumbnail = parcel.readString().toString()
        placesModel = parcel.readParcelable(AddressPlacesModel::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userId)
        parcel.writeString(soundId)
        parcel.writeString(description)
        parcel.writeString(privacyPolicy)
        parcel.writeString(allowComments)
        parcel.writeString(allowDuet)
        parcel.writeString(hashtagsJson)
        parcel.writeString(usersJson)
        parcel.writeString(product_json)
        parcel.writeString(videoId)
        parcel.writeString(duet)
        parcel.writeString(videoType)
        parcel.writeString(width)
        parcel.writeString(height)
        parcel.writeString(tagStoreId)
        parcel.writeString(default_thumbnail)
        parcel.writeString(user_thumbnail)
        parcel.writeParcelable(placesModel, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UploadVideoModel> {
        override fun createFromParcel(parcel: Parcel): UploadVideoModel {
            return UploadVideoModel(parcel)
        }

        override fun newArray(size: Int): Array<UploadVideoModel?> {
            return arrayOfNulls(size)
        }
    }
}