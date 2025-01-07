package com.aos.floney.view.history.memo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aos.data.util.CurrencyUtil
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.formatNumber
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class InsertMemoViewModel @Inject constructor(): BaseViewModel() {

    var insertMemoValue = MutableLiveData<String>()

    private var _onClickedSaveWriting = MutableEventFlow<String>()
    val onClickedSaveWriting: EventFlow<String> get() = _onClickedSaveWriting

    private var _onClickedBack = MutableEventFlow<Boolean>()
    val onClickedBack: EventFlow<Boolean> get() = _onClickedBack

    // 작성 저장하기 버튼 클릭
    fun onClickedSaveWriting() {
        viewModelScope.launch {
            _onClickedSaveWriting.emit(insertMemoValue.value.toString())
        }
    }

    fun onClickedBack() {
        viewModelScope.launch {
            _onClickedBack.emit(true)
        }
    }
}
