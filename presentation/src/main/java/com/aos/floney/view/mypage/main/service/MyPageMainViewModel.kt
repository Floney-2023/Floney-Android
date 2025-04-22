package com.aos.floney.view.mypage.main.service

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aos.data.util.CommonUtil
import com.aos.data.util.CurrencyUtil
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
import com.aos.floney.util.convertStringToDate
import com.aos.floney.util.getAdvertiseCheck
import com.aos.floney.util.getAdvertiseTenMinutesCheck
import com.aos.floney.util.getCurrentDateTimeString
import com.aos.model.book.getCurrencySymbolByCode
import com.aos.model.user.MyBooks
import com.aos.usecase.booksetting.BooksCurrencySearchUseCase
import com.aos.usecase.mypage.RecentBookkeySaveUseCase
import com.aos.usecase.subscribe.SubscribeBenefitUseCase
import com.aos.usecase.subscribe.SubscribeBookUseCase
import com.aos.usecase.subscribe.SubscribeCheckUseCase
import com.aos.usecase.subscribe.SubscribeUserBenefitUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.abs
import kotlin.properties.Delegates

@HiltViewModel
class MyPageMainViewModel @Inject constructor(
    private val prefs: SharedPreferenceUtil,
    private val mypageSearchUseCase: MypageSearchUseCase,
    private val booksCurrencySearchUseCase: BooksCurrencySearchUseCase,
    private val recentBookKeySaveUseCase: RecentBookkeySaveUseCase,
    private val subscribeUserUseCase: SubscribeCheckUseCase,
    private val subscribeBookUseCase: SubscribeBookUseCase,
    private val subscribeBenefitUseCase: SubscribeBenefitUseCase,
    private val subscribeUserBenefitUseCase: SubscribeUserBenefitUseCase,
    private val subscriptionDataStoreUtil: SubscriptionDataStoreUtil
) : BaseViewModel() {

    // 광고 시간
    private var _advertiseTime = MutableLiveData<String>()
    val advertiseTime: LiveData<String> get() = _advertiseTime

    // 회원 정보
    private var _mypageInfo = MutableLiveData<UiMypageSearchModel>()
    val mypageInfo: LiveData<UiMypageSearchModel> get() = _mypageInfo

    // 가계부 리스트
    private var _mypageList = MutableLiveData<List<MyBooks>>()
    val mypageList: LiveData<List<MyBooks>> get() = _mypageList

    // 알람 페이지
    private var _alarmPage = MutableEventFlow<Boolean>()
    val alarmPage: EventFlow<Boolean> get() = _alarmPage

    // 메일 문의하기 페이지
    private var _mailPage = MutableEventFlow<Boolean>()
    val mailPage: EventFlow<Boolean> get() = _mailPage

    // 공지사항 페이지
    private var _noticePage = MutableEventFlow<Boolean>()
    val noticePage: EventFlow<Boolean> get() = _noticePage

    // 회원 정보 페이지
    private var _informPage = MutableEventFlow<Boolean>()
    val informPage: EventFlow<Boolean> get() = _informPage

    // 설정 페이지
    private var _settingPage = MutableEventFlow<Boolean>()
    val settingPage: EventFlow<Boolean> get() = _settingPage

    // 개인 정보 페이지
    private var _privatePage = MutableEventFlow<Boolean>()
    val privatePage: EventFlow<Boolean> get() = _privatePage

    // 이용 약관 페이지
    private var _usageRightPage = MutableEventFlow<Boolean>()
    val usageRightPage: EventFlow<Boolean> get() = _usageRightPage

    // 가계부 추가 BottomSheet
    private var _bookAddBottomSheet = MutableEventFlow<Boolean>()
    val bookAddBottomSheet: EventFlow<Boolean> get() = _bookAddBottomSheet

    // 광고 페이지
    private var _adMobPage = MutableEventFlow<Boolean>()
    val adMobPage: EventFlow<Boolean> get() = _adMobPage

    // 제안하기 페이지
    private var _supposePage = MutableEventFlow<Boolean>()
    val supposePage: EventFlow<Boolean> get() = _supposePage

    // 리뷰 작성하기 페이지
    private var _reviewPage = MutableEventFlow<Boolean>()
    val reviewPage: EventFlow<Boolean> get() = _reviewPage

    // 구독 해지하기 페이지
    private var _unsubscribePage = MutableEventFlow<Boolean>()
    val unsubscribePage : EventFlow<Boolean> get() = _unsubscribePage

    // 마이페이지 정보 로드
    private var _loadCheck = MutableEventFlow<Boolean>()
    val loadCheck: EventFlow<Boolean> get() = _loadCheck

    // 구독 페이지
    private var _subscribePage = MutableEventFlow<Boolean>()
    val subscribePage: EventFlow<Boolean> get() = _subscribePage

    // 구독 여부 (null : 로딩중, false/true 관리)
    private var _subscribeCheck = MutableLiveData<Boolean?>(null)
    val subscribeCheck: LiveData<Boolean?> get() = _subscribeCheck

    // 구독 해지 팝업 로드
    private var _unsubscribePopup = MutableEventFlow<Boolean>()
    val unsubscribePopup: EventFlow<Boolean> get() = _unsubscribePopup

    // 가계부 추가 가능 여부
    var walletAddCheck = MutableLiveData<Boolean>(false)

    // 광고 남은 시간 설정
    fun settingAdvertiseTime() {
        val adverseTiseTime = prefs.getString("advertiseTime", "")
        if (adverseTiseTime.isNotEmpty()) {
            val remainingMinutes = getAdvertiseCheck(adverseTiseTime)

            if (remainingMinutes <= 0) {
                prefs.setString("advertiseTime", "")
                _advertiseTime.postValue("6:00")
            } else {
                val hours = remainingMinutes / 60
                val minutes = remainingMinutes % 60
                _advertiseTime.postValue(String.format("%02d:%02d", hours, minutes))
            }

        } else {
            _advertiseTime.postValue("6:00")
        }
    }

    // 광고 시청 시간 설정
    fun updateAdvertiseTime() {
        prefs.setString("advertiseTime", getCurrentDateTimeString())
        settingAdvertiseTime()
    }

    // 가계부 구독 여부 세팅
    fun setBookSubscribeChecking(){
        viewModelScope.launch(Dispatchers.IO) {
            subscribeBookUseCase(prefs.getString("bookKey","")).onSuccess {

                // 가계부 구독 여부 캐싱
                subscriptionDataStoreUtil.setBookSubscribe(it.isValid)

                // 3. 구독 만료 팝업 여부 확인
                getSubscribeBenefitChecking()
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

                        // 구독 만료 여부 확인
                        // 유저 관점에서
                        val expiredUser = !subscriptionDataStoreUtil.getUserSubscribe().first() && userBenefit.maxBook

                        // 가계부 관점에서
                        val expiredBook = !subscriptionDataStoreUtil.getBookSubscribe().first() && (bookBenefit.maxFavorite || bookBenefit.overBookUser)

                        Timber.i("book : ${expiredBook} user : ${expiredUser}")
                        val expiredCheck = expiredUser || expiredBook
                        // 구독 혜택 적용 여부 캐싱
                        subscriptionDataStoreUtil.setSubscribeExpired(expiredCheck)

                        _loadCheck.emit(true)
                    }
                }
            } catch (e: Exception) {
                // 코루틴 실행 중 발생한 예외 처리
                baseEvent(Event.ShowToast(e.message.parseErrorMsg()))
            }
        }
    }

    // 1. 유저 구독 여부 가져오기
    fun getSubscribeStatus(bookSize: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            subscribeUserUseCase().onSuccess {
                walletAddCheck.postValue(when {
                    it.isValid -> bookSize < 4
                    else -> bookSize < 2
                })

                Timber.e("기존 구독 상태 : ${subscribeCheck.value ?: "null"} 현재 구독 상태 : ${it.isValid}")
                _subscribeCheck.postValue(it.isValid)
                subscriptionDataStoreUtil.setUserSubscribe(it.isValid)

                // 구독 상태였다가, 새로 읽어온 값이 false라면(=구독 취소된 상태) 구독 취소 팝업
                if(subscribeCheck.value == true && !it.isValid)
                    _unsubscribePopup.emit(true)

                // 2. 가계부 구독 여부 확인
                setBookSubscribeChecking()

            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg()))
            }
        }
    }

    // 마이페이지 정보 읽어오기
    fun searchMypageItems() {
        viewModelScope.launch(Dispatchers.IO) {
            mypageSearchUseCase().onSuccess {

                var sortedBooks =
                    it.myBooks.sortedByDescending { it.bookKey == prefs.getString("bookKey", "") }

                val updatedResult = it.copy(myBooks = sortedBooks.map { myBook ->
                    if (myBook.bookKey == prefs.getString("bookKey", "")) {
                        myBook.copy(recentCheck = true)
                    } else {
                        myBook.copy(recentCheck = false)
                    }
                })

                CommonUtil.provider = it.provider
                CommonUtil.userEmail = it.email
                CommonUtil.userProfileImg = it.profileImg

                _mypageInfo.postValue(updatedResult)

                getSubscribeStatus(bookSize = updatedResult.myBooks.size)
            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg()))
            }
        }
    }

    // 화폐 설정 조회
    fun searchCurrency() {
        viewModelScope.launch {
            booksCurrencySearchUseCase(prefs.getString("bookKey", "")).onSuccess {
                if (it.myBookCurrency != "") {
                    baseEvent(Event.HideLoading)
                    // 화폐 단위 저장
                    prefs.setString("symbol", getCurrencySymbolByCode(it.myBookCurrency))
                    CurrencyUtil.currency = getCurrencySymbolByCode(it.myBookCurrency)

                } else {
                    baseEvent(Event.HideLoading)
                    baseEvent(Event.ShowToastRes(R.string.currency_error))
                }
            }.onFailure {
                baseEvent(Event.HideLoading)
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@MyPageMainViewModel)))
            }
        }
    }

    // 알람 페이지 이동
    fun onClickAlarmPage() {
        viewModelScope.launch {
            _alarmPage.emit(true)
        }
    }

    // 설정 페이지 이동
    fun onClickSettingPage() {
        viewModelScope.launch {
            _settingPage.emit(true)
        }
    }

    // 회원 정보 페이지 이동
    fun onClickInformPage() {
        viewModelScope.launch {
            _informPage.emit(true)
        }
    }

    // 메일 문의 하기 페이지 이동
    fun onClickAnswerPage() {
        viewModelScope.launch {
            _mailPage.emit(true)
        }
    }

    // 공지 사항 페이지 이동
    fun onClickNoticePage() {
        viewModelScope.launch {
            _noticePage.emit(true)
        }
    }

    // 리뷰 작성하기 페이지 이동
    fun onClickReviewPage() {
        viewModelScope.launch {
            _reviewPage.emit(true)
        }
    }

    // 개인 정보 처리방침 페이지 이동
    fun onClickPrivateRolePage() {
        viewModelScope.launch {
            _privatePage.emit(true)
        }
    }

    // 이용 약관 페이지 이동
    fun onClickUsageRightPage() {
        viewModelScope.launch {
            _usageRightPage.emit(true)
        }
    }

    // 구독 해지하기 페이지 이동
    fun onClickUnsubscribePage() {
        viewModelScope.launch {
            _unsubscribePage.emit(true)
        }
    }

    // 최근 저장 가계부 저장 (가계부 전환)
    fun settingBookKey(bookKey: String) {
        viewModelScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                if (mypageInfo.value!!.myBooks.size != 1 && bookKey != prefs.getString(
                        "bookKey",
                        ""
                    )
                ) {// 가계부가 2개 이상일 때만 로딩 싸이클
                    recentBookKeySaveUseCase(bookKey).onSuccess {
                        prefs.setString("bookKey", bookKey)


                        baseEvent(Event.ShowLoading)

                        val sortedBooks =
                            _mypageInfo.value!!.myBooks.sortedByDescending { it.bookKey == bookKey }

                        val updatedResult =
                            _mypageInfo.value!!.copy(myBooks = sortedBooks.map { myBook ->
                                if (myBook.bookKey == bookKey) {
                                    myBook.copy(recentCheck = true)
                                } else {
                                    myBook.copy(recentCheck = false)
                                }
                            })

                        delay(1000)
                        _mypageInfo.postValue(updatedResult)

                        // 가계부 구독 여부 update
                        setBookSubscribeChecking()

                        // 화폐 단위 가져오기
                        searchCurrency()
                    }.onFailure {
                        baseEvent(Event.HideLoading)
                        baseEvent(Event.ShowToast(it.message.parseErrorMsg()))
                    }
                }
            }
        }
    }

    // 가계부 추가
    fun onClickBookAdd() {
        viewModelScope.launch {
            _bookAddBottomSheet.emit(true)
        }
    }

    // 광고 제거 혹은 플레이스토어 리뷰
    fun onClickAdMobOrReview() {
        viewModelScope.launch {
            if(subscribeCheck.value!!)
                _reviewPage.emit(true)
            else
                _adMobPage.emit(true)
        }
    }

    // 유저 프로필 이미지 불러오기
    fun getUserProfile(): String {
        return CommonUtil.userProfileImg
    }

    // 카페 제안하기
    fun onClickSuppose() {
        viewModelScope.launch {
            _supposePage.emit(true)
        }
    }

    // 구독 버튼 클릭 (구독하기 or 구독 정보 보기)
    fun onClickSubscribe() {
        viewModelScope.launch {
            _subscribePage.emit(subscribeCheck.value!!)
        }
    }
}