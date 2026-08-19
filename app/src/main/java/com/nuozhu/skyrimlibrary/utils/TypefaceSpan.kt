package com.nuozhu.skyrimlibrary.utils

import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.MetricAffectingSpan

class TypefaceSpan(private val typeface: Typeface) : MetricAffectingSpan() {

    override fun updateDrawState(ds: TextPaint) {
        applyCustomTypeface(ds)
    }

    override fun updateMeasureState(paint: TextPaint) {
        applyCustomTypeface(paint)
    }

    private fun applyCustomTypeface(paint: TextPaint) {
        val oldTypeface = paint.typeface
        val oldStyle = oldTypeface?.style ?: 0

        val fakeStyle = oldStyle and typeface.style.inv()

        if (fakeStyle and Typeface.BOLD != 0) {
            paint.isFakeBoldText = true
        }

        if (fakeStyle and Typeface.ITALIC != 0) {
            paint.textSkewX = -0.25f
        }

        paint.typeface = typeface
    }
}