package com.aos.floney.view.history.picture

import androidx.lifecycle.viewModelScope
import com.aos.floney.base.BaseViewModel
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.model.home.ImageUrls
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InsertPictureDetailViewModel @Inject constructor(
) : BaseViewModel() {

    private var _onClickedBack = MutableEventFlow<Boolean>()
    val onClickedBack: EventFlow<Boolean> get() = _onClickedBack

    private var _onClickedDelete = MutableEventFlow<Boolean>()
    val onClickedDelete: EventFlow<Boolean> get() = _onClickedDelete

    private var imageList: List<ImageUrls> = emptyList()
    private var currentIndex: Int = 0

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

    fun setImageList(list: List<ImageUrls>, startIndex: Int) {
        imageList = list
        currentIndex = startIndex
    }

    fun setCurrentIndex(index: Int) {
        currentIndex = index
    }

    fun getImage(index: Int): ImageUrls = imageList[index]

    fun getCurrentImage(): ImageUrls = imageList[currentIndex]
}
