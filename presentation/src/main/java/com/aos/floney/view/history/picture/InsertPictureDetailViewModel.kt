package com.aos.floney.view.history.picture

import androidx.lifecycle.viewModelScope
import com.aos.data.util.CurrencyUtil
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.model.home.ImageUrls
import com.aos.usecase.subscribe.SubscribeDeleteCloudImageUseCase
import com.bumptech.glide.load.ImageHeaderParser.ImageType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InsertPictureDetailViewModel @Inject constructor(
    private val subscribeDeleteCloudImageUseCase : SubscribeDeleteCloudImageUseCase
): BaseViewModel() {

    private var _onClickedBack = MutableEventFlow<Boolean>()
    val onClickedBack: EventFlow<Boolean> get() = _onClickedBack

    private var _onClickedDelete = MutableEventFlow<Boolean>()
    val onClickedDelete: EventFlow<Boolean> get() = _onClickedDelete


    private var _onDeleteComplete = MutableEventFlow<Boolean>()
    val onDeleteComplete: EventFlow<Boolean> get() = _onDeleteComplete

    private lateinit var imageUrl : ImageUrls

    fun imgDelete() {
        viewModelScope.launch(Dispatchers.IO) {
            subscribeDeleteCloudImageUseCase(
                imageUrl.id
            ).onSuccess {
                _onDeleteComplete.emit(true)
            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@InsertPictureDetailViewModel)))
            }
        }
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