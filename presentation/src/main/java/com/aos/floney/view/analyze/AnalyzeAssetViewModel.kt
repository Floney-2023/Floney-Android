package com.aos.floney.view.analyze

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorMsg
import com.aos.model.analyze.UiAnalyzeAssetModel
import com.aos.usecase.analyze.PostAnalyzeAssetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AnalyzeAssetViewModel @Inject constructor(private val pref: SharedPreferenceUtil,
    private val postAnalyzeAssetUseCase: PostAnalyzeAssetUseCase
): BaseViewModel() {

    private var _postAssetResult = MutableLiveData<UiAnalyzeAssetModel>()
    val postAssetResult: LiveData<UiAnalyzeAssetModel> get() = _postAssetResult

    fun postAnalyzeAsset(date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            baseEvent(Event.ShowCircleLoading)
            postAnalyzeAssetUseCase(pref.getString("bookKey", ""), date.substring(0, 7)).onSuccess {
                Timber.e("it $it")
                baseEvent(Event.HideCircleLoading)
                _postAssetResult.postValue(it)
            }.onFailure {
                baseEvent(Event.HideCircleLoading)
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@AnalyzeAssetViewModel)))
            }
        }
    }

}