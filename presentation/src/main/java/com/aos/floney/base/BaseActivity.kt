package com.aos.floney.base

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDialog
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelLazy
import com.aos.floney.BR
import com.aos.floney.R
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.ext.setupTouchEffect
import com.aos.floney.util.LottieLoadingManager
import com.aos.floney.view.common.ErrorToastDialog
import com.aos.floney.view.common.SuccessToastDialog
import com.aos.floney.view.login.LoginActivity
import com.aos.floney.view.splash.SplashActivity
import timber.log.Timber
import java.lang.reflect.ParameterizedType
import javax.inject.Inject

abstract class BaseActivity<B : ViewDataBinding, VM : BaseViewModel>(
    @LayoutRes private val layoutResId: Int,
) : AppCompatActivity() {

    protected lateinit var binding: B

    @Inject
    lateinit var lottieLoadingManager: LottieLoadingManager

    private val viewModelClass = ((javaClass.genericSuperclass as ParameterizedType?)
        ?.actualTypeArguments
        ?.get(1) as Class<VM>).kotlin

    protected open val viewModel by ViewModelLazy(
        viewModelClass,
        { viewModelStore },
        { defaultViewModelProviderFactory },
        { defaultViewModelCreationExtras },
    )

    private val loadingDialog by lazy {
        AppCompatDialog(this).apply {
            setContentView(R.layout.item_progress_loading)
            setCancelable(false)
            window?.setDimAmount(0.7f)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setUpEdgeToEdge()
        setupUi()
        setupObserve()
    }

    override fun onStart() {
        super.onStart()
        setupUI(binding.root)
    }

    private fun setUpEdgeToEdge(){
        val contentView = findViewById<android.view.View>(android.R.id.content)
        if (contentView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(contentView) { view, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

                // 시스템 바 크기만큼 루트 뷰의 자식 뷰(실제 콘텐츠)에 패딩 적용
                val firstChild = (view as? android.view.ViewGroup)?.getChildAt(0)
                firstChild?.setPadding(insets.left, insets.top, insets.right, insets.bottom)

                WindowInsetsCompat.CONSUMED
            }
        }
    }

    private fun setupUi() {
        binding = DataBindingUtil.setContentView(this, layoutResId)
        with(binding) {
            setVariable(BR.vm, viewModel)
            lifecycleOwner = this@BaseActivity
        }
        if (isDarkMode() && this !is SplashActivity) {
            binding.root.setBackgroundColor(Color.WHITE)  // 다크 모드일 때 흰색 배경 (스플래시일 때는 X)
        }
        setStatusBarColor(ContextCompat.getColor(this, R.color.white))
    }

    private fun setupObserve() {
        repeatOnStarted {
            viewModel.baseEventFlow.collect {
                handleEvent(it)
            }
        }
    }

    private fun handleEvent(event: BaseViewModel.Event) {
        try {
            when (event) {
                is BaseViewModel.Event.ShowToast -> {
                    if (event.message != "") {
                        val errorToastDialog = ErrorToastDialog(this, event.message)
                        errorToastDialog.show()

                        Handler(Looper.myLooper()!!).postDelayed({
                            errorToastDialog.dismiss()
                        }, 2000)
                    }
                }

                is BaseViewModel.Event.ShowToastRes -> {
                    val errorToastDialog = ErrorToastDialog(this, getString(event.message))
                    errorToastDialog.show()

                    Handler(Looper.myLooper()!!).postDelayed({
                        errorToastDialog.dismiss()
                    }, 2000)
                }

                is BaseViewModel.Event.ShowSuccessToast -> {
                    val successToastDialog = SuccessToastDialog(this, event.message)
                    successToastDialog.show()

                    Handler(Looper.myLooper()!!).postDelayed({
                        successToastDialog.dismiss()
                    }, 2000)
                }

                is BaseViewModel.Event.ShowSuccessToastRes -> {
                    val successToastDialog = SuccessToastDialog(this, getString(event.message))
                    successToastDialog.show()

                    Handler(Looper.myLooper()!!).postDelayed({
                        successToastDialog.dismiss()
                    }, 2000)
                }

                is BaseViewModel.Event.ShowLoading -> showLoadingDialog()
                is BaseViewModel.Event.HideLoading -> dismissLoadingDialog()
                is BaseViewModel.Event.ExpiredToken -> {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finishAffinity()
                }

                BaseViewModel.Event.HideCircleLoading -> hideCircleLoading()
                BaseViewModel.Event.ShowCircleLoading -> showCircleLoading()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showLoadingDialog() {
        val circle1 = loadingDialog.findViewById<View>(R.id.circle1)
        val circle2 = loadingDialog.findViewById<View>(R.id.circle2)
        val circle3 = loadingDialog.findViewById<View>(R.id.circle3)

        val animationDelay = 200L // 애니메이션 시작 지연 시간 (0.2초)
        val animationDuration = 600L * 2 // 애니메이션 지속 시간 (0.6초)

        val animator1 = ObjectAnimator.ofFloat(circle1, "translationY", 0f, -50f, 0f).apply {
            duration = animationDuration
            interpolator = AccelerateDecelerateInterpolator()
            repeatMode = ValueAnimator.REVERSE
            repeatCount = 100
            startDelay = animationDelay * 0
        }

        val animator2 = ObjectAnimator.ofFloat(circle2, "translationY", 0f, -50f, 0f).apply {
            duration = animationDuration
            interpolator = AccelerateDecelerateInterpolator()
            repeatMode = ValueAnimator.REVERSE
            repeatCount = 100
            startDelay = animationDelay * 1
        }

        val animator3 = ObjectAnimator.ofFloat(circle3, "translationY", 0f, -50f, 0f).apply {
            duration = animationDuration
            interpolator = AccelerateDecelerateInterpolator()
            repeatMode = ValueAnimator.REVERSE
            repeatCount = 100
            startDelay = animationDelay * 2
        }

        // 애니메이션들을 함께 실행
        animator1.start()
        animator2.start()
        animator3.start()

        loadingDialog.show()
    }

    fun dismissLoadingDialog() {
        try {
            loadingDialog.dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showCircleLoading() {
        try {
            if (::lottieLoadingManager.isInitialized) {
                lottieLoadingManager.showLoading(this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideCircleLoading() {
        try {
            if (::lottieLoadingManager.isInitialized) {
                lottieLoadingManager.hideLoading()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 상태바 색상 설정 함수
    protected fun setStatusBarColor(color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = color
        }
    }
    // 다크 모드인지 확인하는 함수
    protected fun isDarkMode(): Boolean {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }
}

fun AppCompatActivity.hideKeyboard() {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    currentFocus?.let {
        inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
    }
}

fun AppCompatActivity.setupUI(view: View) {
    if (view !is EditText) {
        view.setOnTouchListener { _, _ ->
            hideKeyboard()
            false
        }
    }

    if (view is ViewGroup) {
        for (i in 0 until view.childCount) {
            val innerView = view.getChildAt(i)
            if (innerView is Button) {
                innerView.setupTouchEffect()
            }
        }
    }
}