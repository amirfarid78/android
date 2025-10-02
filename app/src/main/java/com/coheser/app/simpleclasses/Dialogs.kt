package com.coheser.app.simpleclasses

import android.animation.ObjectAnimator
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.material.snackbar.Snackbar
import com.coheser.app.Constants
import com.coheser.app.R
import com.coheser.app.activitesfragments.livestreaming.CallBack
import com.coheser.app.activitesfragments.spaces.utils.CookieBar
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.simpleclasses.Functions.frescoImageLoad
import com.realpacific.clickshrinkeffect.applyClickShrink
import com.volley.plus.interfaces.Callback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object Dialogs {

    @JvmStatic
    fun showAlert(activity: Activity?, title: String?, Message: String?) {
        activity?.runOnUiThread {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(title)
            builder.setMessage(Message)
            builder.setNegativeButton(activity.getString(R.string.ok)) { dialog, which -> dialog.dismiss() }
            builder.create()
            builder.show()
        }
    }

    // dialog for show loader for showing dialog with title and descriptions
    @JvmStatic
    fun showAlert(context: Context, title: String?, description: String?, callBack: CallBack?) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(description)
        builder.setPositiveButton(context.getString(R.string.ok)) { dialog, id ->
            callBack?.getResponse(
                "alert",
                "OK"
            )
        }
        builder.create()
        builder.show()
    }

    // dialog for show any kind of alert
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
            .setPositiveButton(postivebtn) { dialog, which ->
                dialog.dismiss()
                callback.onResponce("yes")
            }.show()
    }

    @JvmStatic
    fun showDoubleButtonAlert(
        context: Context?,
        title: String?,
        message: String?,
        negTitle: String?,
        posTitle: String?,
        isCancelable: Boolean,
        callBack: FragmentCallBack
    ) {
        val dialog = Dialog(context!!)
        dialog.setCancelable(isCancelable)
        dialog.setContentView(R.layout.show_double_button_new_popup_dialog)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val tvtitle: TextView
        val tvMessage: TextView
        val tvPositive: TextView
        val tvNegative: TextView
        tvtitle = dialog.findViewById(R.id.tvtitle)
        tvMessage = dialog.findViewById(R.id.tvMessage)
        tvNegative = dialog.findViewById(R.id.tvNegative)
        tvPositive = dialog.findViewById(R.id.tvPositive)
        tvtitle.text = title
        tvMessage.text = message
        tvNegative.text = negTitle
        tvPositive.text = posTitle
        tvNegative.setOnClickListener {
            dialog.dismiss()
            val bundle = Bundle()
            bundle.putBoolean("isShow", false)
            callBack.onResponce(bundle)
        }
        tvPositive.setOnClickListener {
            dialog.dismiss()
            val bundle = Bundle()
            bundle.putBoolean("isShow", true)
            callBack.onResponce(bundle)
        }
        dialog.show()
    }


    @JvmStatic
    fun showGiftDailog(
        context: Context?,
        giftLink: String?
    ) {
        val dialog = Dialog(context!!)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.item_gift_show_dialog)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val profileImage = dialog.findViewById<SimpleDraweeView>(R.id.profileImage)
        if(giftLink?.isNotEmpty() == true) {
            profileImage.controller = frescoImageLoad(giftLink, profileImage, false)
            profileImage.visibility = View.VISIBLE
            val fadeInAnimation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
            profileImage.startAnimation(fadeInAnimation)

            val scaleAnimator = ObjectAnimator.ofFloat(profileImage, "scaleY", 0f, 1f).apply {
                duration = 800
                interpolator = AccelerateDecelerateInterpolator()
            }
            scaleAnimator.start()
        }

        CoroutineScope(Dispatchers.Main).launch {
            delay(2500)
            if(dialog!=null){
                dialog.dismiss()
            }
        }

        dialog.show()
    }


    var indeterminantDialog: Dialog? = null

    @JvmStatic
    fun showIndeterminentLoader(
        activity: Activity?,
        title: String?,
        outside_touch: Boolean,
        cancleable: Boolean
    ) {
        try {
            if (indeterminantDialog != null) {
                cancelIndeterminentLoader()
                indeterminantDialog = null
            }
            activity?.runOnUiThread {
                indeterminantDialog = Dialog(activity)
                indeterminantDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
                indeterminantDialog!!.setContentView(R.layout.item_indeterminant_progress_layout)
                indeterminantDialog!!.window!!.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        activity,
                        R.drawable.d_round_white_background
                    )
                )
                val tvTitle = indeterminantDialog!!.findViewById<TextView>(R.id.tvTitle)
                if (title != null && TextUtils.isEmpty(title)) {
                    tvTitle.text = title
                }
                if (!outside_touch) {
                    indeterminantDialog!!.setCanceledOnTouchOutside(false)
                }
                if (!cancleable) {
                    indeterminantDialog!!.setCancelable(false)
                }
                indeterminantDialog!!.show()
            }
        } catch (e: Exception) {
            Functions.printLog(Constants.tag, "Exception: $e")
        }
    }

    @JvmStatic
    fun cancelIndeterminentLoader() {
        try {
            if (indeterminantDialog != null || indeterminantDialog!!.isShowing) {
                indeterminantDialog!!.cancel()
            }
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception : $e")
        }
    }

    var determinantDialog: Dialog? = null
    var determinantProgress: ProgressBar? = null
    var ptext: TextView? = null

    @JvmStatic
    fun showDeterminentLoader(
        activity: Activity?,
        outside_touch: Boolean,
        cancleable: Boolean
    ) {
        try {
            if (determinantDialog != null) {
                cancelDeterminentLoader()
                determinantDialog = null
                ptext = null
            }
            activity?.runOnUiThread {
                determinantDialog = Dialog(activity)
                determinantDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
                determinantDialog!!.setContentView(R.layout.item_determinant_progress_layout)
                determinantDialog!!.window!!.setBackgroundDrawable(
                    ContextCompat.getDrawable(
                        activity,
                        R.drawable.d_round_white_background
                    )
                )
                ptext = determinantDialog!!.findViewById(R.id.ptext)
                determinantProgress = determinantDialog!!.findViewById(R.id.pbar)
                ptext!!.setText("0%")
                determinantProgress!!.setProgress(1)
                if (!outside_touch) {
                    determinantDialog!!.setCanceledOnTouchOutside(false)
                }
                if (!cancleable) {
                    determinantDialog!!.setCancelable(false)
                }
                determinantDialog!!.show()
            }
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception: $e")
        }
    }

    @JvmStatic
    fun showLoadingProgress(progress: Int) {
        if (determinantProgress != null && ptext != null) {
            if (progress >= 0 && progress <= 100) {
                determinantProgress!!.progress = progress
                ptext!!.text = "$progress%"
            }
        }
    }

    @JvmStatic
    fun cancelDeterminentLoader() {
        try {
            if (determinantDialog != null || determinantDialog!!.isShowing) {
                determinantDialog!!.cancel()
            }
        } catch (e: Exception) {
            Log.d(Constants.tag, "Exception : $e")
        }
    }


    @JvmStatic
    fun showValidationMsg(activity: Activity, containerView: View?, message: String?) {
        val layout = activity.layoutInflater.inflate(R.layout.validation_message_view, null)
        val tvMessage = layout.findViewById<TextView>(R.id.tvMessage)
        tvMessage.text = message
        val snackbar = Snackbar.make(
            containerView!!, "", Snackbar.LENGTH_LONG
        )
        val snackbarLayout = snackbar.view as Snackbar.SnackbarLayout
        val textView = snackbarLayout.findViewById<View>(R.id.snackbar_text) as TextView
        textView.visibility = View.INVISIBLE
        val params = snackbar.view.layoutParams
        if (params is CoordinatorLayout.LayoutParams) {
            params.gravity = Gravity.TOP
        } else {
            (params as FrameLayout.LayoutParams).gravity = Gravity.TOP
        }
        snackbarLayout.setPadding(0, 0, 0, 0)
        snackbarLayout.addView(layout, 0)
        snackbar.view.visibility = View.INVISIBLE
        snackbar.addCallback(object : Snackbar.Callback() {
            override fun onShown(sb: Snackbar) {
                super.onShown(sb)
                snackbar.view.visibility = View.VISIBLE
            }
        })
        CoroutineScope(Dispatchers.Main).launch {
            delay(1000)
            snackbar.view.visibility = View.INVISIBLE
        }

        snackbar.setDuration(Snackbar.LENGTH_LONG)
        snackbar.show()
        layout.setOnClickListener {
            snackbar.dismiss()
            snackbar.view.visibility = View.INVISIBLE
        }
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

    fun showError(activity: Activity?, msg: String) {
        CookieBar.build(activity)
            .setCustomView(R.layout.custom_error)
            .setCustomViewInitializer { view ->
                val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
                tvTitle.text = "" + msg
            }
            .setEnableAutoDismiss(false)
            .setSwipeToDismiss(false)
            .setDuration(4000)
            .show()
    }

    fun showSuccess(activity: Activity?, msg: String) {
        CookieBar.build(activity)
            .setCustomView(R.layout.custom_success)
            .setCustomViewInitializer { view ->
                val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
                tvTitle.text = "" + msg
            }
            .setDuration(4000)
            .setEnableAutoDismiss(false)
            .setSwipeToDismiss(false)
            .setCookiePosition(Gravity.TOP)
            .show()
    }


    var invitationcCookieBar: CookieBar? = null
    fun showInvitationDialog(activity: Activity?, userName: String, callBack: FragmentCallBack) {
        invitationcCookieBar = CookieBar.build(activity)
            .setCustomView(R.layout.item_speaker_alert)
            .setCustomViewInitializer { view ->
                val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
                val tabMaybeLater = view.findViewById<LinearLayout>(R.id.tabMaybeLater)
                val tabJoinAsSpeaker = view.findViewById<LinearLayout>(R.id.tabJoinAsSpeaker)
                val s =
                    "&#128075; <b>" + userName + "</b> " + view.context.getString(R.string.invited_you_to_join_as_a_speaker)
                tvTitle.text = Html.fromHtml(s, Html.FROM_HTML_MODE_LEGACY)

                tabMaybeLater.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putBoolean("isShow", false)
                    callBack.onResponce(bundle)
                    CookieBar.dismiss(activity)
                }
                tabMaybeLater.applyClickShrink()

                tabJoinAsSpeaker.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putBoolean("isShow", true)
                    callBack.onResponce(bundle)
                    CookieBar.dismiss(activity)
                }
                tabJoinAsSpeaker.applyClickShrink()
            }
            .setDuration(3600000)
            .setSwipeToDismiss(true)
            .show()
    }

    fun closeInvitationCookieBar(activity: Activity?) {
        if (invitationcCookieBar != null) {
            CookieBar.dismiss(activity)
        }
    }


}