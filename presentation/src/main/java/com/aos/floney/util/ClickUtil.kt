package com.aos.floney.util

object ClickUtil {
    var lastClickTime = 0L

    inline fun debounceClick(minInterval: Long = 1000L, block: () -> Unit) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime < minInterval) return
        lastClickTime = currentTime
        block()
    }
}
