package com.aos.floney.view.book.setting.category

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.R
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.formatNumber
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.model.book.UiBookCategory
import com.aos.model.home.DayMoneyModifyItem
import com.aos.usecase.booksetting.BooksCategoryAddUseCase
import com.aos.usecase.booksetting.BooksCategoryDeleteUseCase
import com.aos.usecase.history.GetBookCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookSettingCategoryAddViewModel @Inject constructor(
    private val app: Application,
    stateHandle: SavedStateHandle,
    private val prefs: SharedPreferenceUtil,
    private val booksCategoryAddUseCase: BooksCategoryAddUseCase
) : BaseViewModel() {

    // 자산, 지출, 수입, 이체
    private val _flow: MutableLiveData<String> = stateHandle.getLiveData("flow")
    val flow: LiveData<String> get() = _flow

    // 항목이름
    var name = MutableLiveData<String>("")

    // 이전 페이지
    private var _back = MutableEventFlow<Boolean>()
    val back: EventFlow<Boolean> get() = _back


    // 이전 페이지
    private var _completePage = MutableEventFlow<String>()
    val completePage: EventFlow<String> get() = _completePage

    fun onClickedExit() {
        viewModelScope.launch {
            _back.emit(true)
        }
    }

    // 카테고리 추가
    fun onClickAddComplete() {
        if (name.value!="") {
            viewModelScope.launch(Dispatchers.IO) {
                booksCategoryAddUseCase(
                    bookKey = prefs.getString("bookKey", ""),
                    flow.value!!,
                    name.value!!
                ).onSuccess {
                    baseEvent(Event.ShowSuccessToast(app.getString(R.string.toast_added_to_category)))
                    _completePage.emit(it.name)
                }.onFailure {
                    baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@BookSettingCategoryAddViewModel)))
                }
            }
        } else{
            baseEvent(Event.ShowToast(app.getString(R.string.toast_enter_item_name)))
        }

    }

    // 자산, 지출, 수입, 이체 클릭
    fun onClickFlow(type: String) {
        _flow.value = type
    }
}