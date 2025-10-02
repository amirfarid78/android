package com.coheser.app.apiclasses;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;

public interface InterfaceFileUpload {

    @Multipart
    @POST(ApiLinks.postVideo)
    Call<Object> UploadFile(@Part MultipartBody.Part file,
                            @Part("privacy_type") RequestBody PrivacyType,
                            @Part("user_id") RequestBody UserId,
                            @Part("sound_id") RequestBody SoundId,
                            @Part("allow_comments") RequestBody AllowComments,
                            @Part("description") RequestBody Description,
                            @Part("allow_duet") RequestBody AllowDuet,
                            @Part("users_json") RequestBody UsersJson,
                            @Part("hashtags_json") RequestBody HashtagsJson,
                            @Part("story") RequestBody story,
                            @Part("video_id") RequestBody videoId,
                            @Part("location_string") RequestBody location_string,
                            @Part("lat") RequestBody lat,
                            @Part("long") RequestBody lng,
                            @Part("google_place_id") RequestBody placeID,
                            @Part("location_name") RequestBody locationName,
                            @Part("width") RequestBody width,
                            @Part("height") RequestBody height,
                            @Part("products") RequestBody productJson,
                            @Part("user_thumbnail") RequestBody userSelectThum,
                            @Part("default_thumbnail") RequestBody defaultThum,
                            @Part("user_selected_thum") RequestBody userthumbvalue);



    @Multipart
    @POST(ApiLinks.postVideo)
    Call<Object> UploadFile(@Part MultipartBody.Part file,
                            @Part("privacy_type") RequestBody PrivacyType,
                            @Part("user_id") RequestBody UserId,
                            @Part("sound_id") RequestBody SoundId,
                            @Part("allow_comments") RequestBody AllowComments,
                            @Part("description") RequestBody Description,
                            @Part("allow_duet") RequestBody AllowDuet,
                            @Part("users_json") RequestBody UsersJson,
                            @Part("hashtags_json") RequestBody HashtagsJson,
                            @Part("story") RequestBody story,
                            @Part("video_id") RequestBody videoId,
                            @Part("location_string") RequestBody location_string,
                            @Part("lat") RequestBody lat,
                            @Part("long") RequestBody lng,
                            @Part("google_place_id") RequestBody placeID,
                            @Part("location_name") RequestBody locationName,
                            @Part("width") RequestBody width,
                            @Part("height") RequestBody height,
                            @Part("products") RequestBody productJson,
                            @Part("user_thumbnail") RequestBody userSelectThum,
                            @Part("default_thumbnail") RequestBody defaultThum,
                            @Part("user_selected_thum") RequestBody userthumbvalue,
                            @Part("duet") RequestBody duet);



    @Multipart
    @POST(ApiLinks.editVideo)
    Call<Object> editVideo(@Part("privacy_type") RequestBody PrivacyType,
                            @Part("user_id") RequestBody UserId,
                            @Part("allow_comments") RequestBody AllowComments,
                            @Part("description") RequestBody Description,
                            @Part("allow_duet") RequestBody AllowDuet,
                            @Part("users_json") RequestBody UsersJson,
                            @Part("hashtags_json") RequestBody HashtagsJson,
                            @Part("video_id") RequestBody videoId,
                            @Part("location_string") RequestBody location_string,
                            @Part("lat") RequestBody lat,
                            @Part("long") RequestBody lng,
                            @Part("google_place_id") RequestBody placeID,
                            @Part("location_name") RequestBody locationName,
                            @Part("products") RequestBody productJson,
                            @Part("tag_store_id") RequestBody tagStoreId);


    @Multipart
    @POST(ApiLinks.addUserImage)
    Call<Object> UploadProfileImageVideo(@Part MultipartBody.Part file,
                                         @Part("user_id") RequestBody UserId,
                                         @Part("extension") RequestBody ExtensionId);

    @Multipart
    @POST(ApiLinks.addProductImage)
    Call<Object> UploadProductImage(@Part MultipartBody.Part file,
                                    @Part("product_id") RequestBody ProductId);


    @GET
    Call<ResponseBody> downloadFile(@Url String fileUrl);
}
