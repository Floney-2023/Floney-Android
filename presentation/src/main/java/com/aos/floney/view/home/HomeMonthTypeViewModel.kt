package com.aos.floney.view.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorMsg
import com.aos.model.home.ExtData
import com.aos.model.home.MonthMoney
import com.aos.usecase.home.GetMoneyHistoryMonthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeMonthTypeViewModel @Inject constructor(
    private val prefs: SharedPreferenceUtil,
    private val getMoneyHistoryMonthUseCase: GetMoneyHistoryMonthUseCase
) : BaseViewModel() {

    private var _getCalendarList = MutableLiveData<List<MonthMoney>>()
    val getCalendarList: LiveData<List<MonthMoney>> get() = _getCalendarList

    private var _getExtData = MutableLiveData<ExtData>()
    val getExtData: LiveData<ExtData> get() = _getExtData

    fun getBookMonth(_date: String) {
        val date = "${_date.substring(0, 7)}-01"
        viewModelScope.launch {
            getMoneyHistoryMonthUseCase(bookKey = prefs.getString("bookKey", ""), date = date).onSuccess {
                Timber.e("_getCalendarList ${it.data}")
                _getCalendarList.postValue(it.data)
                _getExtData.postValue(it.extData)

            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@HomeMonthTypeViewModel)))
            }
        }
    }

}