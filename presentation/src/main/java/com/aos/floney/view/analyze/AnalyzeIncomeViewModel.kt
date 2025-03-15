package com.aos.floney.view.analyze

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorMsg
import com.aos.model.analyze.UiAnalyzeCategoryInComeModel
import com.aos.model.analyze.UiAnalyzeCategoryOutComeModel
import com.aos.usecase.analyze.PostAnalyzeInComeCategoryUseCase
import com.aos.usecase.analyze.PostAnalyzeOutComeCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AnalyzeIncomeViewModel @Inject constructor(
    private val prefs: SharedPreferenceUtil,
    private val postAnalyzeInComeCategoryUseCase: PostAnalyzeInComeCategoryUseCase
) : BaseViewModel() {


    // 지출 - 분석 선택된 월
    private var _selectMonth = MutableLiveData<String>("")
    val selectMonth : LiveData<String> get() = _selectMonth

    // 지출 - 분석 결과값
    private var _postAnalyzeInComeCategoryResult = MutableLiveData<UiAnalyzeCategoryInComeModel>()
    val postAnalyzeInComeCategoryResult: LiveData<UiAnalyzeCategoryInComeModel> get() = _postAnalyzeInComeCategoryResult


    // 지출 분석 가져오기
    fun postAnalyzeCategory(date :String) {
        viewModelScope.launch(Dispatchers.IO) {
            baseEvent(Event.ShowLoading)
            postAnalyzeInComeCategoryUseCase(
                prefs.getString("bookKey", ""), "수입", date
            ).onSuccess {
                baseEvent(Event.HideLoading)
                _selectMonth.postValue(date)
                _postAnalyzeInComeCategoryResult.postValue(it)
            }.onFailure {
                baseEvent(Event.HideLoading)
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@AnalyzeIncomeViewModel)))
            }
        }
    }
}