package com.aos.floney.view.analyze

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorMsg
import com.aos.model.analyze.UiAnalyzeCategoryOutComeModel
import com.aos.usecase.analyze.PostAnalyzeOutComeCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AnalyzeOutComeViewModel @Inject constructor(
    private val prefs: SharedPreferenceUtil,
    private val postAnalyzeOutComeCategoryUseCase: PostAnalyzeOutComeCategoryUseCase,
) : BaseViewModel() {


    // 지출 - 분석 결과값
    private var _postAnalyzeOutComeCategoryResult = MutableLiveData<UiAnalyzeCategoryOutComeModel>()
    val postAnalyzeOutComeCategoryResult: LiveData<UiAnalyzeCategoryOutComeModel> get() = _postAnalyzeOutComeCategoryResult

    // 지출 - 분석 선택된 월
    private var _selectMonth = MutableLiveData<String>("")
    val selectMonth : LiveData<String> get() = _selectMonth

    // 지출 분석 가져오기
    fun postAnalyzeCategory(date :String) {
        viewModelScope.launch(Dispatchers.IO) {
            baseEvent(Event.ShowCircleLoading)
            postAnalyzeOutComeCategoryUseCase(
                prefs.getString("bookKey", ""), "지출", date
            ).onSuccess {
                _selectMonth.postValue(date)
                baseEvent(Event.HideCircleLoading)
                _postAnalyzeOutComeCategoryResult.postValue(it)
            }.onFailure {
                baseEvent(Event.HideCircleLoading)
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@AnalyzeOutComeViewModel)))
            }
        }
    }
}