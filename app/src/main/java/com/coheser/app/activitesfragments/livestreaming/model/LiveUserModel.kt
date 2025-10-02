package com.coheser.app.activitesfragments.livestreaming.model

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import com.coheser.app.activitesfragments.shoping.models.ProductModel

class LiveUserModel() : Parcelable {

    @JvmField
    var streamingId: String? = null

    @JvmField
    var userId: String? = null

    @JvmField
    var userName: String? = null

    @JvmField
    var userPicture: String? = null

    @JvmField
    var onlineType: String? = null

    @JvmField
    var description: String = ""

    @JvmField
    var secureCode: String = ""

    @JvmField
    var joinStreamPrice: String = "0"

    @JvmField
    var userCoins: String? = null

    @JvmField
    var isVerified: Int = 0

    @JvmField
    var duetConnectedUserId: String? = null

    @JvmField
    var isDualStreaming: Boolean = true

    @JvmField
    var isStreamJoinAllow = true

    @JvmField
    var button = false

    @JvmField
    var streamUid:Int = -1

    var setGoalStream: SetGoalStream? = null

    @JvmField
    var productsList: ArrayList<ProductModel> = ArrayList()

    @JvmField
    var pkInvitation: PkInvitation? = null

    var GiftWishList: ArrayList<GiftWishListModel>?=null

    constructor(parcel: Parcel) : this() {
        streamingId = parcel.readString()
        userId = parcel.readString()
        userName = parcel.readString()
        userPicture = parcel.readString()
        onlineType = parcel.readString()
        description = parcel.readString().toString()
        secureCode = parcel.readString().toString()
        joinStreamPrice = parcel.readString().toString()
        userCoins = parcel.readString()
        isVerified = parcel.readInt()
        duetConnectedUserId = parcel.readString()
        isDualStreaming = parcel.readByte() != 0.toByte()
        isStreamJoinAllow = parcel.readByte() != 0.toByte()
        button = parcel.readByte() != 0.toByte()
        streamUid = parcel.readInt()
        setGoalStream = parcel.readParcelable(SetGoalStream::class.java.classLoader)
        productsList = parcel.readArrayList(ProductModel::class.java.classLoader) as ArrayList<ProductModel>
        pkInvitation = parcel.readParcelable(PkInvitation::class.java.classLoader)
        GiftWishList=parcel.readArrayList(GiftWishListModel::class.java.classLoader) as ArrayList<GiftWishListModel>?
    }

    fun getDuetConnectedUserId(): String {
        return if (duetConnectedUserId == null || TextUtils.isEmpty(duetConnectedUserId)) {
            return ""
        } else {
            return duetConnectedUserId!!
        }
    }

    fun setDuetConnectedUserId(duetConnectedUserId: String?) {
        this.duetConnectedUserId = duetConnectedUserId
    }

    fun getJoinStreamPrice(): String {
        return if (joinStreamPrice == null || TextUtils.isEmpty(joinStreamPrice)) {
            ""
        } else {
            joinStreamPrice!!
        }
    }

    fun setJoinStreamPrice(joinStreamPrice: String) {
        this.joinStreamPrice = joinStreamPrice
    }

    fun getUserCoins(): String {
        return if (userCoins == null || TextUtils.isEmpty(userCoins)) {
            ""
        } else {
            userCoins!!
        }
    }

    fun setUserCoins(userCoins: String?) {
        this.userCoins = userCoins
    }

    fun getStreamingId(): String {
        return if (streamingId == null || TextUtils.isEmpty(streamingId)) {
            ""
        } else {
            streamingId!!
        }
    }

    fun setStreamingId(streamingId: String?) {
        this.streamingId = streamingId
    }

    fun getUserId(): String {
        return if (userId == null || TextUtils.isEmpty(userId)) {
            ""
        } else {
            userId!!
        }
    }

    fun setUserId(userId: String?) {
        this.userId = userId
    }

    fun getUserName(): String {
        return if (userName == null || TextUtils.isEmpty(userName)) {
            ""
        } else {
            userName!!
        }
    }

    fun setUserName(userName: String?) {
        this.userName = userName
    }

    fun getUserPicture(): String {
        return if (userPicture == null || TextUtils.isEmpty(userPicture)) {
            ""
        } else {
            userPicture!!
        }
    }

    fun setUserPicture(userPicture: String?) {
        this.userPicture = userPicture
    }

    fun getOnlineType(): String {
        return if (onlineType == null || TextUtils.isEmpty(onlineType)) {
            ""
        } else {
            onlineType!!
        }
    }

    fun setOnlineType(onlineType: String?) {
        this.onlineType = onlineType
    }

    fun getDescription(): String {
        return if (description == null || TextUtils.isEmpty(description)) {
            ""
        } else {
            description!!
        }
    }

    fun setDescription(description: String) {
        this.description = description
    }

    fun getIsVerified(): Int {
        return isVerified
    }

    fun setIsVerified(isVerified: Int) {
        this.isVerified = isVerified
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(streamingId)
        parcel.writeString(userId)
        parcel.writeString(userName)
        parcel.writeString(userPicture)
        parcel.writeString(onlineType)
        parcel.writeString(description)
        parcel.writeString(secureCode)
        parcel.writeString(joinStreamPrice)
        parcel.writeString(userCoins)
        parcel.writeInt(isVerified)
        parcel.writeString(duetConnectedUserId)
        parcel.writeByte(if (isDualStreaming) 1 else 0)
        parcel.writeByte(if (isStreamJoinAllow) 1 else 0)
        parcel.writeByte(if (button) 1 else 0)
        parcel.writeInt(streamUid)
        parcel.writeParcelable(setGoalStream,flags)
        parcel.writeList(productsList)
        parcel.writeParcelable(pkInvitation, flags)
        parcel.writeList(GiftWishList)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LiveUserModel> {
        override fun createFromParcel(parcel: Parcel): LiveUserModel {
            return LiveUserModel(parcel)
        }

        override fun newArray(size: Int): Array<LiveUserModel?> {
            return arrayOfNulls(size)
        }
    }
}
