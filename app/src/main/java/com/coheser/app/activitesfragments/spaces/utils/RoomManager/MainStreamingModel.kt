package com.coheser.app.activitesfragments.spaces.utils.RoomManager

import android.os.Parcel
import android.os.Parcelable
import com.coheser.app.activitesfragments.spaces.models.HomeUserModel

class MainStreamingModel() : Parcelable {
    @JvmField
    var model: StreamModel? = null
    @JvmField
    var userList: ArrayList<HomeUserModel>? = null

    constructor(parcel: Parcel) : this() {
        model = parcel.readParcelable(StreamModel::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(model, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MainStreamingModel> {
        override fun createFromParcel(parcel: Parcel): MainStreamingModel {
            return MainStreamingModel(parcel)
        }

        override fun newArray(size: Int): Array<MainStreamingModel?> {
            return arrayOfNulls(size)
        }
    }


}
