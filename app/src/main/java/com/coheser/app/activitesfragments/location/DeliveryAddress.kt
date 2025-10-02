package com.coheser.app.activitesfragments.location

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

class DeliveryAddress() : Parcelable {

    @JvmField
    @SerializedName("id")
    var id: String = ""

    @SerializedName("user_id")
    var user_id: String = ""

    @JvmField
    @SerializedName("lat")
    var lat: String = ""

    @JvmField
    @SerializedName("long")
    var lng: String = ""

    @JvmField
    @SerializedName("city")
    var city: String = ""

    @JvmField
    @SerializedName("state")
    var state: String = ""


    @SerializedName("country_id")
    var country_id: String = ""

    @JvmField
    @SerializedName("zip")
    var zip: String = ""

    @JvmField
    @SerializedName("street")
    var street: String = ""

    @JvmField
    @SerializedName("apartment")
    var apartment: String = ""

    @SerializedName("instructions")
    var instructions: String = ""

    @SerializedName("created")
    var created: String = ""

    @SerializedName("street_num")
    var street_num: String = ""

    @SerializedName("location_string")
    var location_string: String = ""

    @SerializedName("entry_code")
    var entry_code: String = ""

    @SerializedName("building_name")
    var building_name: String = ""

    @SerializedName("dropoff_option")
    var dropoff_option: String = ""

    @SerializedName("label")
    var label: String = ""

    @SerializedName("default")
    var defaultValue: String = ""

    constructor(parcel: Parcel) : this() {
        id = parcel.readString().toString()
        user_id = parcel.readString().toString()
        lat = parcel.readString().toString()
        lng = parcel.readString().toString()
        city = parcel.readString().toString()
        state = parcel.readString().toString()
        country_id = parcel.readString().toString()
        zip = parcel.readString().toString()
        street = parcel.readString().toString()
        apartment = parcel.readString().toString()
        instructions = parcel.readString().toString()
        created = parcel.readString().toString()
        street_num = parcel.readString().toString()
        location_string = parcel.readString().toString()
        entry_code = parcel.readString().toString()
        building_name = parcel.readString().toString()
        dropoff_option = parcel.readString().toString()
        label = parcel.readString().toString()
        defaultValue = parcel.readString().toString()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(user_id)
        dest.writeString(lat)
        dest.writeString(lng)
        dest.writeString(city)
        dest.writeString(state)
        dest.writeString(country_id)
        dest.writeString(zip)
        dest.writeString(street)
        dest.writeString(apartment)
        dest.writeString(instructions)
        dest.writeString(created)
        dest.writeString(street_num)
        dest.writeString(location_string)
        dest.writeString(entry_code)
        dest.writeString(building_name)
        dest.writeString(dropoff_option)
        dest.writeString(label)
        dest.writeString(defaultValue)
    }

    override fun describeContents(): Int {
        return 0
    }



    companion object CREATOR : Parcelable.Creator<DeliveryAddress> {
        override fun createFromParcel(parcel: Parcel): DeliveryAddress {
            return DeliveryAddress(parcel)
        }

        override fun newArray(size: Int): Array<DeliveryAddress?> {
            return arrayOfNulls(size)
        }
    }


}
