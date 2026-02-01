package com.aos.floney.ext

import android.content.Intent
import android.os.Build
import java.io.Serializable

fun <T: Serializable> Intent.intentSerializable(key: String, clazz: Class<T>): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getSerializableExtra(key, clazz)
    } else {
        @Suppress("DEPRECATION")
        this.getSerializableExtra(key) as T?
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : Serializable> Intent.intentSerializableList(key: String): ArrayList<T> {
    return this.getSerializableExtra(key) as? ArrayList<T>  ?: arrayListOf()
}