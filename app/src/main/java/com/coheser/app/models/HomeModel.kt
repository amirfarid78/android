package com.coheser.app.models

import android.os.Parcel
import android.os.Parcelable
import com.coheser.app.Constants
import com.coheser.app.activitesfragments.shoping.models.ProductModel
import com.coheser.app.simpleclasses.Variables

/**
 * Created by qboxus on 2/18/2019.
 */
class HomeModel() : Parcelable {
    @JvmField
    var user_id: String? = ""

    var userModel: UserModel? = null

    @JvmField
    var sound_id: String? = ""

    @JvmField
    var sound_name: String? = ""

    @JvmField
    var soundFav: String? = ""

    @JvmField
    var sound_url_acc: String? = ""

    @JvmField
    var sound_url_mp3: String? = ""

    @JvmField
    var sound_pic: String? = ""

    @JvmField
    var video_id: String? = ""

    @JvmField
    var video_description: String? = ""

    @JvmField
    var created_date: String? = ""

    @JvmField
    var promote: String? = ""

    @JvmField
    var video_user_id: String? = ""

    @JvmField
    var video_url: String? = ""

    @JvmField
    var gif: String? = ""


    @JvmField
    var default_thumbnail: String? = ""

    @JvmField
    var user_thumbnail: String? = ""

    @JvmField
    var privacy_type: String? = ""

    @JvmField
    var allow_comments: String? = ""

    @JvmField
    var allow_duet: String? = ""

    @JvmField
    var liked: String? = ""

    @JvmField
    var like_count: String? = ""

    @JvmField
    var video_comment_count: String? = ""

    @JvmField
    var views: String? = ""

    @JvmField
    var duet_video_id: String? = ""

    @JvmField
    var duet_username: String? = ""

    @JvmField
    var favourite_count: String? = ""

    @JvmField
    var share: String? = ""

    @JvmField
    var duration: String? = "0"

    @JvmField
    var pin: String? = "0"

    //for playlist
    @JvmField
    var playlistVideoId: String? = null

    @JvmField
    var playlistId: String? = "0"

    @JvmField
    var playlistName: String? = ""

    //for video block
    @JvmField
    var block: String? = ""

    @JvmField
    var aws_label: String? = ""

    //repost
    @JvmField
    var repost_video_id: String? = ""

    @JvmField
    var repost_user_id: String? = ""

    @JvmField
    var repost: String? = ""

    @JvmField
    var lat: String? = ""

    @JvmField
    var lng: String? = ""

    @JvmField
    var location_string: String? = ""

    @JvmField
    var locationId: String? = ""

    @JvmField
    var placeId: String? = ""

    @JvmField
    var location_name: String? = ""

    @JvmField
    var location_image: String? = ""


    @JvmField
    var storeName: String? = ""

    @JvmField
    var productName: String? = ""


    // additional param
    @JvmField
    var favourite: String? = null

    @JvmField
    var apply_privacy_model: PrivacySettingModel? = null

    @JvmField
    var apply_push_notification_model: PushNotificationModel? = null

    @JvmField
    var tagProductList: ArrayList<ProductModel>? = ArrayList()


    @JvmField
    var promotionModel: PromotionModel? = null



    constructor(parcel: Parcel) : this() {
        user_id = parcel.readString()

        userModel = parcel.readParcelable(UserModel::class.java.classLoader)
        sound_id = parcel.readString()
        sound_name = parcel.readString()
        soundFav = parcel.readString()
        sound_url_acc = parcel.readString()
        sound_url_mp3 = parcel.readString()
        sound_pic = parcel.readString()
        video_id = parcel.readString()
        video_description = parcel.readString()
        created_date = parcel.readString()
        promote = parcel.readString()
        video_user_id = parcel.readString()
        video_url = parcel.readString()
        gif = parcel.readString()
        default_thumbnail = parcel.readString()
        user_thumbnail = parcel.readString()
        privacy_type = parcel.readString()
        allow_comments = parcel.readString()
        allow_duet = parcel.readString()
        liked = parcel.readString()
        like_count = parcel.readString()
        video_comment_count = parcel.readString()
        views = parcel.readString()
        duet_video_id = parcel.readString()
        duet_username = parcel.readString()
        favourite_count = parcel.readString()
        share = parcel.readString()
        duration = parcel.readString()
        pin = parcel.readString()
        playlistVideoId = parcel.readString()
        playlistId = parcel.readString()
        playlistName = parcel.readString()
        block = parcel.readString()
        aws_label = parcel.readString()
        repost_video_id = parcel.readString()
        repost_user_id = parcel.readString()
        repost = parcel.readString()
        lat = parcel.readString()
        lng = parcel.readString()
        location_string = parcel.readString()
        locationId = parcel.readString()
        placeId = parcel.readString()
        location_name = parcel.readString()
        location_image = parcel.readString()
        storeName = parcel.readString()
        productName = parcel.readString()
        favourite = parcel.readString()
        apply_privacy_model =
            parcel.readParcelable(PrivacySettingModel::class.java.classLoader)
        apply_push_notification_model =
            parcel.readParcelable(PushNotificationModel::class.java.classLoader)
        tagProductList = parcel.readArrayList(ProductModel::class.java.classLoader) as ArrayList<ProductModel>
        promotionModel =
            parcel.readParcelable(com.coheser.app.models.PromotionModel::class.java.classLoader)

    }


    fun getVideoDescription(): String? {
        return video_description
    }

    fun getSound_url_acc(): String? {
        if (!sound_url_acc!!.contains(Variables.http)) {
            sound_url_acc = Constants.BASE_URL + sound_url_acc
        }
        return sound_url_acc
    }

    fun setSound_url_acc(sound_url_acc: String?) {
        this.sound_url_acc = sound_url_acc
    }

    fun getSound_url_mp3(): String? {
        if (!sound_url_mp3!!.contains(Variables.http)) {
            sound_url_mp3 = Constants.BASE_URL + sound_url_mp3
        }
        return sound_url_mp3
    }

    fun setSound_url_mp3(sound_url_mp3: String?) {
        this.sound_url_mp3 = sound_url_mp3
    }


    fun getSound_pic(): String? {
        if (!sound_pic!!.contains(Variables.http)) {
            sound_pic = Constants.BASE_URL + sound_pic
        }
        return sound_pic
    }

    fun setSound_pic(sound_pic: String?) {
        this.sound_pic = sound_pic
    }

    fun getVideo_url(): String? {
        if (!video_url!!.contains(Variables.http)) {
            video_url = Constants.BASE_URL + video_url
        }
        return video_url
    }

    fun setVideo_url(video_url: String?) {
        this.video_url = video_url
    }

    fun getGif(): String? {
        if (!gif!!.contains(Variables.http)) {
            gif = Constants.BASE_URL + gif
        }
        return gif
    }

    fun setGif(gif: String?) {
        this.gif = gif
    }

    fun getThum(): String? {
        if (!default_thumbnail!!.contains(Variables.http)) {
            default_thumbnail = Constants.BASE_URL + default_thumbnail
        }
        return default_thumbnail
    }


    fun setThum(thum: String?) {
        this.default_thumbnail = thum
    }


    fun getLat(): String? {
        return lat
    }

    fun setLat(lat: String) {
        this.lat = lat
    }

    fun getLng(): String? {
        return lng
    }

    fun setLng(lng: String) {
        this.lng = lng
    }

    fun getLocation_string(): String? {
        return location_string
    }

    fun setLocation_string(location_string: String) {
        this.location_string = location_string
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(user_id)
        parcel.writeParcelable(userModel, flags)
        parcel.writeString(sound_id)
        parcel.writeString(sound_name)
        parcel.writeString(soundFav)
        parcel.writeString(sound_url_acc)
        parcel.writeString(sound_url_mp3)
        parcel.writeString(sound_pic)
        parcel.writeString(video_id)
        parcel.writeString(video_description)
        parcel.writeString(created_date)
        parcel.writeString(promote)
        parcel.writeString(video_user_id)
        parcel.writeString(video_url)
        parcel.writeString(gif)
        parcel.writeString(default_thumbnail)
        parcel.writeString(user_thumbnail)
        parcel.writeString(privacy_type)
        parcel.writeString(allow_comments)
        parcel.writeString(allow_duet)
        parcel.writeString(liked)
        parcel.writeString(like_count)
        parcel.writeString(video_comment_count)
        parcel.writeString(views)
        parcel.writeString(duet_video_id)
        parcel.writeString(duet_username)
        parcel.writeString(favourite_count)
        parcel.writeString(share)
        parcel.writeString(duration)
        parcel.writeString(pin)
        parcel.writeString(playlistVideoId)
        parcel.writeString(playlistId)
        parcel.writeString(playlistName)
        parcel.writeString(block)
        parcel.writeString(aws_label)
        parcel.writeString(repost_video_id)
        parcel.writeString(repost_user_id)
        parcel.writeString(repost)
        parcel.writeString(lat)
        parcel.writeString(lng)
        parcel.writeString(location_string)
        parcel.writeString(locationId)
        parcel.writeString(placeId)
        parcel.writeString(location_name)
        parcel.writeString(location_image)
        parcel.writeString(storeName)
        parcel.writeString(productName)
        parcel.writeString(favourite)
        parcel.writeParcelable(apply_privacy_model, flags)
        parcel.writeParcelable(apply_push_notification_model, flags)
        parcel.writeList(tagProductList)
        parcel.writeParcelable(promotionModel, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<HomeModel> {
        override fun createFromParcel(parcel: Parcel): HomeModel {
            return HomeModel(parcel)
        }

        override fun newArray(size: Int): Array<HomeModel?> {
            return arrayOfNulls(size)
        }
    }

}
