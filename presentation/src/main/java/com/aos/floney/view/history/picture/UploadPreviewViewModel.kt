package com.aos.floney.view.history.picture

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aos.floney.base.BaseViewModel
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.model.home.ImageUrls
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UploadPreviewViewModel @Inject constructor() : BaseViewModel() {

    private val _images = MutableLiveData<List<Uri>>()
    val images: LiveData<List<Uri>> get() = _images

    private var _onClickedBack = MutableEventFlow<Boolean>()
    val onClickedBack: EventFlow<Boolean> get() = _onClickedBack

    private var _onClickedUpload = MutableEventFlow<Boolean>()
    val onClickedUpload: EventFlow<Boolean> get() = _onClickedUpload

    fun setImages(uris: List<Uri>) {
        _images.value = uris
    }

    fun onClickedBack() {
        viewModelScope.launch {
            _onClickedBack.emit(true)
        }
    }

    fun onClickedUpload() {
        viewModelScope.launch {
            _onClickedUpload.emit(true)
        }
    }

}
