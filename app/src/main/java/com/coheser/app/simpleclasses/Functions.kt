package com.coheser.app.simpleclasses

import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.util.Patterns
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.work.WorkManager
import com.amulyakhare.textdrawable.TextDrawable
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.generic.RoundingParams
import com.facebook.drawee.interfaces.DraweeController
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.facebook.imagepipeline.request.Postprocessor
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.LoadControl
import com.google.android.exoplayer2.upstream.DefaultAllocator
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.SplashActivity
import com.coheser.app.activitesfragments.accounts.LoginActivity
import com.coheser.app.activitesfragments.location.DeliveryAddress
import com.coheser.app.activitesfragments.profile.analytics.KeyMatricsModel
import com.coheser.app.activitesfragments.sendgift.GiftModel
import com.coheser.app.activitesfragments.shoping.models.ProductModel
import com.coheser.app.interfaces.GenrateBitmapCallback
import com.coheser.app.mainmenu.MainMenuActivity
import com.coheser.app.models.HomeModel
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.TicTicApp.Companion.appLevelContext
import com.volley.plus.interfaces.Callback
import io.paperdb.Paper
import jp.wasabeef.fresco.processors.BlurPostprocessor
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.DecimalFormat
import java.util.Arrays
import java.util.Locale
import java.util.concurrent.Executors
import java.util.regex.Pattern

object Functions {

    @JvmStatic
    fun getExoControler(): LoadControl {
        return DefaultLoadControl.Builder()
            .setAllocator(DefaultAllocator(true, 12 * 1024 * 1024))
            .setBufferDurationsMs(1000, 5000, 1000, 1000)
            .setTargetBufferBytes(-1)
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()
    }

    @JvmStatic
    fun isOpenGLVersionSupported(context: Context?, version: Int): Boolean {
        val activityManager =
            context!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val configurationInfo = activityManager.deviceConfigurationInfo
        return configurationInfo.reqGlEsVersion >= version
    }

    @JvmStatic
    fun isRTL(view: View?): Boolean {
        return ViewCompat.getLayoutDirection(view!!) == ViewCompat.LAYOUT_DIRECTION_RTL
    }

    @JvmStatic
    fun isWebUrl(url: String): Boolean {
        val urlRegex = "^((http|https)://)?([a-zA-Z0-9\\-]+\\.)+[a-zA-Z]{2,6}(:[0-9]+)?(/.*)?$"
        val pattern = Pattern.compile(urlRegex)
        val matcher = pattern.matcher(url)
        return matcher.matches()
    }

    @JvmStatic
    fun getPhoneResolution(activity: Activity): DisplayMetrics {
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics
    }

    fun getCountryCode(context: Context?): String {
        if (context != null) {
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (telephonyManager != null) {
                val countryCode = telephonyManager.networkCountryIso
                if (!TextUtils.isEmpty(countryCode)) {
                    return countryCode.uppercase(Locale.getDefault())
                }
            }
        }
        return Locale.getDefault().country.uppercase(Locale.getDefault())
    }

    fun copyCode(context: Context, text: String?) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("text", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
    }

    @JvmStatic
    fun applyPhoneNoValidation(number: String?, countryCode: String): String {
        var number = number
        if (number?.get(0) == '0') {
            number = number.substring(1)
        }
        number = number!!.replace("+", "")
        number = number.replace(countryCode, "")
        if (number[0] == '0') {
            number = number.substring(1)
        }
        number = countryCode + number
        number = number.replace(" ", "")
        number = number.replace("(", "")
        number = number.replace(")", "")
        number = number.replace("-", "")
        return number
    }

    // change the color of status bar into black
    fun blackStatusBar(activity: Activity) {
        val view = activity.window.decorView
        var flags = view.systemUiVisibility
        flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
        view.systemUiVisibility = flags
        activity.window.statusBarColor = Color.BLACK
    }

    @JvmStatic
    fun PrintHashKey(context: Context) {
        try {
            val info = context.packageManager
                .getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = Base64.encodeToString(md.digest(), Base64.DEFAULT)
                Log.d(Constants.tag, "KeyHash : $hashKey")
            }
        } catch (e: Exception) {
            Log.e(Constants.tag, "error:", e)
        }
    }

    // change the color of status bar into white
    fun whiteStatusBar(activity: Activity, tab: Int) {
        val view = activity.window.decorView
        var flags = view.systemUiVisibility
        flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        view.systemUiVisibility = flags
        if (DarkModePrefManager(activity).isNightMode) {
            activity.window.navigationBarColor = ContextCompat.getColor(activity, R.color.black)
            activity.window.statusBarColor = Color.BLACK
        } else {
            if (tab == 4 && getSharedPreference(activity).getBoolean(
                    Variables.ISBusinessProfile,
                    false
                )
            ) {
                activity.window.statusBarColor = ContextCompat.getColor(
                    activity,
                    R.color.my_business_color
                )
            } else {
                activity.window.statusBarColor = Color.WHITE
            }
        }
    }

    // close the keybord
    @JvmStatic
    fun hideSoftKeyboard(activity: Activity?) {
        val imm = activity!!.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun hideKeyboard(view: View) {
        val inputMethodManager =
            view.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    @JvmStatic
    fun getDPToPixels(i: Int): Float {
        if (appLevelContext == null) {
            return 0f
        } else {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                i.toFloat(),
                appLevelContext!!.resources.displayMetrics
            )
        }
    }

    // retun the sharepref instance
    @JvmStatic
    fun getSharedPreference(context: Context?): SharedPreferences {
        return if (Variables.sharedPreferences != null) Variables.sharedPreferences else {
            Variables.sharedPreferences =
                context!!.getSharedPreferences(Variables.PREF_NAME, Context.MODE_PRIVATE)
            Variables.sharedPreferences
        }
    }

    @JvmStatic
    fun getSettingsPreference(context: Context?): SharedPreferences {
        return if (Variables.settingsPreferences != null) Variables.settingsPreferences else {
            Variables.settingsPreferences = context!!.getSharedPreferences(
                Variables.SETTING_PREF_NAME,
                Context.MODE_PRIVATE
            )
            Variables.settingsPreferences
        }
    }

    // print any kind of log
    @JvmStatic
    fun printLog(title: String?, text: String?) {
        if (!Constants.IS_SECURE_INFO) {
            if (title != null && text != null) Log.d(title, text)
        }
    }



    @JvmStatic
    fun isValidEmail(target: CharSequence?): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    // change string value to integer
    @JvmStatic
    fun parseInterger(value: String?): Int {
        return if (value != null && value != "") {
            value.toInt()
        } else 0
    }

    // format the count value
    @JvmStatic
   inline fun getSuffix(value: String?): String? {
        return try {
            if (isStringHasValue(value)) {
                val count = value?.toLong()
                if (count!! < 1000) return "" + count
                val exp = (count.toDouble()?.let { Math.log(it) }?.div(Math.log(1000.0)))?.toInt()
                String.format(
                    Locale.ENGLISH, "%.1f %c",
                    count / Math.pow(1000.0, exp!!.toDouble()),
                    "kMBTPE"[exp - 1]
                )
            } else {
                "0"
            }
        } catch (e: Exception) {
            "0"
        }
    }

    // return  the rundom string of given length
    @JvmStatic
    fun getRandomString(n: Int): String {
        val AlphaNumericString = ("ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "abcdefghijklmnopqrstuvxyz")
        val sb = StringBuilder(n)
        for (i in 0 until n) {
            val index = (AlphaNumericString.length
                    * Math.random()).toInt()
            sb.append(AlphaNumericString[index])
        }
        return sb.toString()
    }

    @JvmStatic
    fun removeSpecialChar(s: String): String {
        return s.replace("[^a-zA-Z0-9]".toRegex(), "")
    }

    // show loader of simple messages

    @JvmStatic
    fun readableFileSize(size: Long): String {
        if (size <= 0) return "0"
        val units = arrayOf("B", "kB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(
            size / Math.pow(
                1024.0,
                digitGroups.toDouble()
            )
        ) + " " + units[digitGroups]
    }

    @JvmStatic
    fun createChunksOfList(
        originalList: MutableList<GiftModel>,
        chunkSize: Int
    ): List<MutableList<GiftModel>> {
        val listOfChunks: MutableList<MutableList<GiftModel>> = ArrayList()
        for (i in 0 until originalList.size / chunkSize) {
            listOfChunks.add(
                originalList.subList(
                    i * chunkSize, i * chunkSize
                            + chunkSize
                )
            )
        }
        if (originalList.size % chunkSize != 0) {
            listOfChunks.add(
                originalList.subList(
                    originalList.size
                            - originalList.size % chunkSize, originalList.size
                )
            )
        }
        return listOfChunks
    }

    // format the username
    @JvmStatic
    fun showUsername(username: String?): String {
        return if (username != null && username.contains("@")) username else "@$username"
    }

    // format the username
    fun showUsernameOnVideoSection(item: HomeModel): String {
         if (isStringHasValue(item.userModel?.first_name) && isStringHasValue(item.userModel?.last_name)) {
            return item.userModel?.first_name + " " + item.userModel?.last_name
        } else {
            return "" + item.userModel?.username!!
        }
    }

    @JvmStatic
    fun isMyServiceRunning(context: Context?, serviceClass: Class<*>): Boolean {
        val manager = context!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                Log.i("isMyServiceRunning?", true.toString() + "")
                return true
            }
        }
        Log.i("isMyServiceRunning?", false.toString() + "")
        return false
    }

    @JvmStatic
    fun isWorkManagerRunning(context: Context, tag: String): Boolean {
        val workInfos = WorkManager.getInstance(context).getWorkInfosByTag(tag).get()
        return workInfos.any { it.state == androidx.work.WorkInfo.State.RUNNING }
    }

    @JvmStatic
    fun bitmapToBase64(imagebitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        imagebitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        val byteArray = baos.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    fun base64ToBitmap(base_64: String?): Bitmap? {
        var decodedByte: Bitmap? = null
        try {
            val decodedString = Base64.decode(base_64, Base64.DEFAULT)
            decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
        } catch (e: Exception) {
        }
        return decodedByte
    }

    @JvmStatic
    fun isShowContentPrivacy(context: Context, string_case: String?, isFriend: Boolean): Boolean {
        var string_case = string_case
        return if (string_case == null) true else {
            string_case =
                stringParseFromServerRestriction(string_case)
            if (string_case.equals("Everyone", ignoreCase = true)) {
                true
            } else if (string_case.equals("Friends", ignoreCase = true) &&
                getSharedPreference(context)
                    .getBoolean(Variables.IS_LOGIN, false) && isFriend
            ) {
                true
            } else {
                false
            }
        }
    }

    @JvmStatic
    fun stringParseFromServerRestriction(res_string: String): String {
        var res_string = res_string
        res_string = res_string.lowercase(Locale.getDefault())
        res_string = res_string.replace("_", " ")
        return res_string
    }

    @JvmStatic
    fun stringParseIntoServerRestriction(res_string: String): String {
        var res_string = res_string
        res_string = res_string.lowercase(Locale.getDefault())
        res_string = res_string.replace(" ", "_")
        return res_string
    }


    @JvmStatic
    fun getAppFolder(context: Context): String {
        return try {
            context.getExternalFilesDir(null)!!.path + "/"
        } catch (e: Exception) {
            context.filesDir.path + "/"
        }
    }


    fun createDefultFolder(root: String?, folderName: String?): String {
        val defultFile = File(Environment.getExternalStoragePublicDirectory(root), folderName)
        if (!defultFile.exists()) {
            defultFile.mkdirs()
        }
        return defultFile.absolutePath
    }

    // Bottom is all the Apis which is mostly used in com we have add it
    // just one time and whenever we need it we will call it

    // initialize the loader dialog and show
    var dialog: Dialog? = null

    @JvmStatic
    fun showLoader(activity: Activity?, outside_touch: Boolean, cancleable: Boolean) {
        try {
            if (dialog != null) {
                cancelLoader()
                dialog = null
            }
            activity?.runOnUiThread {
                dialog = Dialog(activity)
                dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog!!.setContentView(R.layout.item_dialog_loading_view)
                dialog!!.window!!.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        activity,
                        R.drawable.d_round_white_background
                    )
                )
                if (!outside_touch) dialog!!.setCanceledOnTouchOutside(false)
                if (!cancleable) dialog!!.setCancelable(false)
                dialog!!.show()
            }
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception : $e")
        }
    }

    @JvmStatic
    fun cancelLoader() {
        try {
            if (dialog != null || dialog!!.isShowing) {
                dialog!!.cancel()
            }
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception : $e")
        }
    }

    //store single account record
    @JvmStatic
    fun setUpMultipleAccount(context: Context, userModel: UserModel) {
        userModel.id?.let { Paper.book(Variables.MultiAccountKey).write(it, userModel) }
        storeUserLoginDataIntoDb(context, userModel)
    }

    @JvmStatic
    fun updateUserModel(userModel: UserModel) {
        userModel.id?.let {
            if (isStringHasValue(it) && Paper.book(Variables.MultiAccountKey).contains(it)) {
                Paper.book(Variables.MultiAccountKey).write(it, userModel)
            }
        }

    }



    @JvmStatic
    fun removeMultipleAccount(context: Context) {
        getSharedPreference(context).getString(Variables.U_ID, "0")?.let {
            Paper.book(Variables.MultiAccountKey)
                .delete(it)
        }
    }

    //store single account record
    @JvmStatic
    fun setUpNewSelectedAccount(context: Context, item: UserModel) {
        storeUserLoginDataIntoDb(context, item)
        getSharedPreference(context).edit().putBoolean(Variables.IsCartDataFetch, false)
            .commit()
        val intent = Intent(context, SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(intent)
    }

    // use this method for lod muliple account in case one one account logout and other one can logout
    @JvmStatic
    fun setUpExistingAccountLogin(context: Context) {
        if (!getSharedPreference(context).getBoolean(Variables.IS_LOGIN, false)) {
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


    //check login status
    @JvmStatic
    fun checkLoginUser(context: Activity?): Boolean {
        return if (getSharedPreference(context!!)
                .getBoolean(Variables.IS_LOGIN, false)
        ) {
            true
        } else {
            val intent = Intent(context, LoginActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            context.startActivity(intent)
            false
        }
    }

    @JvmStatic
    fun checkLogin(context: Activity?): Boolean {
        return getSharedPreference(context!!)
                .getBoolean(Variables.IS_LOGIN, false)
    }


    @JvmStatic
    fun showToast(context: Context?, msg: String?) {
        if (Constants.IS_TOAST_ENABLE) {
            Toast.makeText(context, "" + msg, Toast.LENGTH_SHORT).show()
        }
    }

    // use for image loader and return controller for image load
    @JvmStatic
  inline  fun frescoImageLoad(
        url: String?,
        simpleDrawee: SimpleDraweeView,
        isGif: Boolean
    ): DraweeController {
        var url = url
        if (url == null) {
            url = Constants.BASE_URL
        } else if (!url.contains(Variables.http)) {
            url = Constants.BASE_URL + url
        }
        simpleDrawee.tag = url
        val request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
            .build()
        val controller: DraweeController
        controller = if (isGif) {
            Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setOldController(simpleDrawee.controller)
                .setAutoPlayAnimations(true)
                .build()
        } else {
            Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setOldController(simpleDrawee.controller)
                .build()
        }
        return controller
    }

    // use for image loader and return controller for image load
    @JvmStatic
  inline  fun frescoImageLoad(
        drawable: Drawable?,
        simpleDrawee: SimpleDraweeView,
        isGif: Boolean
    ): DraweeController {
        val controller: DraweeController
        simpleDrawee.hierarchy.setPlaceholderImage(drawable)
        simpleDrawee.hierarchy.setFailureImage(drawable)
        controller = if (isGif) {
            Fresco.newDraweeControllerBuilder()
                .setOldController(simpleDrawee.controller)
                .setAutoPlayAnimations(true)
                .build()
        } else {
            Fresco.newDraweeControllerBuilder()
                .setOldController(simpleDrawee.controller)
                .build()
        }
        return controller
    }

    // use for image loader and return controller for image load
    @JvmStatic
   inline fun frescoImageLoad(
        resourceUri: Uri?,
        resource: Int,
        simpleDrawee: SimpleDraweeView
    ): DraweeController {
        val request = ImageRequestBuilder.newBuilderWithSource(resourceUri)
            .build()
        val controller: DraweeController
        simpleDrawee.hierarchy.setPlaceholderImage(resource)
        simpleDrawee.hierarchy.setFailureImage(resource)
        controller = Fresco.newDraweeControllerBuilder()
            .setImageRequest(request)
            .setOldController(simpleDrawee.controller)
            .setAutoPlayAnimations(true)
            .build()
        return controller
    }

    // use for image loader and return controller for image load
    @JvmStatic
  inline fun frescoImageLoad(resourceUri: Uri?, isGif: Boolean): DraweeController {
        val request = ImageRequestBuilder.newBuilderWithSource(resourceUri)
            .build()
        val controller: DraweeController
        controller = if (isGif) {
            Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setAutoPlayAnimations(true)
                .build()
        } else {
            Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .build()
        }
        return controller
    }

    // use for image loader and return controller for image load
    @JvmStatic
    fun frescoGifLoad(
        url: String?,
        resource: Int,
        simpleDrawee: SimpleDraweeView
    ): DraweeController {
        var url = url
        if (url == null) {
            url = "null"
        }
        if (!url.contains(Variables.http)) {
            url = Constants.BASE_URL + url
        }
        val request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
            .build()
        val controller: DraweeController
        simpleDrawee.hierarchy.setPlaceholderImage(resource)
        simpleDrawee.hierarchy.setFailureImage(resource)
        controller = Fresco.newDraweeControllerBuilder()
            .setImageRequest(request)
            .setOldController(simpleDrawee.controller)
            .setAutoPlayAnimations(true)
            .build()
        return controller
    }

    // use for image loader and return controller for image load
    @JvmStatic
    fun frescoImageLoad(
        url: String?,
        resource: Int,
        simpleDrawee: SimpleDraweeView,
        isGif: Boolean
    ): DraweeController {
        var url = url
        if (url == null) {
            url = "null"
        }
        if (!url.contains(Variables.http)) {
            url = Constants.BASE_URL + url
        }
        simpleDrawee.tag = url
        val request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
            .build()
        val controller: DraweeController
        simpleDrawee.hierarchy.setPlaceholderImage(resource)
        simpleDrawee.hierarchy.setFailureImage(resource)
        if (isGif) {
            val roundingParams = RoundingParams.asCircle()
                .setRoundingMethod(RoundingParams.RoundingMethod.OVERLAY_COLOR).setOverlayColor(
                    ContextCompat.getColor(simpleDrawee.context, R.color.white)
                )
            roundingParams.setRoundAsCircle(true)
            simpleDrawee.hierarchy.roundingParams = roundingParams
            controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setOldController(simpleDrawee.controller)
                .setAutoPlayAnimations(true)
                .build()
        } else {
            controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setOldController(simpleDrawee.controller)
                .build()
        }
        return controller
    }

    // use for image loader and return controller for image load
    @JvmStatic
    fun frescoBlurImageLoad(
        url: String,
        context: Context?,
        radius: Int
    ): DraweeController {
        var url = url
        if (!url.contains(Variables.http)) {
            url = Constants.BASE_URL + url
        }
        val postprocessor: Postprocessor = BlurPostprocessor(context, radius)
        val request =
            ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
                .setPostprocessor(postprocessor)
                .build()
        return Fresco.newDraweeControllerBuilder()
            .setImageRequest(request)
            .build()
    }

   inline fun frescoImageLoad(
        context: Context,
        name: String,
        url: String?,
        simpleDrawee: SimpleDraweeView
    ): DraweeController {
        var url = url
        if (url == null || url == "null") {
            url = Constants.BASE_URL
        } else if (!url.contains(Variables.http)) {
            url = Constants.BASE_URL + url
        }
        val placeholderName = getNameFirstLatter(name)
        val drawable = TextDrawable.builder()
            .beginConfig()
            .textColor(ContextCompat.getColor(context, R.color.black))
            .useFont(Typeface.DEFAULT)
            .fontSize(context.resources.getDimension(R.dimen._14sdp).toInt()) /* size in px */
            .bold()
            .toUpperCase()
            .endConfig()
            .buildRound(
                "" + placeholderName,
                ContextCompat.getColor(context, R.color.graycolor)
            )
        simpleDrawee.hierarchy.setPlaceholderImage(drawable)
        simpleDrawee.hierarchy.setFailureImage(drawable)
        val request =
            ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
                .setResizeOptions(
                    ResizeOptions(
                        Constants.ALL_IMAGE_DEFAULT_SIZE,
                        Constants.ALL_IMAGE_DEFAULT_SIZE
                    )
                )
                .build()
        return Fresco.newDraweeControllerBuilder()
            .setImageRequest(request)
            .setOldController(simpleDrawee.controller)
            .build()
    }

   inline fun frescoImageLoad(
        context: Context?,
        name: String,
        fontSize: Int,
        url: String?,
        simpleDrawee: SimpleDraweeView
    ): DraweeController {
        var url = url
        if (url == null || url == "null") {
            url = Constants.BASE_URL
        } else if (!url.contains(Variables.http)) {
            url = Constants.BASE_URL + url
        }
        val placeholderName =
            getNameFirstLatter(name)
        val drawable = TextDrawable.builder()
            .beginConfig()
            .textColor(ContextCompat.getColor(context!!, R.color.black))
            .useFont(Typeface.DEFAULT)
            .fontSize(fontSize) /* size in px */
            .bold()
            .toUpperCase()
            .endConfig()
            .buildRound(
                "" + placeholderName,
                ContextCompat.getColor(context, R.color.gainsboro)
            )
        simpleDrawee.hierarchy.setPlaceholderImage(drawable)
        simpleDrawee.hierarchy.setFailureImage(drawable)
        val request =
            ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
                .setResizeOptions(
                    ResizeOptions(
                        Constants.ALL_IMAGE_DEFAULT_SIZE,
                        Constants.ALL_IMAGE_DEFAULT_SIZE
                    )
                )
                .build()
        return Fresco.newDraweeControllerBuilder()
            .setImageRequest(request)
            .setOldController(simpleDrawee.controller)
            .build()
    }

    inline fun getNameFirstLatter(name: String): String {
        return try {
            if (TextUtils.isEmpty(name)) {
                ""
            } else {
                val str = name.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                if (str.size > 0) {
                    str[0][0].toString() + "" + str[1][0]
                } else {
                    str[0][0].toString() + ""
                }
            }
        } catch (e: Exception) {
            "" + name[0]
        }
    }


    @JvmStatic
    fun isNotificaitonShow(userStatus: String): Boolean {
        return if (userStatus.equals("following", ignoreCase = true)) {
            true
        } else if (userStatus.equals("friends", ignoreCase = true)) {
            true
        } else if (userStatus.equals("follow back", ignoreCase = true)) {
            true
        } else {
            false
        }
    }

    //    com language change
    @JvmStatic
    fun setLocale(lang: String?, context: Activity?, className: Class<*>?, isRefresh: Boolean) {
        val languageArray = context!!.resources.getStringArray(R.array.app_language_code)
        val languageCode = Arrays.asList(*languageArray)
        if (languageCode.contains(lang)) {
            val myLocale = Locale(lang)
            val res = context.baseContext.resources
            val dm = res.displayMetrics
            val conf = Configuration()
            conf.setLocale(myLocale)
            res.updateConfiguration(conf, dm)
            context.onConfigurationChanged(conf)
            if (isRefresh) {
                updateActivity(context, className)
            }
        }
        if (DarkModePrefManager(context).isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    fun updateActivity(context: Activity, className: Class<*>?) {
        val intent = Intent(context, className)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    // manage for store user data
    @JvmStatic
    fun storeUserLoginDataIntoDb(context: Context, userDetailModel: UserModel) {
        val editor = getSharedPreference(context).edit()
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



    //check rational permission status
    @JvmStatic
    fun getPermissionStatus(activity: Activity?, androidPermissionName: String?): String {
        return if (ContextCompat.checkSelfPermission(
                activity!!,
                androidPermissionName!!
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    androidPermissionName
                )
            ) {
                "blocked"
            } else "denied"
        } else "granted"
    }

    //show permission setting screen
    @JvmStatic
    fun showPermissionSetting(context: Context?, message: String?) {
        Dialogs.showDoubleButtonAlert(
            context, context!!.getString(R.string.permission_alert), message,
            context.getString(R.string.cancel_), context.getString(R.string.settings), false
        ) { bundle ->
            if (bundle.getBoolean("isShow", false)) {
                val intent = Intent()
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", context.packageName, null)
                intent.setData(uri)
                context.startActivity(intent)
            }
        }
    }

    //    check com is exist or not
    @JvmStatic
    fun appInstalledOrNot(context: Context, uri: String?): Boolean {
        val pm = context.packageManager
        try {
            pm.getPackageInfo(uri!!, PackageManager.GET_ACTIVITIES)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
        }
        return false
    }

    // logout to com automatically when the login token expire
    @JvmStatic
    fun checkStatus(activity: Activity?, responce: String?) {
        try {
            val response = JSONObject(responce)
            if (response.optString("code", "").equals("501", ignoreCase = true)) {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                val googleSignInClient = GoogleSignIn.getClient(activity!!, gso)
                googleSignInClient.signOut()
                removeMultipleAccount(activity)
                val editor = getSharedPreference(activity).edit()
                Paper.book(Variables.PrivacySetting).destroy()
                editor.clear()
                editor.commit()
                activity.finish()
                setUpExistingAccountLogin(activity)
                activity.startActivity(Intent(activity, MainMenuActivity::class.java))
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun getHeaders(context: Context?): HashMap<String, String> {
        val headers = HashMap<String, String>()
        headers["Api-Key"] = Constants.API_KEY
        if (isStringHasValue(
                getSharedPreference(context).getString(
                    Variables.AUTH_TOKEN,
                    "null"
                )
            )
        ) {
            headers["Auth-Token"] =
                getSharedPreference(context).getString(Variables.AUTH_TOKEN, "null")!!
        }

        return headers
    }

    @JvmStatic
    fun getHeadersWithOutLogin(context: Context?): HashMap<String, String> {
        val headers = HashMap<String, String>()
        headers["Api-Key"] = Constants.API_KEY

        return headers
    }

    @JvmStatic
    fun getHeadersWithAuthTokon(authTokon: String): HashMap<String, String> {
        val headers = HashMap<String, String>()
        headers["Api-Key"] = Constants.API_KEY
        headers["Auth-Token"] = authTokon
        return headers
    }

    @JvmStatic
    fun clearFilesCacheBeforeOperation(vararg files: File) {
        if (files.size > 0) {
            for (file in files) {
                if (file.exists()) {
                    file.delete()
                }
            }
        }
    }

    @JvmStatic
    fun UrlToBitmapGenrator(imgUrl: String?, callback: GenrateBitmapCallback) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute { //Background work here
            val `in`: InputStream
            try {
                val url = URL(imgUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                `in` = connection.inputStream
                val myBitmap = BitmapFactory.decodeStream(`in`)
                callback.onResult(myBitmap)
                executor.shutdownNow()
            } catch (e: Exception) {
                Log.d(Constants.tag, "Exception: $e")
                executor.shutdownNow()
            }
        }
    }


    @JvmStatic
    fun showVideoDurationInSec(videoPath: String?): Int {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoPath)
            val bit = retriever.frameAtTime
            val duration =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            Integer.valueOf(duration) / 1000
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception: $e")
            10
        }
    }

    @JvmStatic
    fun getDevidedChunks(maxProgressTime: Int, chunkSize: Int): Long {
        return (maxProgressTime * 1000 / chunkSize).toLong()
    }

    @JvmStatic
    fun convertEmoji(emoji: String): String {
        val result: String
        result = try {
            val convertEmojiToInt = emoji.substring(2).toInt(16)
            val var8 = Character.toChars(convertEmojiToInt)
            String(var8)
        } catch (var5: Exception) {
            ""
        }
        return result
    }

    @JvmStatic
    fun showToastOnTop(activity: Activity?, mainView: View?, message: String?) {
        val inflater = activity!!.layoutInflater
        val layout: View
        layout = if (mainView == null) {
            inflater.inflate(R.layout.custom_toast, null)
        } else {
            inflater.inflate(
                R.layout.custom_toast,
                mainView.findViewById(R.id.custom_toast_container)
            )
        }
        val tvMessage = layout.findViewById<TextView>(R.id.tvMessage)
        tvMessage.text = message
        val toast = Toast(activity)
        toast.setGravity(Gravity.TOP, 0, 40)
        toast.duration = Toast.LENGTH_LONG
        toast.view = layout
        toast.show()
    }

    @JvmStatic
    fun getPercentage(currentValue: Int, totalValue: Int): Int {
        return currentValue * 100 / totalValue
    }

    @JvmStatic
    fun createChunksOfListKeyMatrics(
        originalList: List<KeyMatricsModel>,
        chunkSize: Int
    ): List<List<KeyMatricsModel>> {
        val listOfChunks: MutableList<List<KeyMatricsModel>> = ArrayList()
        for (i in 0 until originalList.size / chunkSize) {
            listOfChunks.add(
                originalList.subList(
                    i * chunkSize, i * chunkSize
                            + chunkSize
                )
            )
        }
        if (originalList.size % chunkSize != 0) {
            listOfChunks.add(
                originalList.subList(
                    originalList.size
                            - originalList.size % chunkSize, originalList.size
                )
            )
        }
        return listOfChunks
    }

    fun getShareRoomLink(context: Context, userID: String): String {
        return Variables.https + "://" + context.getString(R.string.domain) + context.getString(R.string.share_space_endpoint_second) + userID
    }

    fun shareData(activity: Activity, data: String?) {
        try {
            val sendIntent = Intent("android.intent.action.MAIN")
            sendIntent.setAction(Intent.ACTION_SEND)
            sendIntent.setType("text/plain")
            sendIntent.putExtra(Intent.EXTRA_TEXT, data)
            activity.startActivity(sendIntent)
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception : $e")
        }
    }

    @JvmStatic
    fun convertDpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    @JvmStatic
    fun ParseDouble(doubleValue: Double, digit: Int): String {
        return String.format("%." + digit + "f", doubleValue)
    }


    @JvmStatic
    fun changeValueToInt(value: String): Int {
        val longvalue = value.toDouble()
        return Integer.valueOf(longvalue.toInt())
    }

    @JvmStatic
    fun checkProfileOpenValidation(userId: String?): Boolean {
        return if (Variables.sharedPreferences.getString(Variables.U_ID, "0") == userId) {
            val profile = MainMenuActivity.tabLayout!!.getTabAt(4)
            profile!!.select()
            false
        } else {
            true
        }
    }

    @JvmStatic
    fun isMyProfile(userId: String?): Boolean {
        return Variables.sharedPreferences.getString(Variables.U_ID, "0") == userId
    }

    fun checkProfileOpenValidationByUserName(userName: String?): Boolean {
        return if (Variables.sharedPreferences.getString(Variables.U_NAME, "0") == userName) {
            val profile = MainMenuActivity.tabLayout!!.getTabAt(4)
            profile!!.select()
            false
        } else {
            true
        }
    }

    @JvmStatic
    fun isStringHasValue(text: String?): Boolean {
        return text != null &&
            (!text.equals("", ignoreCase = true)
                    && !text.equals("null", ignoreCase = true))
    }

    @JvmStatic
    fun roundoffDecimal(value: Double?): Double {
        return try {
            java.lang.Double.valueOf(DecimalFormat("##.##").format(value))
        } catch (e: Exception) {
            0.0
        }
    }

    @JvmStatic
    fun capitalizeEachWord(str: String): String {
        val text = str.replace("_".toRegex(), " ")
        val result = StringBuilder(text.length)
        var capitalizeNext = true
        for (c in text.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalizeNext = true
                result.append(c)
            } else if (capitalizeNext) {
                capitalizeNext = false
                result.append(c.uppercaseChar())
            } else {
                result.append(c)
            }

        }
        return result.toString()
    }

    @JvmStatic
    fun isWidthGreaterThanHeight(videoFilePath: String): Boolean {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(videoFilePath)
        val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
        val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
        return if (width != null && height != null) {
            val videoWidth = width.toInt()
            val videoHeight = height.toInt()
            videoWidth > videoHeight
        } else {
            false
        }
    }

    @JvmStatic
    fun isFileSizeLessThan50KB(filePath: String?): Boolean {
        // Create a File object with the specified file path
        val file = File(filePath)

        // Check if the file exists and is a file (not a directory)
        return if (file.exists() && file.isFile) {
            // Get the file size in bytes
            val fileSizeInBytes = file.length()
            // Convert file size to kilobytes
            val fileSizeInKB = fileSizeInBytes / 1024

            // Check if file size is less than 50KB
            fileSizeInKB < 50
        } else {
            // File doesn't exist or is not a file
            false
        }
    }

    fun encodeToShortString(longString: String?): String? {
        return try {
            // Create SHA-256 hash
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(longString!!.toByteArray(StandardCharsets.UTF_8))

            // Encode hash bytes to Base64
            var base64String = Base64.encodeToString(hashBytes, Base64.NO_WRAP)
            base64String = base64String.replace("/".toRegex(), "-")
            base64String.substring(0, 10)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            null
        }
    }

    @JvmStatic
    fun saveAddressModel(model: DeliveryAddress?, context: Context) {
        if (model == null) {
            getSharedPreference(context).edit().putString(Variables.addressModel, "").apply()
        } else {
            val gson = Gson()
            val json = gson.toJson(model)
            printLog(Constants.tag, "saveAddressModel" + json)
            getSharedPreference(context).edit().putString(Variables.addressModel, json).apply()
        }
    }

    fun getAddressModel(context: Context): DeliveryAddress? {
        val json = getSharedPreference(context).getString(Variables.addressModel, "")
        if (!TextUtils.isEmpty(json)) {
            val gson = Gson()
            return gson.fromJson(json, DeliveryAddress::class.java)
        }
        return null
    }






    fun getPercentageValue(context: Context, value: Int, isInCent: Boolean): Double {
        val percentage = getSettingsPreference(context)
            .getString(Variables.MarkedPrice, "0")!!.toDouble()
        val increment = value * (percentage / 100)
        return if (isInCent) {
            increment
        } else {
            increment / 100
        }
    }

    @JvmStatic
    fun applyCommission(priceStr: String?, context: Context?): String {
//        return try {
//            val commissionStr = getSettingsPreference(context).getString(Variables.MarkedPrice, "")
//            Log.d(Constants.tag, "comision value $commissionStr")
//            Log.d(Constants.tag, " before price  $priceStr")
//            val newprice = priceStr!!.substring(1)
//            Log.d(Constants.tag, "split  $newprice")
//            val commission = commissionStr!!.toDouble()
//            val price = newprice.toDouble()
//            val newPrice: Double
//            // Calculate the new price by applying the commission
//            newPrice = if (commission == 0.0) {
//                price
//            } else {
//                price + price * commission / 100
//            }
//
//            "$" + String.format("%.2f", newPrice)
//        } catch (e: Exception) {
//            priceStr!!
//        }

        return priceStr!!
    }

    fun applyCommission(priceInCents: Int, context: Context): String {
        return try {
            // Retrieve the commission value from settings as a string
//            val commissionStr = getSettingsPreference(context).getString(Variables.MarkedPrice, "")
//            Log.d(Constants.tag, "Commission value: $commissionStr")
//            Log.d(Constants.tag, "Original price in cents: $priceInCents")
//
//            // Parse the commission string to a double
//            val commission = commissionStr!!.toDouble()

            // Convert cents to dollars
            val priceInDollars = priceInCents / 100.0

            // Calculate the new price by applying the commission
//            val newPriceInDollars: Double
//            newPriceInDollars = if (commission == 0.0) {
//                priceInDollars
//            } else {
//                priceInDollars + priceInDollars * commission / 100.0
//            }
//            Log.d(
//                Constants.tag,
//                "New price in dollars: " + String.format("%.2f", newPriceInDollars)
//            )

            // Return the new price formatted to 2 decimal places
            String.format("%.2f", priceInDollars)
        } catch (e: Exception) {
            Log.e(Constants.tag, "Error in applyCommission", e)
            // In case of any error, return the original price in dollars formatted to 2 decimal places
            String.format("%.2f", priceInCents / 100.0)
        }
    }

    fun applyServiceFee(priceInCents: Double, context: Context): Double {
        return try {
            val commissionStr =
                getSettingsPreference(context).getString(Variables.FoodtokComission, "")
            val commission = commissionStr!!.toDouble()
            val newPrice = if (commission == 0.0) {
                0.0
            } else {
                (priceInCents * commission / 100.0)
            }
            return newPrice
        } catch (e: Exception) {
            return priceInCents
        }
    }

    @JvmStatic
    fun resizeVideo(targetWidth: Int, originalWidth: Int, originalHeight: Int): IntArray {

        // Calculate the ratio
        val ratio = targetWidth.toDouble() / originalWidth

        // Calculate the new height
        val newHeight = (originalHeight * ratio).toInt()
        return intArrayOf(targetWidth, newHeight)
    }

    @JvmStatic
    fun removePreferenceData(context: Context) {
        Paper.book(Variables.PrivacySetting).destroy()
        removeMultipleAccount(context)
        val editor = getSharedPreference(context).edit()
        editor.clear()
        editor.commit()
        setUpExistingAccountLogin(context)

    }

    fun decodeKey(encodedString: String): String {
        val decodedBytes = Base64.decode(encodedString, Base64.NO_WRAP)
        return String(decodedBytes)
    }

    fun getGeoCodeing(context: Context, location: LatLng): DeliveryAddress? {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (addressList != null && addressList.size > 0) {

                val address = addressList[0]

                val streetNum = address.subThoroughfare ?: "N/A"
                val street = address.thoroughfare ?: "N/A"
                val state = address.adminArea ?: "N/A"
                val country = address.countryName ?: "N/A"
                val cityName = address.locality ?: "N/A"
                val zipCode = address.postalCode ?: "N/A"

                val deliveryAddress = DeliveryAddress()
                deliveryAddress.id = "0"
                deliveryAddress.label = "$cityName"
                deliveryAddress.street = street
                deliveryAddress.street_num = streetNum
                deliveryAddress.state = state
                deliveryAddress.city = cityName
                deliveryAddress.zip = zipCode
                deliveryAddress.lat = location.latitude.toString()
                deliveryAddress.lng = location.longitude.toString()
                deliveryAddress.location_string = addressList[0].getAddressLine(0)

                return deliveryAddress

            }
        } catch (e: IOException) {
            Log.d(Constants.tag, "Exception GetGeoCoding: $e")

        }
        return null
    }




    fun getJsonFromRaw(context: Context, resourceId: Int): JsonObject? {
        try {
            val `is` = context.resources.openRawResource(resourceId)
            val reader = InputStreamReader(`is`, "UTF-8")
            val gson = Gson()
            return gson.fromJson(reader, JsonObject::class.java)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun searchItemById(country: String): String? {
        val jsonObject = appLevelContext?.let { getJsonFromRaw(it, R.raw.zonestime) }
        if (jsonObject != null) {
            try {
                val countries = jsonObject.getAsJsonObject("countries")
                val country = countries.getAsJsonObject(country)
                if (country != null) {
                    val zones = country.getAsJsonArray("zones")
                    return zones.get(0).toString() // Return the item with the specified ID
                }

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        return null // Return null if the item is not found
    }

    fun copyTextToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", text)
        clipboard.setPrimaryClip(clip)
    }

    fun getAddressLable(model: DeliveryAddress?): String? {
        if (isStringHasValue(model?.label!!)) {
            return model.label
        } else {
            return model.location_string?.substringBefore(",")
        }
    }

    fun getAddressString(model: DeliveryAddress?): String? {
        if (isStringHasValue(model?.label!!)) {
            return model.location_string
        } else {
            return model.location_string?.substringAfter(",")
        }

    }

    @JvmStatic
    fun sendBroadByName(context: Context, action: String) {
        val intent = Intent(action)
        intent.setPackage(context.packageName)
        context.sendBroadcast(intent)
    }

    @JvmStatic
    fun openCartScreen(context: Activity, resultCallback: ActivityResultLauncher<Intent>?) {
//        var intent: Intent? = null
//        if (getSharedPreference(context).getString(Variables.carttype, Variables.cartTypeMealMe)
//                .equals(Variables.cartTypeDish)
//        ) {
//            intent = Intent(context, TagDishCartActivity::class.java)
//        } else {
//            intent = Intent(context, TagProductCartActivity::class.java)
//        }
//        if (resultCallback == null) {
//            context.startActivity(intent)
//        } else {
//            resultCallback.launch(intent)
//        }
//        context.overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left)
    }


    fun getProductPrice(productModel: ProductModel?): String {
        return if (productModel == null) {
            "0"
        } else if (productModel.product.sale_price.toDouble() > 0) {
            productModel.product.sale_price
        } else {
            productModel.product.price
        }
    }

    fun replaceSpecialCharactersWithUnderscore(input: String): String {
        // Define a regex pattern for special characters
        val pattern = "[^a-zA-Z0-9]"

        // Replace special characters with underscores
        return input.replace(pattern.toRegex(), "_")
    }
    @JvmStatic
    fun showAlert(
        context: Context?,
        title: String?,
        Message: String?,
        postivebtn: String?,
        negitivebtn: String?,
        callback: Callback
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(Message)
            .setNegativeButton(negitivebtn) { dialog, which ->
                dialog.dismiss()
                callback.onResponce("no")
            }
            .setPositiveButton(
                postivebtn
            ) { dialog, which ->
                dialog.dismiss()
                callback.onResponce("yes")
            }.show()
    }
    @JvmStatic
    fun removeAtSymbol(input: String): String {
        return if (input.startsWith("@")) input.substring(1) else input
    }

}
