package com.coheser.app.activitesfragments.accounts

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import com.coheser.app.activitesfragments.SplashActivity
import com.coheser.app.models.HomeModel
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.Functions
import com.coheser.app.simpleclasses.Variables
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.paperdb.Paper

object AccountUtils {
    val typeSignUp="signup"
    val typeLogin="login"
    val typeSocial="social"


    //store single account record
    @JvmStatic
    fun setUpMultipleAccount(context: Context, userModel: UserModel) {
        userModel.id?.let { Paper.book(Variables.MultiAccountKey).write(it, userModel) }
        storeUserLoginDataIntoDb(context, userModel)
    }


    @JvmStatic
    fun updateUserModel(userModel: UserModel) {
        userModel.id?.let {
            if(Functions.isStringHasValue(it) && Paper.book(Variables.MultiAccountKey).contains(it)) {
                Paper.book(Variables.MultiAccountKey).write(it, userModel)
            } }

    }

    @JvmStatic
    fun getUserModel(id:String): UserModel? {
        return Paper.book(Variables.MultiAccountKey).read<UserModel>(id)
    }


    @JvmStatic
    fun removeMultipleAccount(context: Context) {
        Functions.getSharedPreference(context).getString(Variables.U_ID, "0")?.let {
            Paper.book(Variables.MultiAccountKey)
                .delete(it)
        }
    }

    //store single account record
    @JvmStatic
    fun setUpNewSelectedAccount(context: Context, item: UserModel) {
        storeUserLoginDataIntoDb(context, item)
        Functions.getSharedPreference(context).edit().putBoolean(Variables.IsCartDataFetch,false).commit()
        val intent = Intent(context, SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(intent)
    }

    // use this method for lod muliple account in case one one account logout and other one can logout
    @JvmStatic
    fun setUpExistingAccountLogin(context: Context) {
        if (!Functions.getSharedPreference(context).getBoolean(Variables.IS_LOGIN, false)) {
            if (Paper.book(Variables.MultiAccountKey).allKeys.size > 0) {
                val account = Paper.book(Variables.MultiAccountKey).read<UserModel>(
                    Paper.book(Variables.MultiAccountKey).allKeys[0]
                )
                if (account != null) {
                    setUpNewSelectedAccount(context, account)
                }
            }
        }
    }

    fun setUpSwitchOtherAccount(context: Context, userId: String?) {
        for (key in Paper.book(Variables.MultiAccountKey).allKeys) {
            val account = Paper.book(Variables.MultiAccountKey).read<UserModel>(key)
            if (account != null) {
                if (userId.equals(account.id, ignoreCase = true)) {
                    setUpNewSelectedAccount(context, account)
                    return
                }
            }
        }
    }

    // manage for store user data
    @JvmStatic
    fun storeUserLoginDataIntoDb(context: Context, userDetailModel: UserModel) {
        val editor = Functions.getSharedPreference(context).edit()
        editor.putString(Variables.U_ID, userDetailModel.id)
        editor.putString(Variables.F_NAME, userDetailModel.first_name)
        editor.putString(Variables.L_NAME, userDetailModel.last_name)
        editor.putString(Variables.U_NAME, userDetailModel.username)
        editor.putString(Variables.U_BIO, userDetailModel.bio)
        editor.putString(Variables.U_LINK, userDetailModel.website)
        editor.putString(Variables.U_PHONE_NO, userDetailModel.phone)
        editor.putString(Variables.U_EMAIL, userDetailModel.email)
        editor.putString(Variables.U_SOCIAL_ID, userDetailModel.social_id)
        editor.putString(Variables.U_SOCIAL, userDetailModel.social)
        editor.putLong(Variables.U_Followers, userDetailModel.followers_count)
        editor.putLong(Variables.U_Followings, userDetailModel.following_count)
        editor.putString(Variables.GENDER, userDetailModel.gender)
        editor.putString(Variables.U_PIC, userDetailModel.getProfilePic())
        editor.putString(Variables.U_GIF, userDetailModel.getProfileGif())
        editor.putString(Variables.U_PROFILE_VIEW, userDetailModel.profile_view)
        editor.putString(Variables.U_WALLET, "" + userDetailModel.wallet)
        editor.putString(
            Variables.U_total_coins_all_time,
            "" + userDetailModel.total_all_time_coins
        )
        editor.putString(Variables.U_PAYOUT_ID, userDetailModel.paypal)
        editor.putString(Variables.AUTH_TOKEN, userDetailModel.auth_token)
        editor.putInt(Variables.IS_VERIFIED, userDetailModel.verified)
        editor.putBoolean(Variables.ISBusinessProfile, false)

        editor.putString(Variables.IS_VERIFICATION_APPLY, userDetailModel.applyVerification)
        editor.putString(Variables.REFERAL_CODE, userDetailModel.referral_code)
        editor.putBoolean(Variables.IS_LOGIN, true)
        editor.commit()
    }


    @JvmStatic
    fun removePreferenceData(context: Context) {
        Paper.book(Variables.PrivacySetting).destroy()
        removeMultipleAccount(context)
        val editor = Functions.getSharedPreference(context).edit()
        editor.clear()
        editor.commit()
        setUpExistingAccountLogin(context)

    }


    @JvmStatic
    fun saveRecentProfileData(context: Context, userModel: UserModel) {
        val gson = Gson()
        val json = gson.toJson(userModel)
        Functions.getSharedPreference(context).edit()
            .putString(Variables.profileJson, json).apply()
    }

    @JvmStatic
    fun getRecentProfileData(context: Context): UserModel? {
        val jsonString = Functions.getSharedPreference(context).getString(Variables.profileJson, "")
        if (jsonString != null) {
            val type = object : TypeToken<UserModel?>() {}.type
            val gson = Gson()
            return gson.fromJson(jsonString, type)
        }
        return null
    }

    @JvmStatic
    fun saveProfileVideoJson(context: Context, list:ArrayList<HomeModel>) {
        val gson = Gson()
        val json = gson.toJson(list)
        Functions.getSharedPreference(context).edit()
            .putString(Variables.profileJsonVideo, json).apply()
    }



    @JvmStatic
    fun getProfileVideo(context: Context): ArrayList<HomeModel> {

        val json = Functions.getSharedPreference(context).getString(Variables.profileJsonVideo, "")
        if (TextUtils.isEmpty(json)) {
            return ArrayList()
        } else {
            val type = object : TypeToken<ArrayList<HomeModel?>?>() {}.type
            val gson = Gson()
            return gson.fromJson(json, type)
        }

    }


}