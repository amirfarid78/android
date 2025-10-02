package com.coheser.app.activitesfragments.shoping.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.coheser.app.models.UserModel

class ProductHistoryModel() : Parcelable {
    var id: String? = ""
    var category_id: String? = ""
    var user_id: String? = ""
    var title: String? = ""
    var taggedName: String? = ""
    var description: String? = ""
    var size: String? = ""
    var price: String? = ""
    var sale_price: String? = ""
    var promote: String? = ""
    var view: String? = ""
    var condition: String? = ""
    var delivery_method: String? = ""
    var meetup_location_lat: String? = ""
    var meetup_location_long: String? = ""
    var meetup_location_string: String? = ""
    var updated: String? = ""
    var created: String? = ""
    var count: Int = 1
    var sold: Float = 0f
    @SerializedName("User")
    var user: UserModel? = null
    @SerializedName("ProductImage")
    var productImage: List<ProductImage?> = emptyList()

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        category_id = parcel.readString()
        user_id = parcel.readString()
        title = parcel.readString()
        taggedName = parcel.readString()
        description = parcel.readString()
        size = parcel.readString()
        price = parcel.readString()
        sale_price = parcel.readString()
        promote = parcel.readString()
        view = parcel.readString()
        condition = parcel.readString()
        delivery_method = parcel.readString()
        meetup_location_lat = parcel.readString()
        meetup_location_long = parcel.readString()
        meetup_location_string = parcel.readString()
        updated = parcel.readString()
        created = parcel.readString()
        count = parcel.readInt()
        sold = parcel.readFloat()
        user = parcel.readParcelable(UserModel::class.java.classLoader)
        productImage = parcel.createTypedArrayList(ProductImage.CREATOR)!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(category_id)
        parcel.writeString(user_id)
        parcel.writeString(title)
        parcel.writeString(taggedName)
        parcel.writeString(description)
        parcel.writeString(size)
        parcel.writeString(price)
        parcel.writeString(sale_price)
        parcel.writeString(promote)
        parcel.writeString(view)
        parcel.writeString(condition)
        parcel.writeString(delivery_method)
        parcel.writeString(meetup_location_lat)
        parcel.writeString(meetup_location_long)
        parcel.writeString(meetup_location_string)
        parcel.writeString(updated)
        parcel.writeString(created)
        parcel.writeInt(count)
        parcel.writeFloat(sold)
        parcel.writeParcelable(user, flags)
        parcel.writeTypedList(productImage)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProductHistoryModel> {
        override fun createFromParcel(parcel: Parcel): ProductHistoryModel {
            return ProductHistoryModel(parcel)
        }

        override fun newArray(size: Int): Array<ProductHistoryModel?> {
            return arrayOfNulls(size)
        }
    }

}
