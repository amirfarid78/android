package com.coheser.app.activitesfragments.location

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng

class AddressPlacesModel() : Parcelable {

    @JvmField
    var placeId: String? = ""
    @JvmField
    var title: String? = ""
    @JvmField
    var address: String? = ""
    @JvmField
    var latLng: LatLng? = null
    @JvmField
    var street: String? = null
    @JvmField
    var streetNumber: String? = null
    @JvmField
    var state: String? = null
    @JvmField
    var country: String? = null
    @JvmField
    var cityName: String? = null
    @JvmField
    var zipCode: String? = null
    @JvmField
    var label: String? = null
    @JvmField
    var lat = 0.0
    @JvmField
    var lng = 0.0
    @JvmField
    var location_string: String? = null

    constructor(parcel: Parcel) : this() {
        placeId = parcel.readString()
        title = parcel.readString()
        address = parcel.readString()
        latLng = parcel.readParcelable(LatLng::class.java.classLoader)
        lat = parcel.readDouble()
        lng = parcel.readDouble()


        street = parcel.readString()
        streetNumber = parcel.readString()
        state = parcel.readString()
        country = parcel.readString()
        cityName = parcel.readString()
        zipCode = parcel.readString()
        label = parcel.readString()
        location_string = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(placeId)
        parcel.writeString(title)
        parcel.writeString(address)
        parcel.writeParcelable(latLng, flags)
        parcel.writeDouble(lat)
        parcel.writeDouble(lng)


        parcel.writeString(street)
        parcel.writeString(streetNumber)
        parcel.writeString(state)
        parcel.writeString(country)
        parcel.writeString(cityName)
        parcel.writeString(zipCode)
        parcel.writeString(label)
        parcel.writeString(location_string)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AddressPlacesModel> {
        override fun createFromParcel(parcel: Parcel): AddressPlacesModel {
            return AddressPlacesModel(parcel)
        }

        override fun newArray(size: Int): Array<AddressPlacesModel?> {
            return arrayOfNulls(size)
        }
    }

}
