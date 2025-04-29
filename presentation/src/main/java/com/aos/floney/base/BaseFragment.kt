package com.aos.floney.base

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.ChangeBounds
import android.transition.Fade
import android.transition.Slide
import android.transition.TransitionSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.lifecycle.ViewModelLazy
import com.aos.floney.BR
import com.aos.floney.R
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.ext.setupTouchEffect
import com.aos.floney.util.LottieLoadingManager
import com.aos.floney.view.common.ErrorToastDialog
import com.aos.floney.view.common.SuccessToastDialog
import com.aos.floney.view.login.LoginActivity
import com.google.android.material.transition.MaterialFadeThrough
import com.google.android.material.transition.MaterialSharedAxis
import timber.log.Timber
import java.lang.reflect.ParameterizedType
import javax.inject.Inject

abstract class BaseFragment<B : ViewDataBinding, VM : BaseViewModel>(
    @LayoutRes private val layoutResId: Int,
) : Fragment(layoutResId) {

    private var _binding: B? = null
    protected val binding: B
        get() = _binding!!

    private val viewModelClass =
        ((javaClass.genericSuperclass as ParameterizedType?)?.actualTypeArguments?.get(1) as Class<VM>).kotlin

    protected open val viewModel by ViewModelLazy(
        viewModelClass,
        { viewModelStore },
        { defaultViewModelProviderFactory },
        { defaultViewModelCreationExtras },
    )

    @Inject
    lateinit var lottieLoadingManager: LottieLoadingManager

    private val loadingDialog by lazy {
        AppCompatDialog(requireContext()).apply {
            setContentView(R.layout.item_progress_loading)
            setCancelable(false)
            window?.setDimAmount(0.7f)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    protected open val applyTransition: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        _binding = DataBindingUtil.bind(view!!)!!


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUi()
        setupObserve()
        setupUI(view)
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
                        val errorToastDialog = ErrorToastDialog(requireContext(), event.message)
                        errorToastDialog.show()

                        Handler(Looper.myLooper()!!).postDelayed({
                            errorToastDialog.dismiss()
                        }, 2000)
                    }
                }

                is BaseViewModel.Event.ShowToastRes -> {
                    val errorToastDialog =
                        ErrorToastDialog(requireContext(), getString(event.message))
                    errorToastDialog.show()

                    Handler(Looper.myLooper()!!).postDelayed({
                        errorToastDialog.dismiss()
                    }, 2000)
                }

                is BaseViewModel.Event.ShowSuccessToast -> {
                    val successToastDialog = SuccessToastDialog(requireContext(), event.message)
                    successToastDialog.show()

                    Handler(Looper.myLooper()!!).postDelayed({
                        successToastDialog.dismiss()
                    }, 2000)
                }

                is BaseViewModel.Event.ShowSuccessToastRes -> {
                    val successToastDialog =
                        SuccessToastDialog(requireContext(), getString(event.message))
                    successToastDialog.show()

                    Handler(Looper.myLooper()!!).postDelayed({
                        successToastDialog.dismiss()
                    }, 2000)
                }

                is BaseViewModel.Event.ShowLoading -> showLoadingDialog()
                is BaseViewModel.Event.HideLoading -> dismissLoadingDialog()
                is BaseViewModel.Event.ExpiredToken -> {
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                    requireActivity().finishAffinity()
                }

                BaseViewModel.Event.HideCircleLoading -> hideCircleLoading()
                BaseViewModel.Event.ShowCircleLoading -> showCircleLoading()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupUi() {
        with(binding) {
            setVariable(BR.vm, viewModel)
            lifecycleOwner = viewLifecycleOwner

            if (applyTransition) {
                enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true).apply {
                    duration = 350L
                    interpolator = DecelerateInterpolator()
                }

                returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false).apply {
                    duration = 350L
                    interpolator = DecelerateInterpolator()
                }
            }
        }
    }

    fun showLoadingDialog() {
        val circle1 = loadingDialog.findViewById<View>(R.id.circle1)
        val circle2 = loadingDialog.findViewById<View>(R.id.circle2)
        val circle3 = loadingDialog.findViewById<View>(R.id.circle3)

        // 다시 시작할 시, reset
        circle1?.translationY = 0f
        circle2?.translationY = 0f
        circle3?.translationY = 0f

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
    fun dismissLoadingDialog(){
        try {
            loadingDialog.dismiss()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showCircleLoading() {
        try {
            if (::lottieLoadingManager.isInitialized) {
                lottieLoadingManager.showLoading(requireActivity())
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

    protected open fun onBackPressed() {
        // 상속받는 클래스에서 필요에 따라 구현
    }
}

fun Fragment.hideKeyboard() {
    val inputMethodManager = requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    requireActivity().currentFocus?.let {
        inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
    }
}

fun Fragment.setupUI(view: View) {

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