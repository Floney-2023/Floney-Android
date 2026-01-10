package com.aos.floney

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.view.settleup.SettleUpCompleteViewModel
import com.aos.model.settlement.UiSettlementAddModel
import com.aos.model.settlement.settleOutcomes
import com.aos.usecase.booksetting.BooksCodeCheckUseCase
import com.aos.usecase.booksetting.BooksSettingGetUseCase
import com.aos.usecase.mypage.AlarmSaveGetUseCase
import com.aos.usecase.settlement.SettlementAddUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class SettleUpUnitTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var prefs: SharedPreferenceUtil

    @Mock
    private lateinit var settlementAddUseCase: SettlementAddUseCase

    @Mock
    private lateinit var booksSettingGetUseCase: BooksSettingGetUseCase

    @Mock
    private lateinit var alarmSaveGetUseCase: AlarmSaveGetUseCase

    @Mock
    private lateinit var booksCodeCheckUseCase: BooksCodeCheckUseCase

    private lateinit var viewModel: SettleUpCompleteViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        viewModel = SettleUpCompleteViewModel(
            stateHandle = SavedStateHandle(),
            prefs = prefs,
            settlementAddUseCase = settlementAddUseCase,
            booksSettingGetUseCase = booksSettingGetUseCase,
            alarmSaveGetUseCase = alarmSaveGetUseCase,
            booksCodeCheckUseCase = booksCodeCheckUseCase
        )
    }
}
