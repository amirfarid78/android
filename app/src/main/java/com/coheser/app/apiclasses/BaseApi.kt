package com.coheser.app.apiclasses

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface BaseApi {

    @POST("showDeliveryAddresses")
    fun showDeliveryAddresses(@Body body: String): Call<String>

    @POST("showUserDetail")
    fun showUserDetail(@Body body: String): Call<String>



    @POST("showNearbyVideos")
    fun showNearbyVideos(@Body body: String): Call<String>

    @POST("showFollowingVideos")
    fun showFollowingVideos(@Body body: String): Call<String>

    @POST("showRelatedVideos")
    fun showRelatedVideos(@Body body: String): Call<String>


    @POST("showSuggestedUsers")
    fun showSuggestedUsers(@Body body: String): Call<String>


    @POST("followUser")
    fun followUser(@Body body: String): Call<String>


    @POST("showInterestSection")
    fun showInterestSection(@Body body: String): Call<String>


    @POST("showSettings")
    fun showSettings(@Body body: String): Call<String>


    @POST("addPayout")
    fun addPayout(@Body body: String): Call<String>


    @POST("showPayout")
    fun showPayout(@Body body: String): Call<String>


    @POST("showVideosAgainstUserID")
    fun showVideosAgainstUserID(@Body body: String): Call<String>


    @POST("showUserLikedVideos")
    fun showUserLikedVideos(@Body body: String): Call<String>


    @POST("showUserRepostedVideos")
    fun showUserRepostedVideos(@Body body: String): Call<String>


    @POST("showStoreTaggedVideos")
    fun showStoreTaggedVideos(@Body body: String): Call<String>

    @POST("showTaggedVideosAgainstUserID")
    fun showTaggedVideosAgainstUserID(@Body body: String): Call<String>


    @POST("showFavouriteVideos")
    fun showFavouriteVideos(@Body body: String): Call<String>


    @POST("blockUser")
    fun blockUser(@Body body: String): Call<String>


    @POST("showFollowers")
    fun showFollowers(@Body body: String): Call<String>


    @POST("showFollowing")
    fun showFollowing(@Body body: String): Call<String>

    @POST("search")
    fun search(@Body body: String): Call<String>

    @POST("showProfileVisitors")
    fun showProfileVisitors(@Body body: String): Call<String>

    @POST("editProfile")
    fun editProfile(@Body body: String): Call<String>


    @POST("updatePushNotificationSettings")
    fun updatePushNotificationSettings(@Body body: String): Call<String>


    @POST("addPrivacySetting")
    fun addPrivacySetting(@Body body: String): Call<String>


    @POST("deleteUserAccount")
    fun deleteUserAccount(@Body body: String): Call<String>


    @POST("showBlockedUsers")
    fun showBlockedUsers(@Body body: String): Call<String>


    @POST("userVerificationRequest")
    fun userVerificationRequest(@Body body: String): Call<String>


    @POST("showReportReasons")
    fun showReportReasons(@Body body: String): Call<String>


    @POST("reportVideo")
    fun reportVideo(@Body body: String): Call<String>
    @POST("report")
    fun report(@Body body: String): Call<String>

    @POST("reportUser")
    fun reportUser(@Body body: String): Call<String>

    @POST("reportRoom")
    fun reportRoom(@Body body: String): Call<String>

    @POST("reportProduct")
    fun reportProduct(@Body body: String): Call<String>

    @POST("showVideosAgainstHashtag")
    fun showVideosAgainstHashtag(@Body body: String): Call<String>


    @POST("addHashtagFavourite")
    fun addHashtagFavourite(@Body body: String): Call<String>

    @POST("showDiscoverySections")
    fun showDiscoverySections(@Body body: String): Call<String>


    @POST("showVideosAgainstLocation")
    fun showVideosAgainstLocation(@Body body: String): Call<String>


    @POST("showVideoDetailAd")
    fun showVideoDetailAd(@Body body: String): Call<String>


    @POST("destinationTap")
    fun destinationTap(@Body body: String): Call<String>

    @POST("pinVideo")
    fun pinVideo(@Body body: String): Call<String>

    @POST("NotInterestedVideo")
    fun NotInterestedVideo(@Body body: String): Call<String>

    @POST("downloadVideo")
    fun downloadVideo(@Body body: String): Call<String>

    @POST("deleteWaterMarkVideo")
    fun deleteWaterMarkVideo(@Body body: String): Call<String>

    @POST("repostVideo")
    fun repostVideo(@Body body: String): Call<String>


    @POST("showVideoDetail")
    fun showVideoDetail(@Body body: String): Call<String>

    @POST("shareVideo")
    fun shareVideo(@Body body: String): Call<String>

    @POST("showAllNotifications")
    fun showAllNotifications(@Body body: String): Call<String>


    @POST("readNotification")
    fun readNotification(@Body body: String): Call<String>

    @POST("addDeliveryAddress")
    fun addDeliveryAddress(@Body body: String): Call<String>

    @POST("deleteDeliveryAddress")
    fun deleteDeliveryAddress(@Body body: String): Call<String>


    @POST("addSoundFavourite")
    fun addSoundFavourite(@Body body: String): Call<String>

    @POST("withdrawRequest")
    fun withdrawRequest(@Body body: String): Call<String>
    @POST("showWithdrawalHistory")
    fun showWithdrawalHistory(@Body body: String): Call<String>
    @POST("showProducts")
    fun showProducts(@Body body: String): Call<String>


    @POST("showFriendsStories")
    fun showFriendsStories(@Body body: String): Call<String>


    @POST("showUnReadNotifications")
    fun showUnReadNotifications(@Body body: String): Call<String>
    @POST("purchaseFromCard")
    fun purchaseFromCard(@Body body: String): Call<String>
    @POST("purchaseProduct")
    fun purchaseProduct(@Body body: String): Call<String>

    @POST("showShops")
    fun showShops(@Body body: String): Call<String>

    @POST("showRooms")
    fun showRooms(@Body body: String): Call<String>


    @POST("purchaseCoin")
    fun purchaseCoin(@Body body: String): Call<String>





}