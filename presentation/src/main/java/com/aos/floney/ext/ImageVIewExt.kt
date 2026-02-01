package com.aos.floney.ext

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.R
import com.aos.floney.view.home.HomeActivity
import com.aos.model.analyze.UiAnalyzeAssetModel
import com.aos.model.analyze.UiAnalyzePlanModel
import com.aos.model.subscribe.PictureItem
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import timber.log.Timber
import java.io.File

@BindingAdapter("setImageToUrl")
fun ImageView.setImageToUrl(url: String?) {
    url?.let {
        if(it.equals("user_default")) {
            Glide.with(this)
                .load(R.drawable.icon_default_profile)
                .into(this)
        } else if(it.equals("book_default")) {
            Glide.with(this)
                .load(R.drawable.icon_book_profile)
                .into(this)
        } else if(it.equals("user_book_default")) {
            Glide.with(this)
                .load(R.drawable.btn_profile)
                .into(this)
        } else if(it.equals("btn_book_default")) {
            Glide.with(this)
                .load(R.drawable.btn_book_profile)
                .into(this)
        } else {
            Glide.with(this)
                .load(url)
                .format(DecodeFormat.PREFER_RGB_565)
                .into(this)
        }
    }
}


@BindingAdapter("setBookImageToUrl")
fun ImageView.setBookImageToUrl(url: String?) {
    if (url.isNullOrEmpty() || url == "btn_book_default" || url == "book_default") {
        // 기본 이미지 설정
        val drawable = if (url == "btn_book_default") R.drawable.btn_book_profile else R.drawable.icon_book_profile
        Glide.with(this)
            .load(drawable)
            .into(this)
    } else {
        // URL로부터 이미지 로드
        Glide.with(this)
            .load(url)
            .format(DecodeFormat.PREFER_RGB_565)
            .error(R.drawable.icon_book_profile)
            .into(this)
    }
}

@BindingAdapter("setProfileStatus")
fun CardView.setProfileStatus(status: Boolean) {
    val prefs = SharedPreferenceUtil(context)
    if (!prefs.getBoolean("seeProfileStatus", status)) {
        this.visibility = View.GONE
    } else {
        this.visibility = View.VISIBLE
    }
}
@BindingAdapter("setUserImageToUrl")
fun ImageView.setUserImageToUrl(url: String?) {

    val prefs = SharedPreferenceUtil(context)
    url?.let {
        if(!prefs.getBoolean("seeProfileStatus",true) || it.equals("user_default")) {
            Glide.with(this)
                .load(R.drawable.icon_default_profile)
                .into(this)
        } else if(it.equals("user_btn_default")) {
            Glide.with(this)
                .load(R.drawable.btn_profile)
                .into(this)
        } else {
            Glide.with(this)
                .load(url)
                .error(R.drawable.icon_default_profile)
                .format(DecodeFormat.PREFER_RGB_565)
                .into(this)
        }
    }
}

@BindingAdapter("setAlarmImage")
fun ImageView.setAlarmImage(url: String?) {
    url?.let {
        if(it.equals("icon_noti_currency")) {
            Glide.with(this)
                .load(R.drawable.icon_alarm_currrency)
                .into(this)
        } else if(it.equals("icon_noti_exit")) {
            Glide.with(this)
                .load(R.drawable.icon_noti_exit)
                .into(this)
        } else if(it.equals("icon_noti_join")) {
            Glide.with(this)
                .load(R.drawable.icon_noti_join)
                .into(this)
        } else if(it.equals("icon_noti_reset")) {
            Glide.with(this)
                .load(R.drawable.icon_noti_reset)
                .into(this)
        } else if(it.equals("icon_noti_settlement")) {
            Glide.with(this)
                .load(R.drawable.icon_noti_settlement)
                .into(this)
        }else {
            Glide.with(this)
                .load(url)
                .into(this)
        }
    }
}

@BindingAdapter("setAnalyzeImage")
fun ImageView.setAnalyzeImage(item: UiAnalyzePlanModel?) {
    if(item != null) {
        if(item.initBudget.substring(0, item.initBudget.length - 1) == "0") {
            Glide.with(this)
                .load(R.drawable.analyze_plan_0_49_icon)
                .into(this)
        } else {
            when(item.percent.toInt()) {
                in 0..30 ->{
                    Glide.with(this)
                        .load(R.drawable.analyze_plan_0_49_icon)
                        .into(this)
                }
                in 31..60 -> {
                    Glide.with(this)
                        .load(R.drawable.analyze_plan_50_79_icon)
                        .into(this)
                }
                in 61..100 -> {
                    Glide.with(this)
                        .load(R.drawable.analyze_plan_80_99_icon)
                        .into(this)
                }
            }
        }
    } else {
        Glide.with(this)
            .load(R.drawable.analyze_plan_0_49_icon)
            .into(this)
    }
}

@BindingAdapter("setAnalyzeAssetImage")
fun ImageView.setAnalyzeAssetImage(item: UiAnalyzeAssetModel?) {
    if (item != null) {
        val isNegative = item.difference.takeLast(5) == "감소했어요"

        when {
            isNegative -> {
                Glide.with(this)
                    .load(R.drawable.asset_down_icon)
                    .into(this)
            }
            !isNegative -> {
                Glide.with(this)
                    .load(R.drawable.asset_up_icon)
                    .into(this)
            }
            else -> {
                Glide.with(this)
                    .load(R.drawable.asset_up_icon)
                    .into(this)
            }
        }
    } else {
        Glide.with(this)
            .load(R.drawable.asset_up_icon)
            .into(this)
    }
}

@BindingAdapter("bind:setImageColor")
fun ImageView.setImageColor(color: Int) {
    this.setBackgroundColor(color)
}

@BindingAdapter(value = ["bind:setPictureImage", "imageIndex"], requireAll = true)
fun ImageView.setPictureImage(files: List<File>, num: Int) {
    Glide.with(this)
        .load(files[num])
        .fitCenter()
        .centerCrop()
        .into(this)
}

@BindingAdapter("setSortedImage")
fun ImageView.setSortedImage(picture: PictureItem?) {
    if (picture == null) {
        resetPictureImage(this)
        return
    }

    when (picture) {
        is PictureItem.CloudImage -> {
            setPictureImageFromUrl(this, picture.imageUrls.url)
        }
        is PictureItem.LocalImage -> {
            setPictureImage(this, picture.file)
        }
    }
}

private fun resetPictureImage(imageView: ImageView) {
    Glide.with(imageView.context).clear(imageView)
    imageView.setImageDrawable(null)
}

private fun setPictureImageFromUrl(imageView: ImageView, url: String) {
    Timber.i("url ${url}")
    Glide.with(imageView)
        .load(url)
        .thumbnail(Glide.with(imageView).load(url))
        .fitCenter()
        .centerCrop()
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .skipMemoryCache(true)
        .into(imageView)
}

private fun setPictureImage(imageView: ImageView, file: File) {
    Glide.with(imageView)
        .load(file)
        .thumbnail(Glide.with(imageView).load(file))
        .fitCenter()
        .centerCrop()
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .skipMemoryCache(true)
        .into(imageView)
}
