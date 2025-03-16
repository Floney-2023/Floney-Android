package com.aos.floney.view.history.picture

import androidx.lifecycle.viewModelScope
import com.aos.floney.base.BaseViewModel
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.model.home.ImageUrls
import com.bumptech.glide.load.ImageHeaderParser.ImageType
import kotlinx.coroutines.launch
import javax.inject.Inject

class InsertPictureDetailViewModel @Inject constructor(

): BaseViewModel() {

    private var _onClickedBack = MutableEventFlow<Boolean>()
    val onClickedBack: EventFlow<Boolean> get() = _onClickedBack

    private var _onClickedDelete = MutableEventFlow<Boolean>()
    val onClickedDelete: EventFlow<Boolean> get() = _onClickedDelete

    private lateinit var imageUrl : ImageUrls

    private var imageType = ""

    fun imgDelete() {

    }

    fun onClickedBack() {
        viewModelScope.launch {
            _onClickedBack.emit(true)
        }
    }

    fun onClickedDelete() {
        viewModelScope.launch {
            _onClickedDelete.emit(true)
        }
    }

    fun setImageUrl(url: ImageUrls) {
        imageUrl = url
    }

    fun getImageUrl(): ImageUrls {
        return imageUrl
    }
}