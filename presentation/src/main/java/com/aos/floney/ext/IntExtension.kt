package com.aos.floney.ext

import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.net.Uri

fun Int.getResourceUri(context: Context): String {
    return context.resources.let {
        Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(it.getResourcePackageName(this))		// it : resources, this : ResId(Int)
            .appendPath(it.getResourceTypeName(this))		// it : resources, this : ResId(Int)
            .appendPath(it.getResourceEntryName(this))		// it : resources, this : ResId(Int)
            .build()
            .toString()
    }
}

fun Int.DpToPx(): Int {
    return (this * Resources.getSystem().displayMetrics.density).toInt()
}