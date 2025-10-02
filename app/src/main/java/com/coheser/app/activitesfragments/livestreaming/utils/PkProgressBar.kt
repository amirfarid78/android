package com.coheser.app.activitesfragments.livestreaming.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.coheser.app.R

class PkProgressBar : View {
    var firstPercentage:Int = 50
    var secondPercentage:Int = 50


    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    fun getFirstSectionPercentage(): Int {
        return firstPercentage
    }

    fun setFirstSectionPercentage(percentage: Int) {
        this.firstPercentage = percentage
        invalidate() // invalidate the view to trigger a redraw
    }

    fun getSecondSectionPercentage(): Int {
        return secondPercentage
    }

    fun setSecondSectionPercentage(percentage: Int) {
        this.secondPercentage = percentage
        invalidate() // invalidate the view to trigger a redraw
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val firstSectionWidth = width * firstPercentage / 100
        val secondSectionWidth = width * secondPercentage / 100
        // draw the first section
        val paint = Paint()
        paint.color = context.resources.getColor(R.color.p_color)
        canvas.drawRect(0f, 0f, firstSectionWidth.toFloat(), height.toFloat(), paint)

        // draw the second section
        paint.color = context.resources.getColor(R.color.k_color)
        canvas.drawRect(
            firstSectionWidth.toFloat(),
            0f,
            (firstSectionWidth + secondSectionWidth).toFloat(),
            height.toFloat(),
            paint
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
    }
}