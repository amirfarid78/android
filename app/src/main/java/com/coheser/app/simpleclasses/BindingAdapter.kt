package com.coheser.app.simpleclasses


import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableBoolean
import com.coheser.app.models.UserModel

object BindingAdapter {

    @JvmStatic
    @BindingAdapter("visibleIf")
    fun setVisibleIf(view: View, isVisible: Boolean) {
        view.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter("checkVisibility")
    fun checkVisibility(view: View, visible: ObservableBoolean) {
        view.visibility = if (visible.get()) View.VISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter("checkVisibility")
    fun checkVisibility(view: View, string: String?) {

        view.visibility = if (Functions.isStringHasValue(string)) View.VISIBLE else View.GONE
    }

    @JvmStatic
    @BindingAdapter("showCount")
    fun showCount(textView: TextView, count: String) {
       if(Functions.isStringHasValue(count)){
           textView.setText(Functions.getSuffix(count))
       }
        else{
           textView.setText("0")
        }
    }

    @JvmStatic
    @BindingAdapter("showFullName")
    fun showFullName(textView: TextView, userModel: UserModel?) {
        if(Functions.isStringHasValue(userModel?.first_name) && Functions.isStringHasValue(userModel?.last_name)){
            textView.setText(userModel?.first_name+userModel?.last_name)
        }
        else{
            textView.setText(userModel?.username)
        }
    }

    @JvmStatic
    @BindingAdapter("showUserName")
    fun showUserName(textView: TextView, username:String?) {
        textView.setText("@"+username)
    }


    @JvmStatic
    @BindingAdapter("showOrHide")
    fun showOrHide(textView: TextView, string: String?) {
        if(Functions.isStringHasValue(string)){
            textView.visibility=View.VISIBLE
            textView.text = string
        }
        else{
            textView.visibility=View.GONE
        }
    }


}