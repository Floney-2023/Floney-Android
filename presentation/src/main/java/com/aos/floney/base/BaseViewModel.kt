package com.aos.floney.base

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aos.floney.util.MutableEventFlow
import com.aos.floney.util.asEventFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {
    private val _baseEventFlow = MutableEventFlow<Event>()
    val baseEventFlow = _baseEventFlow.asEventFlow()
    fun baseEvent(event: Event) {
        viewModelScope.launch {
            _baseEventFlow.emit(event)
        }
    }
    sealed class Event {
        data class ShowToast(val message: String) : Event()
        data class ShowToastRes(@StringRes val message: Int) : Event()
        data class ShowSuccessToast(val message: String) : Event()
        data class ShowSuccessToastRes(@StringRes val message: Int) : Event()
        
        object ShowLoading: Event()
        object HideLoading: Event()
        object ShowCircleLoading: Event()
        object HideCircleLoading: Event()
        object ExpiredToken: Event()
    }
}