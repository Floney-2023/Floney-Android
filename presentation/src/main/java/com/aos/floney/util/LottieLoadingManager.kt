package com.aos.floney.util

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialog
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.aos.floney.R
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 스타일 로딩 애니메이션을 제공하는 싱글톤 매니저
 */
@Singleton
class LottieLoadingManager @Inject constructor() {

    private var loadingDialog: Dialog? = null

    /**
     * 로딩 애니메이션을 표시합니다.
     * @param activity 애니메이션을 표시할 액티비티
     */
    fun showLoading(activity: Activity) {
        if (loadingDialog?.isShowing == true) return

        loadingDialog = AppCompatDialog(activity).apply {
            window?.apply {
                setContentView(R.layout.layout_circle_loading)
                setCancelable(false)
                window?.setDimAmount(0f)
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
        }

        loadingDialog?.show()
    }

    /**
     * 로딩 애니메이션을 숨깁니다.
     */
    fun hideLoading() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }
}