package com.coheser.app.services


import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.HomeFragment
import com.coheser.app.apiclasses.FileUploader
import com.coheser.app.models.UploadVideoModel
import com.coheser.app.simpleclasses.DataHolder
import com.coheser.app.simpleclasses.FirebaseFunction
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables
import com.volley.plus.interfaces.Callback
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import java.io.File
import kotlin.coroutines.resume

class   VideoUploadWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {


    override suspend fun doWork(): Result {

        val bundle=DataHolder.instance?.data

        val videopath = bundle?.getString("uri") ?: return Result.failure()
        val draftFile = bundle?.getString("draft_file")
        val uploadModel = bundle?.getParcelable<UploadVideoModel>("data") ?: return Result.failure()


        DataHolder.instance?.data=null


        return try {
            val result = suspendCancellableCoroutine<Result> { continuation ->

                val defaultBase64=Functions.getSharedPreference(applicationContext).getString(Variables.default_video_thumb,"")
                val selectedThumb=Functions.getSharedPreference(applicationContext).getString(Variables.selected_video_thumb,"")

                FirebaseFunction.uploadVideoThumbFirebase(applicationContext,defaultBase64!!,object :Callback{
                    override fun onResponce(url: String?) {
                        if(url!=null && url.contains(Variables.http)) {
                            uploadModel.default_thumbnail=url

                            if(Functions.isStringHasValue(selectedThumb)){

                                FirebaseFunction.uploadVideoThumbFirebase(applicationContext,selectedThumb!!,object :Callback{
                                    override fun onResponce(url2: String?) {

                                            uploadModel.user_thumbnail= url2!!

                                            val fileUploader =
                                                FileUploader(File(videopath), applicationContext, uploadModel)
                                            fileUploader.SetCallBack(object :
                                                FileUploader.FileUploaderCallback {
                                                override fun onError() {
                                                    Functions.printLog(Constants.tag, "Error")
                                                    continuation.resume(Result.failure())
                                                    sendBroadByName(Variables.homeBroadCastAction)
                                                    sendBroadByName(Variables.profileBroadCastAction)
                                                }

                                                override fun onFinish(responses: String) {
                                                    Functions.printLog(Constants.tag, responses)
                                                    try {
                                                        val jsonObject = JSONObject(responses)
                                                        if (jsonObject.optInt("code", 0) == 200) {
                                                            Variables.reloadMyVideos = true
                                                            Variables.reloadMyVideosInner = true
                                                            deleteDraftFile(draftFile)
                                                            Functions.showToast(
                                                                applicationContext,
                                                                applicationContext.getString(R.string.your_video_is_uploaded_successfully)
                                                            )
                                                        }
                                                        continuation.resume(Result.success())
                                                        sendBroadByName(Variables.homeBroadCastAction)
                                                        sendBroadByName(Variables.profileBroadCastAction)
                                                    } catch (e: Exception) {
                                                        Functions.printLog(Constants.tag, "Exception: $e")
                                                        continuation.resume(Result.failure())
                                                        sendBroadByName(Variables.homeBroadCastAction)
                                                        sendBroadByName(Variables.profileBroadCastAction)
                                                    }
                                                }

                                                override fun onProgressUpdate(
                                                    currentpercent: Int,
                                                    totalpercent: Int,
                                                    msg: String
                                                ) {
                                                    if (currentpercent > 0) {
                                                        val bundle = Bundle().apply {
                                                            putBoolean("isShow", true)
                                                            putInt("currentpercent", currentpercent)
                                                            putInt("totalpercent", totalpercent)
                                                        }
                                                        HomeFragment.uploadingCallback?.onResponce(bundle)
                                                    }
                                                }
                                            })

                                    }
                                })

                            }

                            else {

                                val fileUploader =
                                    FileUploader(File(videopath), applicationContext, uploadModel)
                                fileUploader.SetCallBack(object :
                                    FileUploader.FileUploaderCallback {
                                    override fun onError() {
                                        Functions.printLog(Constants.tag, "Error")
                                        continuation.resume(Result.failure())
                                        sendBroadByName(Variables.homeBroadCastAction)
                                        sendBroadByName(Variables.profileBroadCastAction)
                                    }

                                    override fun onFinish(responses: String) {
                                        Functions.printLog(Constants.tag, responses)
                                        try {
                                            val jsonObject = JSONObject(responses)
                                            if (jsonObject.optInt("code", 0) == 200) {
                                                Variables.reloadMyVideos = true
                                                Variables.reloadMyVideosInner = true
                                                deleteDraftFile(draftFile)
                                                Functions.showToast(
                                                    applicationContext,
                                                    applicationContext.getString(R.string.your_video_is_uploaded_successfully)
                                                )
                                            }
                                            continuation.resume(Result.success())
                                            sendBroadByName(Variables.homeBroadCastAction)
                                            sendBroadByName(Variables.profileBroadCastAction)
                                        } catch (e: Exception) {
                                            Functions.printLog(Constants.tag, "Exception: $e")
                                            continuation.resume(Result.failure())
                                            sendBroadByName(Variables.homeBroadCastAction)
                                            sendBroadByName(Variables.profileBroadCastAction)
                                        }
                                    }

                                    override fun onProgressUpdate(
                                        currentpercent: Int,
                                        totalpercent: Int,
                                        msg: String
                                    ) {
                                        if (currentpercent > 0) {
                                            val bundle = Bundle().apply {
                                                putBoolean("isShow", true)
                                                putInt("currentpercent", currentpercent)
                                                putInt("totalpercent", totalpercent)
                                            }
                                            HomeFragment.uploadingCallback?.onResponce(bundle)
                                        }
                                    }
                                })
                            }
                        }
                        else{

                            continuation.resume(Result.failure())
                        }
                    }
                })

            }
            result
        }
        catch (e: Exception) {
            Functions.printLog(Constants.tag, "Exception: $e")
            Result.failure()
        }

    }


    private fun sendBroadByName(action: String) {
        val intent = Intent(action)
        intent.setPackage(applicationContext.packageName)
        applicationContext.sendBroadcast(intent)
    }

    private fun deleteDraftFile(draftFile: String?) {
        draftFile?.let {
            val file = File(it)
            if (file.exists()) {
                file.delete()
            }
        }
    }

}