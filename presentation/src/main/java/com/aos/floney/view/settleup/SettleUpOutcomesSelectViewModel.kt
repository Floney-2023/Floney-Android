package com.aos.floney.view.settleup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.R
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.floney.util.getAdvertiseCheck
import com.aos.floney.util.getAdvertiseTenMinutesCheck
import com.aos.floney.util.getCurrentDateTimeString
import com.aos.model.settlement.BookUsers
import com.aos.model.settlement.Outcomes
import com.aos.model.settlement.UiMemberSelectModel
import com.aos.model.settlement.UiOutcomesSelectModel
import com.aos.model.settlement.settleOutcomes
import com.aos.usecase.settlement.BooksOutComesUseCase
import com.aos.usecase.settlement.BooksUsersUseCase
import com.aos.usecase.subscribe.SubscribeCheckUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettleUpOutcomesSelectViewModel @Inject constructor(
    stateHandle: SavedStateHandle,
    private val prefs: SharedPreferenceUtil,
    private val booksOutComesUseCase : BooksOutComesUseCase,
    private val subscribeCheckUseCase: SubscribeCheckUseCase
): BaseViewModel() {


    var memberArray: LiveData<Array<String>> = stateHandle.getLiveData("member")
    var startDate: LiveData<String> = stateHandle.getLiveData("startDay")
    var endDate: LiveData<String> = stateHandle.getLiveData("endDay")


    private var _outcomesList = MutableLiveData<UiOutcomesSelectModel>()
    val outcomesList: LiveData<UiOutcomesSelectModel> get() = _outcomesList

    // 선택 날짜 String format
    private var _selectDay = MutableLiveData<String>("")
    val selectDay: LiveData<String> get() = _selectDay

    // 시작 날짜
    private var _startDay = MutableLiveData<String>("")
    val startDay: LiveData<String> get() = _startDay

    // 끝 날짜
    private var _endDay = MutableLiveData<String>("")
    val endDay: LiveData<String> get() = _endDay

    // 이전 페이지
    private var _back = MutableEventFlow<Boolean>()
    val back: EventFlow<Boolean> get() = _back

    // 다음 정산 페이지
    private var _nextPage = MutableEventFlow<Boolean>()
    val nextPage: EventFlow<Boolean> get() = _nextPage


    private var _outcome = MutableLiveData<LongArray>()
    val outcome: LiveData<LongArray> get() = _outcome

    private var _userEmail = MutableLiveData<Array<String>>()
    val userEmail: LiveData<Array<String>> get() = _userEmail

    private var _settlementPage = MutableEventFlow<Boolean>()
    val settlementPage: EventFlow<Boolean> get() = _settlementPage

    // 구독 여부
    private var subscribeCheck = MutableLiveData<Boolean>(false)

    init {
        getOutcomesItems()
        getSubscribeChecking()
    }
    fun getOutcomesItems(){
        val userEmails = memberArray.value!!.map { it }
        viewModelScope.launch(Dispatchers.IO) {
            booksOutComesUseCase(userEmails, startDate.value!!, endDate.value!!, prefs.getString("bookKey","")).onSuccess {
                // 불러오기 성공
                _outcomesList.postValue(it)
            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@SettleUpOutcomesSelectViewModel)))
            }
        }
    }
    // 이전 페이지로
    fun onClickedBackPage(){
        viewModelScope.launch {
            _back.emit(true)
        }
    }
    // exit 버튼 클릭 -> 처음 정산하기 페이지
    fun onClickedExit() {
        viewModelScope.launch {
            _settlementPage.emit(true)
        }
    }
    // 정산 내역 all check
    fun onClickedAllCheck()
    {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedList = _outcomesList.value?.outcomes?.map { user ->
                user.copy(isClick = true) // 선택된 내역의 isCheck를 true로 설정
            }
            _outcomesList.postValue(_outcomesList.value?.copy(outcomes = updatedList!!))
        }
    }
    fun onClickedNextPage() {
        val selectedEmails = getSelectedEmails()
        val selectedOutcomes = getSelectedOutcomes()

        _userEmail.value = selectedEmails
        _outcome.value = selectedOutcomes

        viewModelScope.launch(Dispatchers.Main) {
            baseEvent(Event.ShowLoading)

            // 애니메이션 사이클 지속 시간 계산
            val animationDelay = 200L
            val animationDuration = 600L
            val minimumCycleDuration = animationDuration + animationDelay * 2

            withContext(Dispatchers.IO) {
                if (selectedEmails.isNotEmpty()) {
                    val advertiseTime = prefs.getString("advertiseTime", "")
                    val advertiseTenMinutes = prefs.getString("advertiseSettleUpTenMinutes", "")
                    val showNextPage =
                        getAdvertiseCheck(advertiseTime) > 0 || getAdvertiseTenMinutesCheck(advertiseTenMinutes) > 0 || subscribeCheck.value!!

                    if (getAdvertiseCheck(advertiseTime) <= 0) {
                        prefs.setString("advertiseTime", "")
                    }
                    if (getAdvertiseTenMinutesCheck(advertiseTenMinutes) <= 0) {
                        prefs.setString("advertiseSettleUpTenMinutes", "")
                    }
                    // 최소 한 싸이클이 완료될 때까지 지연
                    delay(minimumCycleDuration)
                    baseEvent(Event.HideLoading)
                    _nextPage.emit(!showNextPage)
                } else {
                    baseEvent(Event.ShowToast("정산할 내역을 선택해주세요."))
                    baseEvent(Event.HideLoading)
                }
            }
        }
    }

    private fun getSelectedEmails(): Array<String> {
        return _outcomesList.value?.outcomes
            ?.filter { it.isClick }
            ?.map { it.userEmail }
            ?.toTypedArray()
            ?: emptyArray()
    }

    private fun getSelectedOutcomes(): LongArray {
        return _outcomesList.value?.outcomes
            ?.filter { it.isClick }
            ?.map {  it.money }
            ?.toLongArray() // LongArray로 변환
            ?: LongArray(0) // null일 때 빈 LongArray 반환
    }

    // 정산 내역 count
    fun settingSettlementOutcomes(bookUsers: Outcomes)
    {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedList = _outcomesList.value?.outcomes?.map { user ->
                if (user.id == bookUsers.id) {
                    user.copy(isClick = !user.isClick) // 선택된 내역의 isCheck를 true로 설정
                } else {
                    user
                }
            }
            _outcomesList.postValue(_outcomesList.value?.copy(outcomes = updatedList!!))

        }
    }

    // 10분 광고 시간 기록
    fun updateAdvertiseTenMinutes(){
        prefs.setString("advertiseSettleUpTenMinutes", getCurrentDateTimeString())
    }

    // 구독 여부 가져오기
    fun getSubscribeChecking(){
        viewModelScope.launch(Dispatchers.IO) {
            subscribeCheckUseCase().onSuccess {
                subscribeCheck.postValue(it.isValid)
            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg()))
            }
        }
    }
}