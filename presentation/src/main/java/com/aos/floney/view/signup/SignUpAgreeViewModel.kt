package com.aos.floney.view.signup

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aos.floney.R
import com.aos.floney.base.BaseViewModel
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SignUpAgreeViewModel @Inject constructor(
    private val app: Application
): BaseViewModel() {


    // 뒤로가기
    private var _back = MutableEventFlow<Boolean>()
    val back: EventFlow<Boolean> get() = _back

    // 다음 페이지 이동
    private var _nextPage = MutableEventFlow<Boolean>()
    val nextPage: EventFlow<Boolean> get() = _nextPage

    // 전체 동의 체크
    private var _allTerms = MutableLiveData<Boolean>(false)
    val allTerms: LiveData<Boolean> get() = _allTerms

    // 서비스 이용 약관
    private var _serviceTerms = MutableLiveData<Boolean>(false)
    val serviceTerms: LiveData<Boolean> get() = _serviceTerms

    // 개인 정보 이용 동의
    private var _useInfoTerms = MutableLiveData<Boolean>(false)
    val useInfoTerms: LiveData<Boolean> get() = _useInfoTerms

    // 마케팅 정보 수신
    private var _marketingTerms = MutableLiveData<Boolean>(false)
    val marketingTerms: LiveData<Boolean> get() = _marketingTerms

    // 만 14세 이상 확인
    private var _ageTerms = MutableLiveData<Boolean>(false)
    val ageTerms: LiveData<Boolean> get() = _ageTerms

    // 약관 페이지 이동
    private var _clickedTerms = MutableLiveData<Boolean>()
    val clickedTerms: LiveData<Boolean> get() = _clickedTerms


    var subtitle = MutableLiveData<String>("")

    var link = MutableLiveData<String>("")

    // 전체 동의 클릭
    fun onClickAllTerms() {
        val flag = !_allTerms.value!!

        _allTerms.postValue(flag)
        _serviceTerms.postValue(flag)
        _useInfoTerms.postValue(flag)
        _marketingTerms.postValue(flag)
        _ageTerms.postValue(flag)
    }

    fun onClickMoveTermsUrl(url: String) {
        subtitle.value = when (url) {
            "https://m.cafe.naver.com/floney/2" -> app.getString(R.string.service_terms_title)
            "https://m.cafe.naver.com/floney/4" -> app.getString(R.string.service_privacy_title)
            "https://m.cafe.naver.com/floney/6" -> app.getString(R.string.sign_up_marketing_terms_title)
            else -> app.getString(R.string.terms_title)
        }
        link.value = url
        _clickedTerms.postValue(true)
    }

    // 서비스 이용 약관 클릭
    fun onClickServiceTerms() {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                _serviceTerms.postValue(!_serviceTerms.value!!)
            }
            checkAllAgree()
        }
    }

    // 개인정보 수집 및 이용 동의 클릭
    fun onClickUseInfoTerms() {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                _useInfoTerms.postValue(!_useInfoTerms.value!!)
            }
            checkAllAgree()
        }
    }

    // 마케팅 정보 수신 클릭
    fun onClickMarketingTerms() {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                _marketingTerms.postValue(!_marketingTerms.value!!)
            }
            checkAllAgree()
        }
    }

    // 만 14세 이상 확인 클릭
    fun onClickAgeTerms() {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                _ageTerms.postValue(!_ageTerms.value!!)
            }
            checkAllAgree()
        }
    }

    // 다음 페이지로 이동
    fun onClickNextPage() {
        if(checkEssentialAgree()) {
            // 페이지 전환
            viewModelScope.launch {
                _nextPage.emit(true)
            }
        } else {
            baseEvent(Event.ShowToastRes(R.string.sign_up_error_terms))
        }
    }

    // 이전 페이지로 이동
    fun onClickPreviousPage() {
        viewModelScope.launch {
            _back.emit(true)
        }
    }

    // 모두 체크 됐으면 전체 동의 버튼 Check 상태로 변경
    private fun checkAllAgree() {
        _allTerms.value = _serviceTerms.value == true && _useInfoTerms.value == true && _marketingTerms.value == true && _ageTerms.value == true
    }

    // 필수 체크 여부
    private fun checkEssentialAgree(): Boolean {
        return _serviceTerms.value == true && _useInfoTerms.value == true && _ageTerms.value == true
    }

}
