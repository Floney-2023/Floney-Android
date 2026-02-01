package com.aos.floney.ext

import android.app.Activity
import android.os.Build
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.aos.floney.R

fun Activity.applyHistoryOpenTransition() {
    overridePendingTransition(
        R.anim.slide_in,
        R.anim.none
    )
}

fun Activity.applyHistoryCloseTransition() {
    if (Build.VERSION.SDK_INT >= 34) {
        overrideActivityTransition(
            Activity.OVERRIDE_TRANSITION_CLOSE,
            R.anim.none,
            R.anim.slide_out // 슬라이드 다운
        )
    } else {
        overridePendingTransition(
            R.anim.none,
            R.anim.slide_out
        )
    }
}

fun Activity.applyOpenTransition() {
    overridePendingTransition(
        android.R.anim.fade_in,
        android.R.anim.fade_out
    )
}

fun Activity.applyCloseTransition() {
    if (Build.VERSION.SDK_INT >= 34) {
        overrideActivityTransition(
            Activity.OVERRIDE_TRANSITION_CLOSE,
            android.R.anim.fade_in,
            android.R.anim.fade_out
        )
    } else {
        overridePendingTransition(
            android.R.anim.fade_in,
            android.R.anim.fade_out
        )
    }
}

fun AppCompatActivity.setStatusBarTransparent() {

    window.apply {
        setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        }
    }

    if (Build.VERSION.SDK_INT >= 30) {    // API 30 에 적용
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}