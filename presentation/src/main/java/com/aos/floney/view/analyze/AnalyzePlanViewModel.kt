package com.aos.floney.view.analyze

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import java.util.Locale
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.model.analyze.UiAnalyzePlanModel
import com.aos.usecase.analyze.PostAnalyzeIPlanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Calendar

@HiltViewModel
class AnalyzePlanViewModel @Inject constructor(
    private val prefs: SharedPreferenceUtil,
    val postAnalyzeIPlanUseCase: PostAnalyzeIPlanUseCase
) : BaseViewModel() {
    // 예산 조회하기
    private var _postAnalyzePlan =
        MutableLiveData<UiAnalyzePlanModel>(UiAnalyzePlanModel("", "0원", "0", "", "", "", ""))
    val postAnalyzePlan: LiveData<UiAnalyzePlanModel> get() = _postAnalyzePlan

    // 포맷된 텍스트 for UI - AnalyzeMapper에서 가져옴
    val budgetUsedText: LiveData<String> = _postAnalyzePlan.map { plan ->
        plan.budgetUsedText
    }

    val budgetPerDayText: LiveData<String> = _postAnalyzePlan.map { plan ->
        plan.budgetPerDayText
    }

    private var _onClickSetBudget = MutableEventFlow<Boolean>()
    val onClickSetBudget: EventFlow<Boolean> get() = _onClickSetBudget

    // 예산 조회하기
    fun postAnalyzePlan(date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            baseEvent(Event.ShowCircleLoading)
            postAnalyzeIPlanUseCase(prefs.getString("bookKey", ""), date).onSuccess {
                Timber.e("it $it")

                _postAnalyzePlan.postValue(it)
                baseEvent(Event.HideCircleLoading)
            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@AnalyzePlanViewModel)))
                baseEvent(Event.HideCircleLoading)
            }
        }
    }

    fun onClickSetBudget() {
        viewModelScope.launch {
            _onClickSetBudget.emit(true)
        }
    }

    private fun calculateRemainingDaysInMonth(): Long {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_MONTH)
        val lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        return (lastDayOfMonth - today).toLong() + 1
    }
}
