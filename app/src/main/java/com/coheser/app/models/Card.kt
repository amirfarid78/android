package com.coheser.app.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

class Card() : Parcelable {


    @SerializedName("id" )
    var id              : Int?    = null
    @SerializedName("card"              )
    var card            : String? = null
    @SerializedName("user_id"           )
    var userId          : Int?    = null
    @SerializedName("last_4"            )
    var last4           : Int?    = null
    @SerializedName("brand"             )
    var brand           : String? = null
    @SerializedName("exp_month"         )
    var expMonth        : Int?    = null
    @SerializedName("exp_year"          )
    var expYear         : Int?    = null
    @SerializedName("card_id"           )
    var cardId          : String? = null
    @SerializedName("payment_method_id" )
    var paymentMethodId : String? = null
    @SerializedName("default"           )
    var default         : Int?    = null
    @SerializedName("mealme"            )
    var mealme          : Int?    = null
    @SerializedName("email"             )
    var email           : String? = null
    @SerializedName("created"           )
    var created         : String? = null

    constructor(parcel: Parcel) : this() {
        id = parcel.readValue(Int::class.java.classLoader) as? Int
        card = parcel.readString()
        userId = parcel.readValue(Int::class.java.classLoader) as? Int
        last4 = parcel.readValue(Int::class.java.classLoader) as? Int
        brand = parcel.readString()
        expMonth = parcel.readValue(Int::class.java.classLoader) as? Int
        expYear = parcel.readValue(Int::class.java.classLoader) as? Int
        cardId = parcel.readString()
        paymentMethodId = parcel.readString()
        default = parcel.readValue(Int::class.java.classLoader) as? Int
        mealme = parcel.readValue(Int::class.java.classLoader) as? Int
        email = parcel.readString()
        created = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(card)
        parcel.writeValue(userId)
        parcel.writeValue(last4)
        parcel.writeString(brand)
        parcel.writeValue(expMonth)
        parcel.writeValue(expYear)
        parcel.writeString(cardId)
        parcel.writeString(paymentMethodId)
        parcel.writeValue(default)
        parcel.writeValue(mealme)
        parcel.writeString(email)
        parcel.writeString(created)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Card> {
        override fun createFromParcel(parcel: Parcel): Card {
            return Card(parcel)
        }

        override fun newArray(size: Int): Array<Card?> {
            return arrayOfNulls(size)
        }
    }


}
