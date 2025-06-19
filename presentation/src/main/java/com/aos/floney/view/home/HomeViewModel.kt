package com.aos.floney.view.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aos.data.util.CurrencyUtil
import com.aos.data.util.SharedPreferenceUtil
import com.aos.data.util.SubscriptionDataStoreUtil
import com.aos.floney.R
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.floney.util.getAdvertiseCheck
import com.aos.floney.util.getAdvertiseTenMinutesCheck
import com.aos.floney.util.getCurrentDateTimeString
import com.aos.model.book.getCurrencySymbolByCode
import com.aos.model.home.DayMoney
import com.aos.model.home.ExtData
import com.aos.model.home.MonthMoney
import com.aos.model.home.UiBookDayModel
import com.aos.model.home.UiBookInfoModel
import com.aos.model.user.UserModel.userNickname
import com.aos.usecase.booksetting.BooksCurrencySearchUseCase
import com.aos.usecase.home.GetBookInfoUseCase
import com.aos.usecase.home.GetMoneyHistoryDaysUseCase
import com.aos.usecase.subscribe.SubscribeBenefitUseCase
import com.aos.usecase.subscribe.SubscribeBookUseCase
import com.aos.usecase.subscribe.SubscribeCheckUseCase
import com.aos.usecase.subscribe.SubscribeUserBenefitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val prefs: SharedPreferenceUtil,
    private val subscriptionDataStoreUtil: SubscriptionDataStoreUtil,
    private val getMoneyHistoryDaysUseCase: GetMoneyHistoryDaysUseCase,
    private val booksCurrencySearchUseCase : BooksCurrencySearchUseCase,
    private val getBookInfoUseCase: GetBookInfoUseCase,
    private val subscribeUserUseCase: SubscribeCheckUseCase,
    private val subscribeBookUseCase: SubscribeBookUseCase,
    private val subscribeBenefitUseCase: SubscribeBenefitUseCase,
    private val subscribeUserBenefitUseCase: SubscribeUserBenefitUseCase
) : BaseViewModel() {

    // 날짜 데이터
    private val _calendar = MutableStateFlow<Calendar>(Calendar.getInstance())

    // 표시 중인 날짜
    private var _showDate = MutableLiveData<String>()
    val showDate: LiveData<String> get() = _showDate

    private var _bookInfo = MutableLiveData<UiBookInfoModel>()
    val bookInfo: LiveData<UiBookInfoModel> get() = _bookInfo

    // 날짜 선택 버튼 클릭
    private var _clickedChoiceDate = MutableEventFlow<String>()
    val clickedChoiceDate: EventFlow<String> get() = _clickedChoiceDate

    // 이전 월 이동
    private var _clickedPreviousMonth = MutableEventFlow<String>()
    val clickedPreviousMonth: EventFlow<String> get() = _clickedPreviousMonth

    // 다음 월 이동
    private var _clickedNextMonth = MutableEventFlow<String>()
    val clickedNextMonth: EventFlow<String> get() = _clickedNextMonth

    // 캘린더, 일별 표시 타입
    private var _clickedShowType = MutableLiveData<String>("month")
    val clickedShowType: LiveData<String> get() = _clickedShowType

    // 내역추가
    private var _clickedAddHistory = MutableEventFlow<Boolean>()
    val clickedAddHistory: EventFlow<Boolean> get() = _clickedAddHistory

    private var _getMoneyDayData = MutableEventFlow<UiBookDayModel>()
    val getMoneyDayData: EventFlow<UiBookDayModel> get() = _getMoneyDayData

    private var _getMoneyDayList = MutableLiveData<List<DayMoney>>()
    val getMoneyDayList: LiveData<List<DayMoney>> get() = _getMoneyDayList

    private var _onClickedShowDetail = MutableLiveData<MonthMoney?>(null)
    val onClickedShowDetail: LiveData<MonthMoney?> get() = _onClickedShowDetail

    private var myNickname: String = ""

    // 설정 페이지
    private var _settingPage = MutableEventFlow<Boolean>()
    val settingPage: EventFlow<Boolean> get() = _settingPage

    // 광고 ON/OFF
    private var _showAdvertisement = MutableLiveData<Boolean>()
    val showAdvertisement: LiveData<Boolean> get() = _showAdvertisement


    // 접근 권한 확인 O/X
    private var _accessCheck = MutableEventFlow<Boolean>()
    val accessCheck: EventFlow<Boolean> get() = _accessCheck

    // 유저 구독 여부
    var subscribeCheck = MutableLiveData<Boolean>(false)

    // 구독 만료 여부 (구독 안 한 경우만 확인, 구독 적용 팝업을 보여주기 위해서)
    var subscribeExpired = MutableLiveData<Boolean>(false)

    // 구독 팝업 표시 여부 (구독 만료 여부 & 10분 타이머 체크)
    private var _subscribePopupShow = MutableLiveData<Boolean>()
    val subscribePopupShow: LiveData<Boolean> get() = _subscribePopupShow

    // 진입 시 표시되는 팝업인 지
    var subscribePopupEnter = MutableLiveData<Boolean>(true)

    // dim 처리 여부 값 합쳐진 LiveData 선언
    val showOverlay = MediatorLiveData<Boolean>().apply {
        addSource(_onClickedShowDetail) { value = shouldShowOverlay() }
        addSource(_subscribePopupShow) { value = shouldShowOverlay() }
    }

    private fun shouldShowOverlay(): Boolean {
        return _onClickedShowDetail.value != null || _subscribePopupShow.value == true
    }

    init {
        getFormatDateMonth()
        setAdvertisement()
    }

    fun initCalendarMonth() {
//        _calendar.value = Calendar.getInstance()
        _showDate.value = getFormatDateMonth()
    }

    fun initCalendarDay() {
//        _calendar.value = Calendar.getInstance()
        _showDate.value = getFormatDateDay()
    }

    // 가계부 정보 가져오기
    fun getBookInfoData() {
        viewModelScope.launch {
            getBookInfoUseCase(prefs.getString("bookKey","")).onSuccess {

                // 프로필 보기 여부 저장
                prefs.setBoolean("seeProfileStatus", it.seeProfileStatus)

                // 내 닉네임 저장
                it.ourBookUsers.forEach {
                    if (it.me) {
                        myNickname = it.name
                        userNickname = it.name
                    }
                }
                _bookInfo.postValue(it)

                // 화폐 단위 가져오기
                searchCurrency()
            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@HomeViewModel)))
            }
        }
    }

    // 화폐 설정 조회
    fun searchCurrency(){
        viewModelScope.launch {
            booksCurrencySearchUseCase(prefs.getString("bookKey", "")).onSuccess {
                if(it.myBookCurrency != "") {
                    // 화폐 단위 저장
                    prefs.setString("symbol", getCurrencySymbolByCode(it.myBookCurrency))
                    CurrencyUtil.currency = getCurrencySymbolByCode(it.myBookCurrency)

                } else {
                    baseEvent(Event.ShowToastRes(R.string.currency_error))
                }
            }.onFailure {
                _accessCheck.emit(true)
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@HomeViewModel)))
            }
        }
    }

    // 유저 가계부 유효 확인
    fun getBookDays(date: String) {
        viewModelScope.launch {
            getMoneyHistoryDaysUseCase(prefs.getString("bookKey", ""), date).onSuccess { data ->
                // 이월 설정 데이터 생성
                val carryInfoData = createCarryInfoData(data, date, bookInfo.value!!.seeProfileStatus)

                // seeProfileStatus 업데이트 목록 생성
                val updatedData = updateSeeProfileStatus(data.data, bookInfo.value!!.seeProfileStatus)

                // 반복 내역 유무에 따른 정렬된 데이터 목록 생성
                val sortedData = sortData(updatedData)

                // 최종 데이터 : 이월 설정, 반복 내역 O, 반복 내역 X 순서
                val updatedList = buildUpdatedList(carryInfoData, sortedData)

                _getMoneyDayData.emit(UiBookDayModel(updatedList, data.extData, data.carryOverData))
                _getMoneyDayList.postValue(updatedList)
            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@HomeViewModel)))
            }
        }
    }

    private fun createCarryInfoData(data: UiBookDayModel, date: String, seeProfileStatus: Boolean): DayMoney? {
        return if (data.carryOverData.carryOverStatus &&
            data.carryOverData.carryOverMoney != "0" &&
            date.split("-")[2].toInt() == 1) {
            DayMoney(
                id = -1,
                money = data.carryOverData.carryOverMoney,
                description = "이월",
                lineCategory = "",
                lineSubCategory = "",
                assetSubCategory = "",
                exceptStatus = false,
                writerEmail = "",
                writerNickName = "",
                writerProfileImg = "user_default",
                repeatDuration = "없음",
                memo = "",
                imageUrls = listOf(),
                seeProfileStatus = seeProfileStatus
            )
        } else {
            null
        }
    }

    private fun updateSeeProfileStatus(data: List<DayMoney>, seeProfileStatus: Boolean): List<DayMoney> {
        return data.map { dayMoney ->
            dayMoney.copy(seeProfileStatus = seeProfileStatus)
        }
    }

    private fun sortData(data: List<DayMoney>): List<DayMoney> {
        return data.sortedBy { it.repeatDuration != "없음" }
    }

    private fun buildUpdatedList(carryInfoData: DayMoney?, sortedData: List<DayMoney>): List<DayMoney> {
        return carryInfoData?.let { listOf(it) + sortedData } ?: sortedData
    }

    // 이전 월 클릭
    fun onClickPreviousMonth() {
        viewModelScope.launch {
            if (_clickedShowType.value == "month") {
                updateCalendarMonth(-1)
                _clickedPreviousMonth.emit(getFormatDateMonth())
            } else {
                updateCalendarDay(-1)
                _clickedPreviousMonth.emit(getFormatDateDay())
            }
        }
    }

    // 다음 월 클릭
    fun onClickNextMonth() {
        viewModelScope.launch {
            if (_clickedShowType.value == "month") {
                updateCalendarMonth(1)
                _clickedNextMonth.emit(getFormatDateMonth())
            } else {
                updateCalendarDay(1)
                _clickedNextMonth.emit(getFormatDateDay())
            }
        }
    }

    // 다음 월 클릭
    fun onClickChoiceDate() {
        if (_clickedShowType.value == "month") {
            viewModelScope.launch {
                _clickedChoiceDate.emit(getFormatYearMonthDate())
            }
        }
    }

    // 일자 상세 표시 열기
    fun onClickShowDetail(item: MonthMoney) {
        updateCalendarClickedItem(item.year.toInt(), item.month.toInt(), item.day.toInt())
        _onClickedShowDetail.postValue(item)

        getBookDays(
            "${item.year}-${
                if (item.month.toInt() < 10) {
                    "0${item.month}"
                } else {
                    item.month
                }
            }-${
                if (item.day.toInt() < 10) {
                    "0${item.day}"
                } else {
                    item.day
                }
            }"
        )
    }

    // 일자 상세 표시 닫기
    fun onClickCloseShowDetail() {
        _onClickedShowDetail.postValue(null)
    }

    // 캘린더, 일별 표시타입 변경
    fun onClickShowType(type: String) {
        if (_clickedShowType.value != type) {
            _clickedShowType.value = type
        }
    }

    // 캘린더, 일별 표시타입 변경
    fun onClickAddHistory() {
        viewModelScope.launch {
            _clickedAddHistory.emit(true)
        }
    }

    // 탭바로 추가할 경우
    fun onClickTabAddHistory() {
        setTodayDate()

        viewModelScope.launch {
            _clickedAddHistory.emit(true)
        }
    }

    // 캘린더 값 변경
    private fun updateCalendarMonth(value: Int) {
        _calendar.value.set(Calendar.DAY_OF_MONTH, 1)
        _calendar.value.add(Calendar.MONTH, value)
    }

    // 캘린더 값 변경
    private fun updateCalendarDay(value: Int) {
        _calendar.value.add(Calendar.DATE, value)
    }

    // 캘린더 값 변경
    fun updateCalendarClickedItem(year: Int, month: Int, date: Int) {
        _calendar.value.set(Calendar.YEAR, year)
        _calendar.value.set(Calendar.MONTH, month - 1)
        _calendar.value.set(Calendar.DATE, date)

        viewModelScope.launch {
            if (_clickedShowType.value == "month") {
                _clickedNextMonth.emit(getFormatDateMonth())
            } else {
                _clickedNextMonth.emit(getFormatDateDay())
            }
        }
    }

    // 오늘 날짜로 calendar 설정하기
    private fun setTodayDate() {
        val dateArr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()).split("-")
        _calendar.value.set(Calendar.YEAR, dateArr[0].toInt())
        _calendar.value.set(Calendar.MONTH, dateArr[1].toInt() - 1)
        _calendar.value.set(Calendar.DATE, dateArr[2].toInt())
    }

    // 날짜 포멧 결과 가져오기
    fun getFormatDateMonth(): String {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(_calendar.value.time)
        val showDate = date.substring(0, 7).replace("-", ".")
        _showDate.postValue(showDate)
        return date
    }

    // 날짜 포멧 결과 가져오기
    fun getFormatDateDay(): String {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(_calendar.value.time)
        val showDate = date.substring(5, 10).replace("-", ".")
        _showDate.postValue(showDate)
        return date
    }

    // 년, 월 일 가져오기
    private fun getFormatYearMonthDate(): String {
        return SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(_calendar.value.time) + "-01"
    }

    // 선택된 날짜 가져오기
    fun getClickDate(): String {
        val year = _calendar.value.get(Calendar.YEAR)
        val month = if ((_calendar.value.get(Calendar.MONTH) + 1) < 10) {
            "0${_calendar.value.get(Calendar.MONTH) + 1}"
        } else {
            _calendar.value.get(Calendar.MONTH) + 1
        }
        val day = if (_calendar.value.get(Calendar.DATE) < 10) {
            "0${_calendar.value.get(Calendar.DATE)}"
        } else {
            _calendar.value.get(Calendar.DATE)
        }
        return "$year.$month.$day"
    }

    // 내 닉네임 가져오기
    fun getMyNickname(): String {
        return myNickname
    }

    // 가계부 설정 페이지 이동
    fun onClickSettingPage() {
        viewModelScope.launch {
            val advertiseTime = prefs.getString("advertiseTime", "")
            val advertiseTenMinutes = prefs.getString("advertiseBookSettingTenMinutes", "")

            // true면 광고 없이 이동, false면 광고 후 이동
            val showSettingPage = advertiseTime.isNotEmpty() || getAdvertiseTenMinutesCheck(advertiseTenMinutes) > 0 || subscribeCheck.value!!

            if (getAdvertiseTenMinutesCheck(advertiseTenMinutes) <= 0) {
                prefs.setString("advertiseBookSettingTenMinutes", "")
            }

            _settingPage.emit(!showSettingPage)
        }
    }
    // 10분 광고 시간 기록
    fun updateAdvertiseTenMinutes(){
        prefs.setString("advertiseBookSettingTenMinutes", getCurrentDateTimeString())
    }
    // 광고 표시 여부
    fun setAdvertisement() {
        val advertiseTime = prefs.getString("advertiseTime", "")

        if (getAdvertiseCheck(advertiseTime) <= 0) {
            prefs.setString("advertiseTime", "")
        }

        _showAdvertisement.postValue(advertiseTime.isEmpty())
    }
    // 가계부 접근 확인
    fun setAccessCheck(booleanExtra: Boolean) {
        viewModelScope.launch {
            if(booleanExtra)
                _accessCheck.emit(true)
        }
    }

    // 구독 여부 세팅
    fun setBookSubscribeChecking(){
        viewModelScope.launch(Dispatchers.IO) {
            subscribeBookUseCase(prefs.getString("bookKey","")).onSuccess {

                // 가계부 구독 여부 캐싱
                subscriptionDataStoreUtil.setBookSubscribe(it.isValid)

                // 3. 혜택(가계부, 개인)이 적용되어있는 지 확인
                getSubscribeBenefitChecking()

            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg()))
            }
        }
    }

    fun setUserSubscribeChecking() {
        // 1. 유저 구독 여부 확인
        viewModelScope.launch(Dispatchers.IO) {
            subscribeUserUseCase().onSuccess {
                subscribeCheck.postValue(it.isValid)

                // 유저 구독 여부 캐싱
                subscriptionDataStoreUtil.setUserSubscribe(it.isValid)

                if (it.isValid) { // 개인 구독이 되었다면 광고 시간 reset
                    prefs.setString("advertiseTime", "")
                    _showAdvertisement.postValue(false)
                }

                // 2. 가계부 구독 여부 확인
                setBookSubscribeChecking()

            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg()))
            }
        }
    }


    // 구독 혜택 받고 있는 지 여부 가져오기
    fun getSubscribeBenefitChecking(){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bookKey = prefs.getString("bookKey", "")

                // 가계부 혜택 확인
                val benefitResult = subscribeBenefitUseCase(bookKey)
                benefitResult.onFailure {
                    baseEvent(Event.ShowToast(it.message.parseErrorMsg()))
                    return@launch // 실패 시 작업 종료
                }

                // 유저 혜택 확인
                val userBenefitResult = subscribeUserBenefitUseCase()
                userBenefitResult.onFailure {
                    baseEvent(Event.ShowToast(it.message.parseErrorMsg()))
                    return@launch // 실패 시 작업 종료
                }

                // 두 작업이 모두 성공한 경우 처리
                benefitResult.onSuccess { bookBenefit ->
                    userBenefitResult.onSuccess { userBenefit ->
                        // 10분 타이머 남은 시간
                        val remainTime = prefs.getString("subscribeCheckTenMinutes", "")

                        // 구독 만료 여부 확인
                        // 유저 관점에서
                        val expiredUser = !subscriptionDataStoreUtil.getUserSubscribe().first() && userBenefit.maxBook

                        // 가계부 관점에서
                        val expiredBook = !subscriptionDataStoreUtil.getBookSubscribe().first() && (bookBenefit.maxFavorite || bookBenefit.overBookUser)

                        Timber.i("book : ${expiredBook} user : ${expiredUser} remainTime : ${remainTime}")

                        val expiredCheck = expiredUser || expiredBook

                        // 구독 혜택 적용 여부 캐싱
                        subscriptionDataStoreUtil.setSubscribeExpired(expiredCheck)

                        // 10분 지났을 경우 리셋
                        if (getAdvertiseTenMinutesCheck(remainTime) < 0)
                            prefs.setString("subscribeCheckTenMinutes", "")

                        // 구독 만료 여부 업데이트
                        subscribeExpired.postValue(expiredCheck)

                        // 구독 팝업 표시 여부 업데이트 (구독 만료 O && 타이머 시간이 유효하지 않을 경우)
                        changeSubscribePopupShow(getAdvertiseTenMinutesCheck(remainTime) <= 0 && expiredCheck)
                    }
                }
            } catch (e: Exception) {
                // 코루틴 실행 중 발생한 예외 처리
                baseEvent(Event.ShowToast(e.message.parseErrorMsg()))
            }
        }
    }
    fun changeSubscribePopupShow(isCheck : Boolean){
        viewModelScope.launch(Dispatchers.IO) {
            _subscribePopupShow.postValue(isCheck)
        }
    }
}