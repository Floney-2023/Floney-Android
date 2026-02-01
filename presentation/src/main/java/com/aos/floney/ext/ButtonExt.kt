package com.aos.floney.ext

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.view.MotionEvent
import android.widget.Button

@SuppressLint("ClickableViewAccessibility")
fun Button.setupTouchEffect() {
    val originalColor = this.currentTextColor
    val fadedColor = originalColor.withAlpha(100)

    this.setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                animateTextColorChange(this, originalColor, fadedColor)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                animateTextColorChange(this, fadedColor, originalColor)
            }
        }
        false
    }
}

private fun animateTextColorChange(button: Button, fromColor: Int, toColor: Int) {
    ValueAnimator.ofArgb(fromColor, toColor).apply {
        duration = 150
        addUpdateListener { animator ->
            button.setTextColor(animator.animatedValue as Int)
        }
        start()
    }
}

fun Int.withAlpha(alpha: Int): Int {
    return (this and 0x00FFFFFF) or (alpha shl 24)
}