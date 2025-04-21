package com.aos.floney.util
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.aos.floney.R
import com.aos.floney.view.common.BaseAlertDialog
import com.aos.floney.view.common.WarningPopupDialog

object PermissionUtil {
    const val REQUEST_GALLERY_PERMISSION = 1001

    fun hasGalleryPermission(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestGalleryPermission(activity: Activity) {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        ActivityCompat.requestPermissions(activity, permissions, REQUEST_GALLERY_PERMISSION)
    }

    fun checkAndRequestGalleryPermission(
        activity: Activity,
        fragmentManager: FragmentManager,
        onGranted: () -> Unit
    ) {
        if (hasGalleryPermission(activity)) {
            onGranted()
        } else {
            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }

            val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)

            if (shouldShowRationale) {
                requestGalleryPermission(activity)
            } else {
                // 이미 요청했었고, '다시 묻지 않기'가 체크된 경우
                showPermissionSettingDialog(activity, fragmentManager)
            }
        }
    }

    private fun showPermissionSettingDialog(context: Context, fragmentManager: FragmentManager) {

        val exitDialogFragment = WarningPopupDialog(
            title = "사진 권한 필요",
            info = "사진을 불러오려면 권한이 필요해요.\n" +
                    "설정 화면으로 이동할까요?",
            leftButton = "설정으로 이동",
            rightButton = "닫기",
            check = true
        ) { checked ->
            if (checked) {
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            }
        }
        exitDialogFragment.show(fragmentManager, "WithdrawDialog")
    }
}