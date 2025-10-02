package com.coheser.app.simpleclasses

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import com.coheser.app.interfaces.FragmentCallBack
import com.hendraanggrian.appcompat.widget.SocialEditText

class Typewriter @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : SocialEditText(context, attrs) {

    private var mText: CharSequence? = null
    private var mIndex = 0
    private var mDelay: Long = 150 // Default 150ms delay
    private var callback: FragmentCallBack? = null
    private val mHandler = Handler()

    private val characterAdder = object : Runnable {
        override fun run() {
            setText(mText?.subSequence(0, mIndex++))
            if (mIndex <= mText!!.length) {
                mHandler.postDelayed(this, mDelay)
            } else if (callback != null) {
                callback!!.onResponce(null)
            }
        }
    }

    fun animateText(text: CharSequence) {
        mText = text
        mIndex = 0

        setText("")
        mHandler.removeCallbacks(characterAdder)
        mHandler.postDelayed(characterAdder, mDelay)
    }

    fun setCharacterDelay(millis: Long, fragmentCallBack: FragmentCallBack) {
        mDelay = millis
        callback = fragmentCallBack
    }
}
