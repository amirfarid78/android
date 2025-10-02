package com.coheser.app.apiclasses;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.coheser.app.simpleclasses.FileUtils;
import com.google.gson.Gson;
import com.coheser.app.Constants;
import com.coheser.app.models.UploadVideoModel;
import com.coheser.app.simpleclasses.Functions;
import com.google.gson.JsonIOException;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

public class FileUploader {

    private FileUploaderCallback mFileUploaderCallback;
    long filesize = 0l;
    UploadVideoModel uploadModel;

    public FileUploader(File file, Context context, UploadVideoModel uploadModel) {
        this.uploadModel=uploadModel;
        filesize = file.length();

        InterfaceFileUpload interfaceFileUpload = ApiClient.getRetrofitInstance(context)
                .create(InterfaceFileUpload.class);

        Functions.printLog(Constants.tag,""+new Gson().toJson(uploadModel).toString());
        Log.d(Constants.tag,"UploadFile: "+file.getAbsolutePath());

        PRRequestBody requestFile = new PRRequestBody(file);
        MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("video",
                file.getName(), requestFile);

//        RequestBody requestFile = RequestBody.create(MediaType.parse("video/*"), file);
//        MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("video",
//                file.getName(), requestFile);

        RequestBody PrivacyType = RequestBody.create(
                okhttp3.MultipartBody.FORM, uploadModel.privacyPolicy);

        RequestBody UserId = RequestBody.create(
                okhttp3.MultipartBody.FORM, uploadModel.userId);

        RequestBody SoundId = RequestBody.create(
                okhttp3.MultipartBody.FORM, uploadModel.soundId);

        RequestBody AllowComments = RequestBody.create(
                okhttp3.MultipartBody.FORM, uploadModel.allowComments);

        RequestBody Description = RequestBody.create(
                okhttp3.MultipartBody.FORM, uploadModel.description);

        RequestBody AllowDuet = RequestBody.create(
                okhttp3.MultipartBody.FORM, uploadModel.allowDuet);

        RequestBody UsersJson = RequestBody.create(
                okhttp3.MultipartBody.FORM, uploadModel.usersJson);

        RequestBody HashtagsJson = RequestBody.create(
                okhttp3.MultipartBody.FORM, uploadModel.hashtagsJson);

        RequestBody storyJson = RequestBody.create(
                okhttp3.MultipartBody.FORM, uploadModel.videoType);


        RequestBody productJson = RequestBody.create(
                okhttp3.MultipartBody.FORM, uploadModel.product_json);

        RequestBody width = RequestBody.create(
                okhttp3.MultipartBody.FORM, uploadModel.width);

        RequestBody height = RequestBody.create(
                okhttp3.MultipartBody.FORM, uploadModel.height);



        RequestBody userSelectThum = RequestBody.create(okhttp3.MultipartBody.FORM, uploadModel.getUser_thumbnail());

        RequestBody defaultThum = RequestBody.create(okhttp3.MultipartBody.FORM, uploadModel.getDefault_thumbnail());

        RequestBody user_selected_thum = RequestBody.create(okhttp3.MultipartBody.FORM, "0");


        Call<Object> fileUpload;
        RequestBody locationString = RequestBody.create(MediaType.parse("text/plain"), ""+uploadModel.getPlacesModel().address);
        RequestBody lat = RequestBody.create(MediaType.parse("text/plain"), ""+uploadModel.getPlacesModel().lat);
        RequestBody lng = RequestBody.create(MediaType.parse("text/plain"),""+uploadModel.getPlacesModel().lng);
        RequestBody placeId = RequestBody.create(MediaType.parse("text/plain"), ""+uploadModel.getPlacesModel().placeId);
        RequestBody locationName = RequestBody.create(MediaType.parse("text/plain"), ""+uploadModel.getPlacesModel().title);


       RequestBody videoId = RequestBody.create(okhttp3.MultipartBody.FORM, uploadModel.videoId);


            if(uploadModel.videoId.equalsIgnoreCase("0")) {
                fileUpload = interfaceFileUpload.UploadFile(fileToUpload,
                        PrivacyType,
                        UserId,
                        SoundId,
                        AllowComments,
                        Description,
                        AllowDuet,
                        UsersJson,
                        HashtagsJson,
                        storyJson,
                        videoId,
                        locationString,
                        lat,
                        lng,
                        placeId,
                        locationName,
                        width,
                        height,
                        productJson,
                        userSelectThum,
                        defaultThum,
                        user_selected_thum);
            }
            else {
                RequestBody duet = RequestBody.create(
                        okhttp3.MultipartBody.FORM, uploadModel.duet);
                fileUpload = interfaceFileUpload.UploadFile(fileToUpload,
                        PrivacyType,
                        UserId,
                        SoundId,
                        AllowComments,
                        Description,
                        AllowDuet,
                        UsersJson,
                        HashtagsJson,
                        storyJson,
                        videoId,
                        locationString,
                        lat,
                        lng,
                        placeId,
                        locationName,
                        width,
                        height,
                        productJson,
                        userSelectThum,
                        defaultThum,
                        user_selected_thum,
                        duet);
            }

        Log.d(Constants.tag, "************************  before call : " + fileUpload.request().url());

        fileUpload.enqueue(new Callback<Object>() {

            @Override
            public void onResponse(@NonNull Call<Object> call,
                                   @NonNull Response<Object> response) {

                String bodyRes=new Gson().toJson(response.body());
                Log.d(Constants.tag,"video Upload Responce: "+bodyRes);
                try {
                    JSONObject jsonObject = new JSONObject(bodyRes);
                    String code = jsonObject.optString("code","0");
                    if (code.contains("200")) {
                        mFileUploaderCallback.onFinish(bodyRes);
                    }
                    else {
                        mFileUploaderCallback.onError();
                      }
                }
                catch (Exception e)
                {
                    Log.d(Constants.tag,"Exception :"+e);
                    mFileUploaderCallback.onError();
                }


            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Log.d(Constants.tag,"Exception onFailure :"+t.toString());

                if (t instanceof HttpException) {
                    HttpException httpException = (HttpException) t;
                    int statusCode = httpException.code();
                    ResponseBody errorBody = httpException.response().errorBody();
                    try {
                        String errorString = errorBody.string();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                else if(t instanceof JsonIOException){
                    JsonIOException jsonIOException = (JsonIOException) t;
                    String errorMessage = jsonIOException.getMessage();
                    Log.e("JsonIOException", errorMessage);
                }
                mFileUploaderCallback.onError();
            }
        });


    }


    public FileUploader(File file, Context context, String userID) {

        InterfaceFileUpload interfaceFileUpload = ApiClient.getRetrofitInstance(context)
                .create(InterfaceFileUpload.class);

        PRRequestBody mFile = new PRRequestBody(file);
        MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("file",
                file.getName(), mFile);

        RequestBody UserId = RequestBody.create(
                okhttp3.MultipartBody.FORM, userID);

        RequestBody ExtensionId = RequestBody.create(
                okhttp3.MultipartBody.FORM, "mp4");

        Call<Object> fileUpload = interfaceFileUpload.UploadProfileImageVideo(fileToUpload,UserId,ExtensionId);

        Log.d(Constants.tag, "URL: " + fileUpload.request().url());
        Log.d(Constants.tag, "file: " + file.getAbsolutePath());
        Log.d(Constants.tag, "UserId: " + userID);
        Log.d(Constants.tag, "ExtensionId: " + "mp4");

        fileUpload.enqueue(new Callback<Object>() {

            @Override
            public void onResponse(@NonNull Call<Object> call,
                                   @NonNull Response<Object> response) {
                String bodyRes=new Gson().toJson(response.body());
                Log.d(Constants.tag,"Responce: "+bodyRes);
                try {
                    JSONObject jsonObject = new JSONObject(bodyRes);
                    int code = jsonObject.optInt("code",0);
                    if (code==200) {
                        mFileUploaderCallback.onFinish(bodyRes);
                    }
                    else
                    {
                        mFileUploaderCallback.onError();
                    }
                }
                catch (Exception e)
                {
                    Log.d(Constants.tag,"Exception :"+e);
                    mFileUploaderCallback.onError();
                }

            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Log.d(Constants.tag,"Exception onFailure :"+t.toString());
                mFileUploaderCallback.onError();
            }
        });


    }

    public void SetCallBack(FileUploaderCallback fileUploaderCallback) {
        this.mFileUploaderCallback = fileUploaderCallback;
    }


    public class PRRequestBody extends RequestBody {
        private File mFile;

        private static final int DEFAULT_BUFFER_SIZE = 1024;

        public PRRequestBody(final File file) {
            mFile = file;
        }

        @Override
        public MediaType contentType() {
            // i want to upload only images
            return MediaType.parse("multipart/form-data");
        }

        @Override
        public long contentLength() throws IOException {
            return mFile.length();
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            long fileLength = mFile.length();
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            FileInputStream in = new FileInputStream(mFile);
            long uploaded = 0;

            try {
                int read;
                Handler handler = new Handler(Looper.getMainLooper());
                while ((read = in.read(buffer)) != -1) {

                    handler.post(new ProgressUpdater(uploaded, fileLength));
                    uploaded += read;
                    sink.write(buffer, 0, read);
                }
            }
            catch (Exception e){
                Log.d(Constants.tag,"Exception : "+e);
            }
            finally {
                in.close();
            }
        }
    }


    private class ProgressUpdater implements Runnable {
        private long mUploaded=0;
        private long mTotal=0;

        ProgressUpdater(long uploaded, long total) {
            mUploaded = uploaded;
            mTotal = total;
        }

        @Override
        public void run() {
            int current_percent = (int) (100 * mUploaded / mTotal);
            int total_percent = (int) (100 * (mUploaded) / mTotal);
            mFileUploaderCallback.onProgressUpdate(current_percent, total_percent,
                    "File Size: " + FileUtils.readableFileSize(filesize));
        }
    }

    public interface FileUploaderCallback {

        void onError();

        void onFinish(String responses);

        void onProgressUpdate(int currentpercent, int totalpercent, String msg);
    }


}
