package com.aos.floney.view.book.add

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.R
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.floney.util.getCurrentDateTimeString
import com.aos.model.book.UiBookSettingModel
import com.aos.usecase.bookadd.BooksJoinUseCase
import com.aos.usecase.booksetting.BooksSettingGetUseCase
import com.aos.usecase.mypage.AlarmSaveGetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookAddInviteSuccessViewModel @Inject constructor(
    private val app: Application,
    private val prefs: SharedPreferenceUtil,
    private val booksSettingGetUseCase : BooksSettingGetUseCase,
    private val alarmSaveGetUseCase : AlarmSaveGetUseCase
): BaseViewModel() {

    // 가계부 정보
    private var _bookSettingInfo = MutableLiveData<UiBookSettingModel>()
    val bookSettingInfo: LiveData<UiBookSettingModel> get() = _bookSettingInfo

    // 다음 페이지 이동
    private var _nextPage = MutableEventFlow<Boolean>()
    val nextPage: EventFlow<Boolean> get() = _nextPage


    // 정보 load 완료
    private var _getInform = MutableEventFlow<Boolean>()
    val getInform: EventFlow<Boolean> get() = _getInform

    init {
        searchBookSettingItems()
    }
    fun searchBookSettingItems()
    {
        viewModelScope.launch(Dispatchers.IO) {
            booksSettingGetUseCase(prefs.getString("bookKey","")).onSuccess {
                // me가 true인 항목이 맨 앞에 오도록 정렬
                val sortedList = it.ourBookUsers.sortedByDescending { it.me }
                _bookSettingInfo.postValue(it.copy(ourBookUsers = sortedList))
                _getInform.emit(true)
            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg()))
            }
        }
    }
    fun saveInviteAlarm(){
        viewModelScope.launch {
            if(prefs.getString("bookKey","").isNotEmpty()) {
                baseEvent(Event.ShowLoading)
                bookSettingInfo.value?.ourBookUsers?.map {
                    alarmSaveGetUseCase(
                        prefs.getString("bookKey",""),
                        app.getString(R.string.notification_title),
                        app.getString(R.string.notification_user_joined,
                            _bookSettingInfo.value!!.ourBookUsers[0].name,
                            _bookSettingInfo.value!!.bookName),
                        "icon_noti_join",
                        it.email,
                        getCurrentDateTimeString()
                    ).onSuccess {
                        baseEvent(Event.HideLoading)
                    }.onFailure {
                        baseEvent(Event.HideLoading)
                        baseEvent(Event.ShowToast(it.message.parseErrorMsg()))
                    }
                }

            }
        }
    }
    // 가계부에 초대됨 -> 홈 화면으로 이동
    fun onClickGoHomeActivity() {
        viewModelScope.launch {
            _nextPage.emit(true)
        }
    }
}