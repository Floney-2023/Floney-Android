package com.aos.floney.view.history.picture

import androidx.lifecycle.viewModelScope
import com.aos.floney.base.BaseViewModel
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class InsertPictureDetailViewModel @Inject constructor(): BaseViewModel() {

    private var _onClickedBack = MutableEventFlow<Boolean>()
    val onClickedBack: EventFlow<Boolean> get() = _onClickedBack

    private var _onClickedDelete = MutableEventFlow<Boolean>()
    val onClickedDelete: EventFlow<Boolean> get() = _onClickedDelete

    private var imageUrl = ""

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

    fun setImageUrl(url: String) {
        imageUrl = url
    }

    fun getImageUrl(): String {
        return imageUrl
    }

}