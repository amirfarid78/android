package com.coheser.app.activitesfragments.shoping.models

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

class AddProductModel(
     var id: String = "",
     var condition: String = "",
     var title: String = "",
     var description: String = "",
     var dealMethod: String = "",
     var locationString: String = "",
     var lat: String? = null,
     var lng: String? = null,
     var price: String = "",
     var imagesList: ArrayList<Uri?> = ArrayList(),
     var categoryModel: CategoryModel? = null
 ) : Parcelable {

     constructor(parcel: Parcel) : this() {
         id = parcel.readString().toString()
         condition = parcel.readString().toString()
         title = parcel.readString().toString()
         description = parcel.readString().toString()
         dealMethod = parcel.readString().toString()
         locationString = parcel.readString().toString()
         lat = parcel.readString().toString()
         lng = parcel.readString().toString()
         price = parcel.readString().toString()
         imagesList = parcel.readArrayList(Uri::class.java.classLoader)!! as ArrayList<Uri?>
         categoryModel = parcel.readParcelable(CategoryModel::class.java.classLoader)
     }

     override fun writeToParcel(parcel: Parcel, flags: Int) {
         parcel.writeString(id)
         parcel.writeString(condition)
         parcel.writeString(title)
         parcel.writeString(description)
         parcel.writeString(dealMethod)
         parcel.writeString(locationString)
         parcel.writeString(lat)
         parcel.writeString(lng)
         parcel.writeString(price)
         parcel.writeList(imagesList)
         parcel.writeParcelable(categoryModel, flags)
     }

     override fun describeContents(): Int {
         return 0
     }

     companion object CREATOR : Parcelable.Creator<AddProductModel> {
         override fun createFromParcel(parcel: Parcel): AddProductModel {
             return AddProductModel(parcel)
         }

         override fun newArray(size: Int): Array<AddProductModel?> {
             return arrayOfNulls(size)
         }
     }

 }