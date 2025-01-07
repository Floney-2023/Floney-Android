package com.aos.floney.view.history.memo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aos.data.util.CurrencyUtil
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.formatNumber
import javax.inject.Inject

class InsertMemoViewModel @Inject constructor(): BaseViewModel() {

    private var _insertMemoValue = MutableLiveData<String>()
    val insertMemoValue: LiveData<String> get() = _insertMemoValue
}