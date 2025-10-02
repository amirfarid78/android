package com.coheser.app.simpleclasses

import android.view.View

class DebounceClickHandler(private val onClickListener: View.OnClickListener) : View.OnClickListener {

    companion object {
        private const val DEBOUNCE_INTERVAL = 500L // 500 milliseconds
    }

    private var lastClickTime: Long = 0

    override fun onClick(view: View) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime > DEBOUNCE_INTERVAL) {
            lastClickTime = currentTime
            onClickListener.onClick(view)
        }
    }
}