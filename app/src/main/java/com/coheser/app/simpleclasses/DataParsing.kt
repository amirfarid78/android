package com.coheser.app.simpleclasses

import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.coheser.activitiesfragments.accounts.model.Interest
import com.coheser.app.Constants
import com.coheser.app.activitesfragments.shoping.models.CategoryModel
import com.coheser.app.activitesfragments.shoping.models.ProductModel
import com.coheser.app.activitesfragments.spaces.models.TopicModel
import com.coheser.app.models.HomeModel
import com.coheser.app.models.PrivacySettingModel
import com.coheser.app.models.PromotionHistoryModel
import com.coheser.app.models.PromotionModel
import com.coheser.app.models.PushNotificationModel
import com.coheser.app.models.StoryVideoModel
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.TicTicApp.Companion.getProxy
import org.json.JSONArray
import org.json.JSONObject

object DataParsing {
    @JvmStatic
    fun getUserDataModel(user: JSONObject?): UserModel {
        val model = UserModel()
        try {
            if (user != null) {

                model.id = user.optString("id")
                model.first_name = user.optString("first_name")
                model.last_name = user.optString("last_name")
                model.gender = user.optString("gender")
                model.bio = user.optString("bio")
                model.website = user.optString("website")
                model.dob = user.optString("dob")
                model.social_id = user.optString("social_id")
                model.email = user.optString("email")
                model.phone = user.optString("phone")
                model.password = user.optString("password")
                model.setProfilePic(user.optString("profile_pic"))
                model.setProfileGif(user.optString("profile_gif"))
                model.profile_view = user.optString("profile_view")
                model.role = user.optString("role")
                model.username = user.optString("username")
                model.social = user.optString("social")
                model.device_token = user.optString("device_token")
                model.token = user.optString("token")
                model.active = user.optInt("active",0)
                model.lat = user.optDouble("lat", 0.0)
                model.lng = user.optDouble("long", 0.0)
                model.online = user.optInt("online",0)
                model.verified = user.optInt("verified",0)
                model.applyVerification = user.optString("verification_applied")
                model.auth_token = user.optString("auth_token")
                model.version = user.optString("version")
                model.device = user.optString("device")
                model.ip = user.optString("ip")
                model.city = user.optString("city")
                model.country = user.optString("country")
                model.city_id = user.optString("city_id")
                model.state_id = user.optString("state_id")
                model.country_id = user.optString("country_id")
                model.wallet = user.optLong("wallet", 0)
                model.profile_visit_count = user.optLong("profile_visit_count", 0)
                model.unread_notification = user.optLong("unread_notification", 0)
                model.paypal = user.optString("paypal")
                model.referral_code = user.optString("referral_code")
                model.reset_wallet_datetime = user.optString("reset_wallet_datetime")
                model.created = user.optString("created")
                model.followers_count = user.optLong("followers_count", 0)
                model.following_count = user.optLong("following_count", 0)
                model.likes_count = user.optLong("likes_count", 0)
                model.video_count = user.optLong("video_count", 0)
                model.notification = user.optString("notification", "1")
                model.button = user.optString("button","follow")
                model.block = user.optString("block", "0")
                model.sold_items_count = user.optLong("sold_items_count")
                model.tagged_products_count = user.optLong("tagged_products_count")
                model.comission_earned = user.optDouble("comission_earned",0.0)
                model.total_balance_usd = user.optDouble("total_balance_usd",0.0)

                model.business = user.optInt("business")

                try {
                    val blockObj = user.getJSONObject("BlockUser")
                    model.blockByUser = blockObj.optString("user_id", "0")
                } catch (e: Exception) {
                    model.blockByUser = user.optString("id")
                }

                val interests = user.optJSONArray("Interests")
                if (interests != null) {
                    model.intrestsCount = interests.length().toLong()
                }

            }
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception : $e")
        }
        return model
    }


    @JvmStatic
    fun parseVideoData(
        userObj: JSONObject?,
        sound: JSONObject?,
        video: JSONObject?,
        location: JSONObject?,
        store: JSONObject?,
        product: JSONObject?,
        userPrivacy: JSONObject?,
        userPushNotification: JSONObject?
    ): HomeModel {
        val item = HomeModel()
        val userDetailModel = getUserDataModel(userObj)
        item.userModel=userDetailModel
        if (!TextUtils.isEmpty(userDetailModel.id)) {
            item.user_id = userDetailModel.id
        }

        if (sound != null) {
            item.sound_id = sound.optString("id")
            item.sound_name = sound.optString("name")
            item.setSound_pic(sound.optString("thum"))
            item.setSound_url_mp3(sound.optString("audio"))
            item.setSound_url_acc(sound.optString("audio"))
        }
        if (video != null) {
            item.like_count = "0" + video.optInt("like_count")
            item.favourite_count = "0" + video.optInt("favourite_count")
            item.share = "0" + video.optInt("share")
            item.duration = video.optString("duration", "0")
            item.video_comment_count = video.optString("comment_count")
            item.video_user_id = video.optString("user_id")
            item.privacy_type = video.optString("privacy_type")
            item.allow_comments = video.optString("allow_comments")
            item.allow_duet = video.optString("allow_duet")
            item.video_id = video.optString("id")
            item.liked = video.optString("like")
            item.favourite = video.optString("favourite")
            item.block = video.optString("block")
            item.aws_label = video.optString("aws_label")
            try {
                val playlistObject = video.getJSONObject("PlaylistVideo")
                if (playlistObject.optString("id") == "0") {
                    item.playlistId = "0"
                    item.playlistName = ""
                } else {
                    item.playlistId = playlistObject.getJSONObject("Playlist").optString("id", "0")
                    item.playlistName =
                        playlistObject.getJSONObject("Playlist").optString("name", "")
                }
            } catch (e: Exception) {
                item.playlistId = "0"
                item.playlistName = ""
            }
            item.pin = video.optString("pin", "0")
            item.repost = video.optString("repost", "0")
            item.repost_video_id = video.optString("repost_video_id", "0")
            item.repost_user_id = video.optString("repost_user_id", "0")
            item.views = video.optString("view")
            item.video_description = video.optString("description")
            item.favourite = video.optString("favourite")
            item.created_date = video.optString("created")
            item.lat = video.optString("lat")
            item.lng = video.optString("long")
            item.location_string = video.optString("location_string")

            item.user_thumbnail=video.optString("user_thumbnail")
            item.default_thumbnail=video.optString("default_thumbnail")
            if(Functions.isStringHasValue(item.user_thumbnail)){
                item.setThum(item.user_thumbnail)
            }else if(Functions.isStringHasValue(item.default_thumbnail)){
                item.setThum(video.optString("default_thumbnail"))
            }else{
                item.setThum(video.optString("thum"))
            }

            item.setGif(video.optString("gif"))
            item.setVideo_url(video.optString("video", ""))
            try {
                if (TicTicApp.appLevelContext != null) {
                    val proxy = getProxy(TicTicApp.appLevelContext!!)
                    val proxyUrl = proxy.getProxyUrl(item.getVideo_url())
                    if (Functions.isWebUrl(proxyUrl)) {
                        item.setVideo_url(proxyUrl)
                    }
                }
            } catch (e: Exception) {
            }
            item.allow_duet = video.optString("allow_duet")
            item.duet_video_id = video.optString("duet_video_id")
            if (video.has("duet")) {
                val duet = video.optJSONObject("duet")
                if (duet != null) {
                    val userDetailModelDuet = getUserDataModel(duet.optJSONObject("User"))
                    if (!TextUtils.isEmpty(userDetailModelDuet.id)) item.duet_username =
                        userDetailModelDuet.username
                }
            }
            item.promote = video.optString("promote")
            try {
                if (video.has("Promotion")) {
                    val Promotion = video.optJSONObject("Promotion")
                    if (Promotion != null) {
                        val promotionObj = video.getJSONObject("Promotion")
                        val promotionModel = PromotionModel()
                        promotionModel.id = promotionObj.optString("id")
                        promotionModel.user_id = promotionObj.optString("user_id")
                        promotionModel.website_url = promotionObj.optString("website_url")
                        promotionModel.start_datetime = promotionObj.optString("start_datetime")
                        promotionModel.end_datetime = promotionObj.optString("end_datetime")
                        promotionModel.active = promotionObj.optString("active")
                        promotionModel.coin = promotionObj.optString("coin")
                        promotionModel.destination = promotionObj.optString("destination")
                        promotionModel.action_button = promotionObj.optString("action_button")
                        promotionModel.destination_tap = promotionObj.optString("destination_tap")
                        promotionModel.followers = promotionObj.optString("followers")
                        promotionModel.reach = promotionObj.optString("reach")
                        promotionModel.total_reach = promotionObj.optString("total_reach")
                        promotionModel.clicks = promotionObj.optString("clicks")
                        promotionModel.audience_id = promotionObj.optString("audience_id")
                        promotionModel.payment_card_id = promotionObj.optString("payment_card_id")
                        promotionModel.video_id = promotionObj.optString("video_id")
                        promotionModel.created = promotionObj.optString("created")
                        item.promotionModel = promotionModel
                    }
                } else {
                    item.promotionModel = null
                }
            } catch (e: Exception) {
                item.promotionModel = null
            }
        }
        if (location != null) {
            item.lat = location.optString("lat")
            item.lng = location.optString("long")
            item.location_string = location.optString("string")
            item.locationId = location.optString("id")
            item.placeId = location.optString("google_place_id")
            item.location_name = location.optString("name")
            item.location_image = location.optString("image")
        }
        if (product != null) {
            item.productName = product.optString("title")
        }
        if (store != null) {
            item.storeName = store.optString("name")
        }

        if (userPrivacy != null) {
            item.apply_privacy_model = Gson().fromJson(userPrivacy.toString(),
                PrivacySettingModel::class.java)
        }
        if (userPushNotification != null) {
            item.apply_push_notification_model = Gson().fromJson(userPushNotification.toString(),
                PushNotificationModel::class.java)

        }
        return item
    }


    @JvmStatic
    fun parseVideoDetailData(
        item:HomeModel,
        userObj: JSONObject,
        sound: JSONObject?,
        video: JSONObject?,
        location: JSONObject?,
        videoProduct: JSONArray?,
        userPrivacy: JSONObject?,
        userPushNotification: JSONObject?
    ): HomeModel {

        if(userObj!=null) {
            val userDetailModel = getUserDataModel(userObj)
            item.userModel = userDetailModel
            if (!TextUtils.isEmpty(userDetailModel.id)) {
                item.user_id = userDetailModel.id
            }
        }

        if (sound != null) {
            item.sound_id = sound.optString("id")
            item.sound_name = sound.optString("name")
            item.setSound_pic(sound.optString("thum"))
            item.setSound_url_mp3(sound.optString("audio"))
            item.setSound_url_acc(sound.optString("audio"))
        }
        if (video != null) {
            item.like_count = "0" + video.optInt("like_count")
            item.favourite_count = "0" + video.optInt("favourite_count")
            item.share = "0" + video.optInt("share")
            item.duration = video.optString("duration", "0")
            item.video_comment_count = video.optString("comment_count")
            item.video_user_id = video.optString("user_id")
            item.privacy_type = video.optString("privacy_type")
            item.allow_comments = video.optString("allow_comments")
            item.allow_duet = video.optString("allow_duet")
            item.video_id = video.optString("id")
            item.liked = video.optString("like")
            item.favourite = video.optString("favourite")
            item.block = video.optString("block")
            item.aws_label = video.optString("aws_label")

            if(video.has("PlaylistVideo")) {
                try {
                    val playlistObject = video.getJSONObject("PlaylistVideo")
                    if (playlistObject.optString("id") == "0") {
                        item.playlistId = "0"
                        item.playlistName = ""
                    } else {
                        item.playlistId =
                            playlistObject.getJSONObject("Playlist").optString("id", "0")
                        item.playlistName =
                            playlistObject.getJSONObject("Playlist").optString("name", "")
                    }
                } catch (e: Exception) {
                    item.playlistId = "0"
                    item.playlistName = ""
                }
            }

            item.pin = video.optString("pin", "0")
            item.repost = video.optString("repost", "0")
            item.repost_video_id = video.optString("repost_video_id", "0")
            item.repost_user_id = video.optString("repost_user_id", "0")
            item.views = video.optString("view")
            item.video_description = video.optString("description")
            item.favourite = video.optString("favourite")
            item.created_date = video.optString("created")
            item.lat = video.optString("lat")
            item.lng = video.optString("long")
            item.location_string = video.optString("location_string")

            item.user_thumbnail=video.optString("user_thumbnail")
            item.default_thumbnail=video.optString("default_thumbnail")
            if(Functions.isStringHasValue(item.user_thumbnail)){
                item.setThum(item.user_thumbnail)
            }else if(Functions.isStringHasValue(item.default_thumbnail)){
                item.setThum(video.optString("default_thumbnail"))
            }else{
                item.setThum(video.optString("thum"))
            }

            item.setGif(video.optString("gif"))
            item.setVideo_url(video.optString("video", ""))
            try {
                if (TicTicApp.appLevelContext != null) {
                    val proxy = getProxy(TicTicApp.appLevelContext!!)
                    val proxyUrl = proxy.getProxyUrl(item.getVideo_url())
                    if (Functions.isWebUrl(proxyUrl)) {
                        item.setVideo_url(proxyUrl)
                    }
                }
            } catch (e: Exception) {
            }

            item.allow_duet = video.optString("allow_duet")
            item.duet_video_id = video.optString("duet_video_id")
            if (video.has("duet")) {
                val duet = video.optJSONObject("duet")
                if (duet != null) {
                    val userDetailModelDuet = getUserDataModel(duet.optJSONObject("User"))
                    if (!TextUtils.isEmpty(userDetailModelDuet.id)) item.duet_username =
                        userDetailModelDuet.username
                }
            }

            item.promote = video.optString("promote")
            try {
                if (video.has("Promotion")) {
                    val Promotion = video.optJSONObject("Promotion")
                    if (Promotion != null) {
                        val promotionObj = video.getJSONObject("Promotion")
                        val promotionModel = PromotionModel()
                        promotionModel.id = promotionObj.optString("id")
                        promotionModel.user_id = promotionObj.optString("user_id")
                        promotionModel.website_url = promotionObj.optString("website_url")
                        promotionModel.start_datetime = promotionObj.optString("start_datetime")
                        promotionModel.end_datetime = promotionObj.optString("end_datetime")
                        promotionModel.active = promotionObj.optString("active")
                        promotionModel.coin = promotionObj.optString("coin")
                        promotionModel.destination = promotionObj.optString("destination")
                        promotionModel.action_button = promotionObj.optString("action_button")
                        promotionModel.destination_tap = promotionObj.optString("destination_tap")
                        promotionModel.followers = promotionObj.optString("followers")
                        promotionModel.reach = promotionObj.optString("reach")
                        promotionModel.total_reach = promotionObj.optString("total_reach")
                        promotionModel.clicks = promotionObj.optString("clicks")
                        promotionModel.audience_id = promotionObj.optString("audience_id")
                        promotionModel.payment_card_id = promotionObj.optString("payment_card_id")
                        promotionModel.video_id = promotionObj.optString("video_id")
                        promotionModel.created = promotionObj.optString("created")
                        item.promotionModel = promotionModel
                    }
                } else {
                    item.promotionModel = null
                }
            } catch (e: Exception) {
                item.promotionModel = null
            }

        }

        if (location != null) {
            item.lat = location.optString("lat")
            item.lng = location.optString("long")
            item.location_string = location.optString("string")
            item.locationId = location.optString("id")
            item.placeId = location.optString("google_place_id")
            item.location_name = location.optString("name")
            item.location_image = location.optString("image")
        }

        if (videoProduct != null && videoProduct.length() > 0) {
            item.tagProductList?.clear()
            for (i in 0 until videoProduct.length()) {
                try {
                    val `object` = videoProduct.getJSONObject(i)


                    val model = Gson().fromJson(`object`.toString(), ProductModel::class.java)
                    model.product.taggedName=`object`.optString("title")
                    item.tagProductList?.add(model)


                } catch (e: Exception) {
                    Functions.printLog(Constants.tag, "errorParseing videoProduct" + e.toString())
                }
            }
        }

        if (userPrivacy != null) {
            item.apply_privacy_model = Gson().fromJson(userPrivacy.toString(),
                PrivacySettingModel::class.java)
        }
        if (userPushNotification != null) {
            item.apply_push_notification_model = Gson().fromJson(userPushNotification.toString(),
                PushNotificationModel::class.java)
        }
        return item
    }



    @JvmStatic
    fun getVideoDataModel(video: JSONObject?): StoryVideoModel {
        var item: StoryVideoModel = StoryVideoModel()
        if (video != null) {
            item.id = video.optString("id")
            item.user_id = video.optString("user_id")
            item.description = video.optString("description")
            item.video = video.optString("video")
            try {
                if (TicTicApp.appLevelContext != null) {
                    val proxy = getProxy(TicTicApp.appLevelContext!!)
                    val proxyUrl = proxy.getProxyUrl(item.video)
                    if (Functions.isWebUrl(proxyUrl)) {
                        item.video = proxyUrl
                    }
                }
            } catch (e: Exception) {
            }
            item.thum = video.optString("thum")
            item.gif = video.optString("gif")
            item.view = video.optString("view")
            item.section = video.optString("section")
            item.sound_id = video.optString("sound_id")
            item.privacy_type = video.optString("privacy_type")
            item.allow_comments = video.optString("allow_comments")
            item.allow_duet = video.optString("allow_duet")
            item.block = video.optString("block")
            item.duet_video_id = video.optString("duet_video_id")
            item.old_video_id = video.optString("old_video_id")
            item.duration = video.optString("duration")
            item.promote = video.optString("promote")
            item.pin_comment_id = video.optString("pin_comment_id")
            item.pin = video.optString("pin")
            item.repost_user_id = video.optString("repost_user_id")
            item.repost_video_id = video.optString("repost_video_id")
            item.quality_check = video.optString("quality_check")
            item.aws_job_id = video.optString("aws_job_id")
            item.aws_label = video.optString("aws_label")
            item.story = video.optString("story")
            item.created = video.optString("created")
        }
        return item
    }

    @JvmStatic
    fun parsePromotionHistory(promotionObj: JSONObject): PromotionHistoryModel {
        val promotionModel = PromotionHistoryModel()
        promotionModel.id = promotionObj.optString("id")
        promotionModel.user_id = promotionObj.optString("user_id")
        promotionModel.website_url = promotionObj.optString("website_url")
        promotionModel.start_datetime = promotionObj.optString("start_datetime")
        promotionModel.end_datetime = promotionObj.optString("end_datetime")
        promotionModel.active = promotionObj.optString("active")
        promotionModel.coin = promotionObj.optString("coin")
        promotionModel.destination = promotionObj.optString("destination")
        promotionModel.action_button = promotionObj.optString("action_button")
        promotionModel.destination_tap = promotionObj.optString("destination_tap")
        promotionModel.followers = promotionObj.optString("followers")
        promotionModel.reach = promotionObj.optString("reach")
        promotionModel.total_reach = promotionObj.optString("total_reach")
        promotionModel.clicks = promotionObj.optString("clicks")
        promotionModel.audience_id = promotionObj.optString("audience_id")
        promotionModel.payment_card_id = promotionObj.optString("payment_card_id")
        promotionModel.video_id = promotionObj.optString("video_id")
        promotionModel.created = promotionObj.optString("created")
        promotionModel.status = promotionObj.optString("status")
        promotionModel.coins_consumed = promotionObj.optString("coins_consumed")
        return promotionModel
    }

    fun getCategoryDataModel(jsonObject: JSONObject): CategoryModel {
        val model = CategoryModel()
        try {
            model.id = jsonObject.optString("id")
            model.title = jsonObject.optString("title")
            model.image = jsonObject.optString("image")
            model.created = jsonObject.optString("created")
            model.parent_id = jsonObject.optString("parent_id")
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception : getTopicDataModel$e")
        }
        return model
    }



    fun getInterestDataModel(jsonObject: JSONObject): Interest{
        val model = Interest()
        try {
            model.id = jsonObject.optString("id")
            model.title = jsonObject.optString("title")
            model.selected = jsonObject.optString("selected")
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception : getInterestDataModel$e")
        }
        return model
    }

    fun getTopicDataModel(topic: JSONObject): TopicModel {
        val model = TopicModel()
        try {
            model.id = topic.optString("id")
            model.title = topic.optString("title")
            model.image = topic.optString("image")
            model.created = topic.optString("created")
            model.follow = topic.optString("follow")
        } catch (e: java.lang.Exception) {
            Log.d(Constants.tag, "Exception : getTopicDataModel$e")
        }
        return model
    }


}
