package com.aos.floney.view.mypage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aos.data.util.CommonUtil
import com.aos.floney.R
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.model.user.UiMypageSearchModel
import com.aos.usecase.mypage.MypageSearchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.aos.data.util.SharedPreferenceUtil
import com.aos.model.user.MyBooks
import com.aos.usecase.mypage.RecentBookkeySaveUseCase
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltViewModel
class MyPageViewModel @Inject constructor(
): BaseViewModel() {

    // 내역추가
    private var _clickedAddHistory = MutableEventFlow<String>()
    val clickedAddHistory: EventFlow<String> get() = _clickedAddHistory


    // 탭바로 추가할 경우
    fun onClickTabAddHistory() {
        viewModelScope.launch {
            _clickedAddHistory.emit(setTodayDate())
        }
    }

    // 오늘 날짜로 calendar 설정하기
    private fun setTodayDate(): String {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return date
    }

}