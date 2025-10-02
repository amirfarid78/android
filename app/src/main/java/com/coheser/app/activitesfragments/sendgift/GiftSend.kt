package com.coheser.app.activitesfragments.sendgift


import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

 class GiftSend():Parcelable {
     @SerializedName("coin")
     var coin: Int? = null

     @SerializedName("created")
     var created: String? = null

     @SerializedName("gift_id")
     var giftId: Int? = null

     @SerializedName("id")
     var id: Int? = null

     @SerializedName("image")
     var image: String? = null

     @SerializedName("live_streaming_id")
     var liveStreamingId: Int? = null

     @SerializedName("receiver_id")
     var receiverId: Int? = null

     @SerializedName("sender_id")
     var senderId: Int? = null

     @SerializedName("title")
     var title: String? = null

     @SerializedName("video_id")
     var videoId: Int? = null

     constructor(parcel: Parcel) : this() {
         coin = parcel.readValue(Int::class.java.classLoader) as? Int
         created = parcel.readString()
         giftId = parcel.readValue(Int::class.java.classLoader) as? Int
         id = parcel.readValue(Int::class.java.classLoader) as? Int
         image = parcel.readString()
         liveStreamingId = parcel.readValue(Int::class.java.classLoader) as? Int
         receiverId = parcel.readValue(Int::class.java.classLoader) as? Int
         senderId = parcel.readValue(Int::class.java.classLoader) as? Int
         title = parcel.readString()
         videoId = parcel.readValue(Int::class.java.classLoader) as? Int
     }

     override fun writeToParcel(parcel: Parcel, flags: Int) {
         parcel.writeValue(coin)
         parcel.writeString(created)
         parcel.writeValue(giftId)
         parcel.writeValue(id)
         parcel.writeString(image)
         parcel.writeValue(liveStreamingId)
         parcel.writeValue(receiverId)
         parcel.writeValue(senderId)
         parcel.writeString(title)
         parcel.writeValue(videoId)
     }

     override fun describeContents(): Int {
         return 0
     }

     companion object CREATOR : Parcelable.Creator<GiftSend> {
         override fun createFromParcel(parcel: Parcel): GiftSend {
             return GiftSend(parcel)
         }

         override fun newArray(size: Int): Array<GiftSend?> {
             return arrayOfNulls(size)
         }
     }

 }