package com.coheser.app.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.coheser.app.Constants
import com.coheser.app.simpleclasses.Variables

class StoryModel() : Parcelable {

    @JvmField
    @SerializedName("id")
    var id: String? = null
    @JvmField
    var username: String? = ""
    private var profile_pic= ""


    var videoList: ArrayList<StoryVideoModel> = ArrayList()

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        username = parcel.readString()
        profile_pic = parcel.readString().toString()
        videoList = parcel.readArrayList(StoryVideoModel::class.java.classLoader) as ArrayList<StoryVideoModel>

    }


    fun getProfilePic(): String? {
        if (!profile_pic!!.contains(Variables.http)) {
            profile_pic = Constants.BASE_URL + profile_pic
        }
        return profile_pic
    }

    fun setProfilePic(profilePic: String?) {
        this.profile_pic = profilePic!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(username)
        parcel.writeString(profile_pic)
        parcel.writeList(videoList)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<StoryModel> {
        override fun createFromParcel(parcel: Parcel): StoryModel {
            return StoryModel(parcel)
        }

        override fun newArray(size: Int): Array<StoryModel?> {
            return arrayOfNulls(size)
        }
    }
}
