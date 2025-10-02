package com.coheser.app.activitesfragments.shoping.Utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.coheser.app.Constants;
import com.coheser.app.activitesfragments.shoping.models.AddProductModel;
import com.coheser.app.apiclasses.ApiClient;
import com.coheser.app.apiclasses.InterfaceFileUpload;
import com.coheser.app.simpleclasses.Functions;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.BufferedSink;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FileUploaderProductImages {

    private FileUploaderCallback mFileUploaderCallback;
    long filesize = 0l;
    AddProductModel model;

    public FileUploaderProductImages(File file, Context context, AddProductModel uploadModel) {
        this.model=uploadModel;
        filesize = file.length();

        InterfaceFileUpload interfaceFileUpload = ApiClient.getRetrofitInstance(context)
                .create(InterfaceFileUpload.class);

        Log.d(Constants.tag,"UploadFile: "+file.getAbsolutePath());
        PRRequestBody mFile = new PRRequestBody(file);
        MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("file",
                file.getName(), mFile);

        Call<Object> fileUpload;

            RequestBody productId = RequestBody.create(
                    MultipartBody.FORM, model.getId());

            fileUpload = interfaceFileUpload.UploadProductImage(fileToUpload,productId);


        Log.d(Constants.tag, "************************  before call : " +
                fileUpload.request().url());

        fileUpload.enqueue(new Callback<Object>() {

            @Override
            public void onResponse(@NonNull Call<Object> call,
                                   @NonNull Response<Object> response) {

                String bodyRes=new Gson().toJson(response.body());
                Log.d(Constants.tag,"ProductImageResponce: "+bodyRes);
                try {
                    JSONObject jsonObject = new JSONObject(bodyRes);
                    int code = jsonObject.optInt("code",0);
                    if (code==200) {
                        mFileUploaderCallback.onFinish(bodyRes);
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
//            Source source = null;

            try {
                int read;
//                source = Okio.source(mFile);
                Handler handler = new Handler(Looper.getMainLooper());
                while ((read = in.read(buffer)) != -1) {

                    // update progress on UI thread
                    handler.post(new ProgressUpdater(uploaded, fileLength));
                    uploaded += read;
                    sink.write(buffer, 0, read);
                }
            } catch (Exception e){
                Log.d(Constants.tag,"Exception : "+e);
            } finally {
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
                    "File Size: " + Functions.readableFileSize(filesize));
        }
    }

    public interface FileUploaderCallback {

        void onError();

        void onFinish(String responses);

        void onProgressUpdate(int currentpercent, int totalpercent, String msg);
    }


}
