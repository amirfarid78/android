package com.coheser.app.models

import android.os.Parcel
import android.os.Parcelable
import com.coheser.app.Constants
import com.coheser.app.simpleclasses.Variables
import com.google.gson.annotations.SerializedName

class UserModel : Parcelable {

    @JvmField
    @SerializedName("id")
    var id: String? = null

    @JvmField
    @SerializedName("first_name")
    var first_name: String? = ""

    @JvmField
    @SerializedName("last_name")
    var last_name: String? = ""

    @JvmField
    @SerializedName("gender")
    var gender: String? = ""

    @JvmField
    @SerializedName("bio")
    var bio: String? = ""

    @JvmField
    @SerializedName("website")
    var website: String? = ""

    @JvmField
    @SerializedName("dob")
    var dob: String? = ""

    @JvmField
    @SerializedName("social_id")
    var social_id: String? = ""

    @JvmField
    @SerializedName("email")
    var email: String? = ""

    @JvmField
    @SerializedName("phone")
    var phone: String? = ""

    @SerializedName("password")
    var password: String? = ""

    @JvmField
    @SerializedName("role")
    var role: String? = ""

    @JvmField
    @SerializedName("username")
    var username: String? = ""

    @JvmField
    @SerializedName("social")
    var social: String? = ""

    @JvmField
    @SerializedName("device_token")
    var device_token: String? = ""

    @SerializedName("token")
    var token: String? = ""

    @SerializedName("active")
    var active: Int = 1

    @SerializedName("online")
    var online: Int = 0

    @JvmField
    @SerializedName("verified")
    var verified: Int = 0

    @SerializedName("applyVerification")
    var applyVerification: String? = null

    @SerializedName("referral_code")
    var referral_code: String? = null

    @SerializedName("auth_token")
    var auth_token: String? = null

    @SerializedName("version")
    var version: String? = null

    @SerializedName("device")
    var device: String? = null

    @SerializedName("ip")
    var ip: String? = null

    @SerializedName("city")
    var city: String? = null

    @SerializedName("country")
    var country: String? = null

    @SerializedName("city_id")
    var city_id: String? = null

    @SerializedName("state_id")
    var state_id: String? = null

    @SerializedName("country_id")
    var country_id: String? = null

    @JvmField
    @SerializedName("paypal")
    var paypal: String? = null

    @JvmField
    @SerializedName("reset_wallet_datetime")
    var reset_wallet_datetime: String? = null

    @SerializedName("created")
    var created: String? = null

    @JvmField
    @SerializedName("followers_count")
    var followers_count:  Long = 0

    @JvmField
    @SerializedName("following_count")
    var following_count:  Long = 0


    @JvmField
    @SerializedName("likes_count")
    var likes_count:  Long = 0

    @JvmField
    @SerializedName("video_count")
    var video_count:  Long = 0

    @JvmField
    @SerializedName("notification")
    var notification: String? = null

    @JvmField
    @SerializedName("button")
    var button: String? = null

    @JvmField
    @SerializedName("profile_view")
    var profile_view: String? = null

    @JvmField
    @SerializedName("business")
    var business:Int=0

    @JvmField
    @SerializedName("sold_items_count")
    var sold_items_count:  Long = 0


    @JvmField
    @SerializedName("tagged_products_count")
    var tagged_products_count:  Long = 0

    @JvmField
    @SerializedName("block")
    var block: String = "0"

    @JvmField
    @SerializedName("blockByUser")
    var blockByUser: String? = null

    @JvmField
    @SerializedName("lat")
    var lat = 0.0

    @JvmField
    @SerializedName("long")
    var lng = 0.0

    @JvmField
    @SerializedName("wallet")
    var wallet: Long = 0

    @JvmField
    @SerializedName("profile_visit_count")
    var profile_visit_count: Long = 0

    @JvmField
    @SerializedName("total_all_time_coins")
    var total_all_time_coins: Long = 0

    @JvmField
    @SerializedName("unread_notification")
    var unread_notification: Long = 0

    @JvmField
    var isSelected = false

    @JvmField
    @SerializedName("comission_earned")
    var comission_earned: Double = 0.0

    @JvmField
    @SerializedName("total_balance_usd")
    var total_balance_usd: Double = 0.0

    @SerializedName("profile_pic")
    private var profile_pic= ""

    @SerializedName("profile_gif")
    private var profile_gif= ""

    var intrestsCount:Long =0

    @JvmField
    var storyModel: StoryModel? = null

    @JvmField
    var privacySettingModel: PrivacySettingModel? = null

    @JvmField
    var pushNotificationModel: PushNotificationModel? = null


    constructor()
    protected constructor(`in`: Parcel) {
        id = `in`.readString()
        first_name = `in`.readString()
        last_name = `in`.readString()
        gender = `in`.readString()
        bio = `in`.readString()
        website = `in`.readString()
        dob = `in`.readString()
        social_id = `in`.readString()
        email = `in`.readString()
        phone = `in`.readString()
        password = `in`.readString()
        role = `in`.readString()
        username = `in`.readString()
        social = `in`.readString()
        device_token = `in`.readString()
        token = `in`.readString()
        active = `in`.readInt()
        online = `in`.readInt()
        verified = `in`.readInt()
        applyVerification = `in`.readString()
        referral_code = `in`.readString()
        auth_token = `in`.readString()
        version = `in`.readString()
        device = `in`.readString()
        ip = `in`.readString()
        city = `in`.readString()
        country = `in`.readString()
        city_id = `in`.readString()
        state_id = `in`.readString()
        country_id = `in`.readString()
        paypal = `in`.readString()
        reset_wallet_datetime = `in`.readString()
        created = `in`.readString()
        followers_count = `in`.readLong()
        following_count = `in`.readLong()
        likes_count = `in`.readLong()
        video_count = `in`.readLong()
        notification = `in`.readString()
        button = `in`.readString()
        profile_view = `in`.readString()
        business = `in`.readInt()
        profile_pic = `in`.readString().toString()
        profile_gif = `in`.readString().toString()
        block = `in`.readString().toString()
        blockByUser = `in`.readString()
        lat = `in`.readDouble()
        lng = `in`.readDouble()
        wallet = `in`.readLong()
        profile_visit_count = `in`.readLong()
        total_all_time_coins = `in`.readLong()
        unread_notification = `in`.readLong()
        isSelected = `in`.readByte().toInt() != 0
        comission_earned = `in`.readDouble()
        total_balance_usd = `in`.readDouble()
        intrestsCount = `in`.readLong()
        storyModel = `in`.readParcelable(StoryModel::class.java.classLoader)
        privacySettingModel = `in`.readParcelable(PrivacySettingModel::class.java.classLoader)
        pushNotificationModel = `in`.readParcelable(PushNotificationModel::class.java.classLoader)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(first_name)
        dest.writeString(last_name)
        dest.writeString(gender)
        dest.writeString(bio)
        dest.writeString(website)
        dest.writeString(dob)
        dest.writeString(social_id)
        dest.writeString(email)
        dest.writeString(phone)
        dest.writeString(password)
        dest.writeString(role)
        dest.writeString(username)
        dest.writeString(social)
        dest.writeString(device_token)
        dest.writeString(token)
        dest.writeInt(active)
        dest.writeInt(online)
        dest.writeInt(verified)
        dest.writeString(applyVerification)
        dest.writeString(referral_code)
        dest.writeString(auth_token)
        dest.writeString(version)
        dest.writeString(device)
        dest.writeString(ip)
        dest.writeString(city)
        dest.writeString(country)
        dest.writeString(city_id)
        dest.writeString(state_id)
        dest.writeString(country_id)
        dest.writeString(paypal)
        dest.writeString(reset_wallet_datetime)
        dest.writeString(created)
        dest.writeLong(followers_count)
        dest.writeLong(following_count)
        dest.writeLong(likes_count)
        dest.writeLong(video_count)
        dest.writeString(notification)
        dest.writeString(button)
        dest.writeString(profile_view)
        dest.writeInt(business)
        dest.writeString(profile_pic)
        dest.writeString(profile_gif)
        dest.writeString(block)
        dest.writeString(blockByUser)
        dest.writeDouble(lat)
        dest.writeDouble(lng)
        dest.writeLong(wallet)
        dest.writeLong(profile_visit_count)
        dest.writeLong(total_all_time_coins)
        dest.writeLong(unread_notification)
        dest.writeByte((if (isSelected) 1 else 0).toByte())
        dest.writeDouble(comission_earned)
        dest.writeDouble(total_balance_usd)
        dest.writeLong(intrestsCount)
        dest.writeParcelable(storyModel, flags)
        dest.writeParcelable(privacySettingModel, flags)
        dest.writeParcelable(pushNotificationModel, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun getProfileGif(): String? {
        if (profile_gif==null && !profile_gif!!.contains(Variables.http)) {
            profile_gif = Constants.BASE_URL + profile_gif
        }
        return profile_gif
    }

    fun setProfileGif(profileGif: String?) {
        this.profile_gif = profileGif!!
    }

    fun getProfilePic(): String? {
        if (profile_pic==null || !profile_pic!!.contains(Variables.http)) {
            profile_pic = Constants.BASE_URL + profile_pic
        }
        return profile_pic
    }

    fun setProfilePic(profilePic: String?) {
        this.profile_pic = profilePic!!
    }

    companion object CREATOR : Parcelable.Creator<UserModel> {
        override fun createFromParcel(parcel: Parcel): UserModel {
            return UserModel(parcel)
        }

        override fun newArray(size: Int): Array<UserModel?> {
            return arrayOfNulls(size)
        }
    }

}