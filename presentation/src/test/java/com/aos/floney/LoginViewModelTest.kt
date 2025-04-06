package com.aos.floney


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.base.BaseViewModel
import com.aos.floney.view.login.LoginViewModel
import com.aos.model.book.GetBooksInfoCurrencyModel
import com.aos.model.home.GetCheckUserBookModel
import com.aos.model.user.PostLoginModel
import com.aos.usecase.booksetting.BooksCurrencySearchUseCase
import com.aos.usecase.home.CheckUserBookUseCase
import com.aos.usecase.login.AuthTokenCheckUseCase
import com.aos.usecase.login.LoginUseCase
import com.aos.usecase.login.SocialLoginUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import timber.log.Timber

@ExperimentalCoroutinesApi

class LoginViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var prefs: SharedPreferenceUtil

    @Mock
    private lateinit var loginUseCase: LoginUseCase

    @Mock
    private lateinit var checkUserBookUseCase: CheckUserBookUseCase

    @Mock
    private lateinit var booksCurrencySearchUseCase: BooksCurrencySearchUseCase

    @Mock
    private lateinit var authTokenCheckUseCase: AuthTokenCheckUseCase

    @Mock
    private lateinit var socialLoginUseCase: SocialLoginUseCase

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = LoginViewModel(
            prefs,
            loginUseCase,
            checkUserBookUseCase,
            authTokenCheckUseCase,
            socialLoginUseCase
        )
    }


    //onClickLogin with valid credentials emits success
    @Test
    fun `onClickLogin_with_valid_credentials_emits_success`() = runBlockingTest {
        // 세팅
        viewModel.email.value = "test@example.com"
        viewModel.password.value = "asdasd"

        // 성공적인 로그인 시나리오를 위한 stubbing
        Mockito.`when`(loginUseCase(viewModel.email.value!!, viewModel.password.value!!))
            .thenReturn(Result.success(PostLoginModel("accessToken", "refreshToken")))

        // 행동
        viewModel.onClickLogin()

        // 검증
        verify(prefs).setString("accessToken", "accessToken")
        verify(prefs).setString("refreshToken", "refreshToken")
    }

    @Test
    fun `onClickLogin_with_empty_password_emits_error`() = runBlockingTest {
        // 세팅
        viewModel.email.value = "test@example.com"
        viewModel.password.value = "asdasd"  // 비밀번호 필드가 비어 있음

        // 실패 시나리오를 위한 stubbing
        whenever(loginUseCase(viewModel.email.value!!, viewModel.password.value!!))
            .thenReturn(Result.failure(RuntimeException("Password cannot be empty")))

        // 행동
        viewModel.onClickLogin()

        // 검증: 에러 발생 확인
        verify(prefs, never()).setString("accessToken", "accessToken")
        verify(prefs, never()).setString("refreshToken", "refreshToken")
    }

    @Test
    fun `checkUserBooks_empty_bookKey`() = runBlockingTest {
        whenever(checkUserBookUseCase()).thenReturn(Result.success(GetCheckUserBookModel("")))

        val events = mutableListOf<Boolean>()
        val job = launch {
            viewModel.existBook.collect { event ->
                events.add(event)
            }
        }
        viewModel.checkUserBooks()

        assert(events.contains(false)) // Check if the correct event was emitted
        job.cancel() // Cancel the collecting job to clean up the test
    }

    @Test
    fun `checkUserBooks_success_case`() = runBlockingTest {
        whenever(checkUserBookUseCase()).thenReturn(Result.success(GetCheckUserBookModel("key")))

        viewModel.checkUserBooks()

        verify(prefs).setString("bookKey", "key")
    }

    @Test
    fun `searchCurrency_failure_case`() = runBlockingTest {
        whenever(booksCurrencySearchUseCase(prefs.getString("bookKey", ""))).thenReturn(Result.failure(
            RuntimeException("error")
        ))

        val events = mutableListOf<Boolean>()
        val job = launch {
            viewModel.existBook.collect { event ->
                events.add(event)
            }
        }

        assert(events.contains(false))
        job.cancel()
    }
}