package com.aos.floney.ext

import android.app.Activity
import android.os.Build
import com.aos.floney.R

fun Activity.applyHistoryOpenTransition() {
    if (Build.VERSION.SDK_INT >= 34) {
        overrideActivityTransition(
            Activity.OVERRIDE_TRANSITION_OPEN,
            R.anim.slide_in,         // 아래에서 위로
            R.anim.slide_out_down    // 안보이는 백그라운드용 (빈 애니메이션)
        )
    } else {
        overridePendingTransition(
            R.anim.slide_in,
            R.anim.slide_out_down
        )
    }
}

fun Activity.applyHistoryCloseTransition() {
    if (Build.VERSION.SDK_INT >= 34) {
        overrideActivityTransition(
            Activity.OVERRIDE_TRANSITION_CLOSE,
            R.anim.slide_in,         // 다시 아래로 내려감
            R.anim.slide_out_down
        )
    } else {
        overridePendingTransition(
            R.anim.slide_in,
            R.anim.slide_out_down
        )
    }
}


fun Activity.applyOpenTransition() {
    if (Build.VERSION.SDK_INT >= 34) {
        overrideActivityTransition(
            Activity.OVERRIDE_TRANSITION_OPEN,
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
