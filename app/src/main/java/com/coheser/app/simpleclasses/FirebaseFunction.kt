package com.coheser.app.simpleclasses

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.core.net.toUri
import com.coheser.app.Constants
import com.coheser.app.interfaces.FragmentCallBack
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.volley.plus.interfaces.Callback

object FirebaseFunction {

    @JvmStatic
    fun uploadImageToFirebase(context: Activity?, imageUri: Uri?, callback: Callback) {
        if (imageUri != null) {
            Functions.showLoader(context, false, false)
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val fileReference = storageRef.child("images/" + System.currentTimeMillis() + ".jpg")
            fileReference.putFile(imageUri)
                .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot? ->
                    Functions.cancelLoader()
                    fileReference.downloadUrl.addOnSuccessListener { uri: Uri ->
                        val imageUrl = uri.toString()
                        Functions.printLog(Constants.tag, "image url$imageUrl")
                        callback.onResponce(imageUrl)
                    }
                }
                .addOnFailureListener { e: Exception ->
                    Functions.cancelLoader()
                    Functions.printLog(Constants.tag, "" + e.toString())
                }
        }
    }


    @JvmStatic
    fun deleteImageFromFirebase(imageUrl: String) {
        try {
            Functions.printLog(Constants.tag, "image Url:$imageUrl")
            val storage = FirebaseStorage.getInstance()
            val imageRef = storage.getReferenceFromUrl(imageUrl)
            imageRef.delete().addOnSuccessListener { aVoid: Void? ->
                Functions.printLog(
                    Constants.tag,
                    "Image deleted successfully"
                )
            }
                .addOnFailureListener { exception: Exception? ->
                    Functions.printLog(
                        Constants.tag,
                        "Failed to delete image: "
                    )
                }
        } catch (e: Exception) {
        }
    }

    fun isLoginToFirebase(context: Context){
        if(Functions.getSharedPreference(context).getBoolean(Variables.IS_LOGIN,false)) {
            val mAuth = FirebaseAuth.getInstance()
            val currentUser: FirebaseUser? = mAuth.currentUser
            if (currentUser == null) {
                Functions.printLog(Constants.tag,"Firebase Logged Out")
            }
            else{
                Functions.printLog(Constants.tag,"Firebase Logged in")
            }

        }
    }

    private val mFunctions: FirebaseFunctions = FirebaseFunctions.getInstance("us-west2")
    fun sendVerificationCode(mobile: String,fragmentCallBack: FragmentCallBack): Task<HashMap<String, Any>?> {
        val data: MutableMap<String, Any> = HashMap()
        data["to"] = mobile // it must start with area code +xx

        return mFunctions
            .getHttpsCallable("sendVerificationCode")
            .call(data)
            .continueWith { task ->
                val bundle= Bundle()
                try {
                    val result = task.result?.data as HashMap<String, Any>?
                    bundle.putString("result","$result")
                    Functions.printLog(Constants.tag, "sendVerificationCode: $result")
                    result
                } catch (e: Exception) {
                    bundle.putString("result","$e")
                    Functions.printLog(Constants.tag, "sendVerificationCode: $e")
                    HashMap()
                }finally {
                    fragmentCallBack.onResponce(bundle)
                }
            }
    }

    fun verifyCode(mobile: String, code: String,fragmentCallBack: FragmentCallBack): Task<HashMap<String, Any>?> {
        val data: MutableMap<String, Any> = HashMap()
        data["to"] = mobile
        data["code"] = code // use the code parameter

        return mFunctions
            .getHttpsCallable("verifyCode")
            .call(data)
            .continueWith { task ->
                val bundle= Bundle()
                try {
                    val result = task.result?.data as HashMap<String, Any>?
                    bundle.putString("result","$result")
                    Functions.printLog(Constants.tag, "verifyCode: $result")
                    result
                } catch (e: Exception) {
                    bundle.putString("result","$e")
                    Functions.printLog(Constants.tag, "verifyCode: $e")
                    HashMap()
                } finally {
                    fragmentCallBack.onResponce(bundle)
                }
            }
    }


    fun uploadVideoThumbFirebase(context: Context?,base64:String, callback: Callback) {

        val filename="thumb_${Functions.getRandomString(4)}.jpeg"

            val bitmap=FileUtils.base64ToBitmap(base64)
                val uri= bitmap?.let {
                    FileUtils.getBitmapToUri(context!!,
                        it,filename)
                }
        if (uri != null) {
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val fileReference = storageRef.child("videos")
                .child(Functions.getSharedPreference(context).getString(Variables.AUTH_TOKEN,"NoIdUser_${0}").toString())
                .child("${DateOprations.getCurrentDate("yyyyMMdd")}")
                .child(filename)
            fileReference.putFile(uri.toUri())
                .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot? ->
                    Functions.cancelLoader()
                    fileReference.downloadUrl.addOnSuccessListener { uri: Uri ->
                        val imageUrl = uri.toString()
                        Functions.printLog(Constants.tag, "image url$imageUrl")
                        callback.onResponce(imageUrl)
                    }
                }
                .addOnFailureListener { e: Exception ->
                    Functions.cancelLoader()
                    Functions.printLog(Constants.tag, "" + e.toString())
                }
        }
    }


    fun uploadUserProfile(context: Context?,path:Uri, callback: Callback) {
        val filename="profileImage.jpeg"

        if (path != null) {
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val fileReference = storageRef.child("vendor")
                .child(Functions.getSharedPreference(context).getString(Variables.AUTH_TOKEN,"NoIdUser_${0}").toString())
                .child(filename)
            fileReference.putFile(path)
                .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot? ->
                    Functions.cancelLoader()
                    fileReference.downloadUrl.addOnSuccessListener { uri: Uri ->
                        val imageUrl = uri.toString()
                        Functions.printLog(Constants.tag, "image url$imageUrl")
                        callback.onResponce(imageUrl)
                    }
                }
                .addOnFailureListener { e: Exception ->
                    Functions.cancelLoader()
                    Functions.printLog(Constants.tag, "" + e.toString())
                }
        }
    }

}
