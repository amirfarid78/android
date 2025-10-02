package com.coheser.app.simpleclasses

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.coheser.app.R

class MaxHeightLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var maxHeight: Int = 0

    init {
        // Initialize the max height attribute from XML if needed
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.MaxHeightLinearLayout,
            0, 0
        ).apply {
            try {
                maxHeight = getDimensionPixelSize(R.styleable.MaxHeightLinearLayout_maxHeight, 0)
            } finally {
                recycle()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightSpec = heightMeasureSpec

        // If a max height is set, use it
        if (maxHeight > 0) {
            val measuredHeight = MeasureSpec.getSize(heightMeasureSpec)
            if (measuredHeight > maxHeight) {
                heightSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST)
            }
        }

        super.onMeasure(widthMeasureSpec, heightSpec)
    }

    fun setMaxHeight(height: Int) {
        this.maxHeight = height
        requestLayout()
    }
}
