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
import com.aos.data.util.SubscriptionDataStoreUtil
import com.aos.model.user.MyBooks
import com.aos.usecase.mypage.RecentBookkeySaveUseCase
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.exp

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val subscriptionDataStoreUtil: SubscriptionDataStoreUtil
): BaseViewModel() {

    // 내역추가
    private var _clickedAddHistory = MutableEventFlow<String>()
    val clickedAddHistory: EventFlow<String> get() = _clickedAddHistory

    // 구독 유도 팝업 표시 여부
    var subscribePopupShow = MutableLiveData<Boolean>(false)

    // 탭바로 추가할 경우
    fun onClickTabAddHistory() {
        viewModelScope.launch {
            val expiredCheck = subscriptionDataStoreUtil.getSubscribeExpired().first()

            if (expiredCheck) // 만료되었는데 혜택 적용 되어있다면, 팝업 표시
                showSubscribePopupIfNeeded(true)
            else // 아니라면 내역 추가 가능
                _clickedAddHistory.emit(setTodayDate())
        }
    }

    // 구독 만료 팝업 보이기 (화면 이동 막기)
    fun showSubscribePopupIfNeeded( isShow: Boolean) {
        subscribePopupShow.value = isShow
    }

    // 오늘 날짜로 calendar 설정하기
    private fun setTodayDate(): String {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return date
    }
}