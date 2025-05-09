package com.aos.floney.view.splash

import com.aos.data.util.AuthInterceptor
import com.aos.floney.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authInterceptor: AuthInterceptor
): BaseViewModel() {

    fun getSessionExpiredFlag(): Boolean {
        return authInterceptor.getSessionExpiredFlag()
    }
}