package com.aos.floney.util

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
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

        loadingDialog = Dialog(activity).apply {
            setCancelable(false)
            setContentView(R.layout.layout_circle_loading)
            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
                )
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