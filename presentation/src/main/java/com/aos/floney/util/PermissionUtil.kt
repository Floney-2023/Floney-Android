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
    // Keep this constant for any other permission handling that might need it elsewhere in the app
    const val REQUEST_GALLERY_PERMISSION = 1001
    // No longer need gallery permission methods as we're using PhotoPicker
    // Any other permission methods can still be added here
}
