package com.coheser.app.activitesfragments.shoping.models

import android.os.Parcel
import android.os.Parcelable

 class CategoryModel(
     var id: String? = "",
     var title: String? = "",
     var image: String? = "",
     var created: String? = "",
     var parent_id: String? = "",
     var list: ArrayList<CategoryModel> = ArrayList()
 ) : Parcelable {



    constructor(parcel: Parcel) : this() {
        id = parcel.readString() ?:""
        title = parcel.readString() ?: ""
        image = parcel.readString() ?: ""
        created = parcel.readString() ?: ""
        parent_id = parcel.readString() ?: ""
        list= parcel.readArrayList(CategoryModel::class.java.classLoader) as ArrayList<CategoryModel>
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(image)
        parcel.writeString(created)
        parcel.writeString(parent_id)
        parcel.writeList(list)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CategoryModel> {
        override fun createFromParcel(parcel: Parcel): CategoryModel {
            return CategoryModel(parcel)
        }

        override fun newArray(size: Int): Array<CategoryModel?> {
            return arrayOfNulls(size)
        }
    }


}
