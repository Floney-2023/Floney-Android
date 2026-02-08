package com.aos.floney.view.book.setting.asset

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.aos.data.util.CurrencyUtil
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.R
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.formatNumber
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.usecase.analyze.PostAnalyzeAssetUseCase
import com.aos.usecase.booksetting.BooksCodeCheckUseCase
import com.aos.usecase.booksetting.BooksInfoAssetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@HiltViewModel
class BookSettingAssetViewModel @Inject constructor(
    private val application: Application,
    private val prefs: SharedPreferenceUtil,
    private val booksInfoAssetUseCase: BooksInfoAssetUseCase,
    private val postAnalyzeAssetUseCase: PostAnalyzeAssetUseCase
): BaseViewModel() {

    // 가계부 초대 코드
    var inviteCode = MutableLiveData("")

    // 초기 자산 설정
    private var _initAssetSheet = MutableEventFlow<Boolean>()
    val initAssetSheet: EventFlow<Boolean> get() = _initAssetSheet

    // 금액
    var cost = MutableLiveData("")

    private val mutex = Mutex()

    init {
        settingInitAsset()
    }

    // 초기 자산 불러오기
    fun settingInitAsset() {
        viewModelScope.launch {
            // Calendar 인스턴스 생성
            val calendar = Calendar.getInstance()
            // 현재 연도와 월을 가져옴
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH는 0부터 시작하므로 +1 해줌
            // "YYYY-MM" 형식으로 포맷
            val currentYearMonth = String.format("%d-%02d", year, month)
            postAnalyzeAssetUseCase(
                prefs.getString("bookKey",""), currentYearMonth).onSuccess {
                    if (it.initAsset.equals("0${CurrencyUtil.currency}"))
                        cost.postValue("")
                    else
                        cost.postValue(it.initAsset)
            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@BookSettingAssetViewModel)))
            }
        }
    }

    // 초기 자산 설정
    fun onClickSaveButton(){
        viewModelScope.launch {
            if (mutex.isLocked) return@launch
            mutex.withLock {
                booksInfoAssetUseCase(
                    prefs.getString("bookKey",""),
                    settingCost()).onSuccess {
                    baseEvent(Event.ShowSuccessToast(application.getString(R.string.toast_change_successed)))
                    _initAssetSheet.emit(true)
                }.onFailure {
                    baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@BookSettingAssetViewModel)))
                }
            }
        }
    }
    fun settingCost(): Long {
        if (cost.value=="")
            return 0
        else if (cost.value!!.length<=4)
            return cost.value!!.substring(0, cost.value!!.length-1).toLong()
        else if (cost.value!="")
            return cost.value!!.replace(",", "").replace(CurrencyUtil.currency,"").toLong()
        else
            return 0
    }
    // bottomsheet 내리기
    fun onClickedInitBtn(){
        cost.postValue("")
    }

    // 숫자 변화 감지
    fun onCostTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (count == 0) {
            cost.postValue("${s.toString().formatNumber()}")
        } else {
            cost.postValue("${s.toString().formatNumber()}${CurrencyUtil.currency}")
        }
    }
}