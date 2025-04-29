package com.aos.floney.base

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatDialog
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelLazy
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.aos.floney.R
import com.aos.floney.BR
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.view.login.LoginActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import java.lang.reflect.ParameterizedType

abstract class BaseBottomSheetFragment<B : ViewDataBinding, VM : BaseViewModel>(
    @LayoutRes private val layoutResId: Int
) : BottomSheetDialogFragment() {

    private var _binding: B? = null
    protected val binding: B
        get() = _binding!!

    private val viewModelClass by lazy {
        ((javaClass.genericSuperclass as ParameterizedType?)?.actualTypeArguments?.get(1) as Class<VM>).kotlin
    }

    protected open val viewModel by ViewModelLazy(
        viewModelClass,
        { viewModelStore },
        { defaultViewModelProviderFactory },
        { defaultViewModelCreationExtras },
    )

    private val loadingDialog by lazy {
        AppCompatDialog(requireContext()).apply {
            setContentView(R.layout.item_progress_loading)
            setCancelable(false)
            window?.setDimAmount(0.2f)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            ?.let { bottomSheet ->
                BottomSheetBehavior.from(bottomSheet).isGestureInsetBottomIgnored = false
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DataBindingUtil.inflate(inflater, layoutResId, container, false)
        return binding.root
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
                    // Show Toast logic
                }

                is BaseViewModel.Event.ShowToastRes -> {
                    // Show Toast from resources logic
                }

                is BaseViewModel.Event.ShowSuccessToast -> {
                    // Show Toast logic
                }

                is BaseViewModel.Event.ShowSuccessToastRes -> {
                    // Show Toast from resources logic
                }

                is BaseViewModel.Event.ShowLoading -> showLoadingDialog()
                is BaseViewModel.Event.HideLoading -> dismissLoadingDialog()
                is BaseViewModel.Event.ExpiredToken -> {}
                BaseViewModel.Event.HideCircleLoading -> TODO()
                BaseViewModel.Event.ShowCircleLoading -> TODO()
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
        }
    }

    private fun showLoadingDialog() {
        val circle1 = loadingDialog.findViewById<View>(R.id.circle1)
        val circle2 = loadingDialog.findViewById<View>(R.id.circle2)
        val circle3 = loadingDialog.findViewById<View>(R.id.circle3)

        val animationDelay = 200L // 애니메이션 시작 지연 시간 (0.2초)
        val animationDuration = 600L // 애니메이션 지속 시간 (0.6초)

        val animator1 = ObjectAnimator.ofFloat(circle1, "translationY", 0f, -30f, 0f).apply {
            duration = animationDuration
            interpolator = AccelerateDecelerateInterpolator()
            repeatMode = ValueAnimator.RESTART
            repeatCount = 100 // 한 번만 실행
            startDelay = animationDelay * 0
        }

        val animator2 = ObjectAnimator.ofFloat(circle2, "translationY", 0f, -30f, 0f).apply {
            duration = animationDuration
            interpolator = AccelerateDecelerateInterpolator()
            repeatMode = ValueAnimator.RESTART
            repeatCount = 100 // 한 번만 실행
            startDelay = animationDelay * 1
        }

        val animator3 = ObjectAnimator.ofFloat(circle3, "translationY", 0f, -30f, 0f).apply {
            duration = animationDuration
            interpolator = AccelerateDecelerateInterpolator()
            repeatMode = ValueAnimator.RESTART
            repeatCount = 100 // 한 번만 실행
            startDelay = animationDelay * 2
        }

        // 마지막 애니메이션인 animator3에 대한 리스너 설정
        animator3.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                animator1.cancel()
                animator2.cancel()
                animator3.cancel()
                dismissLoadingDialog()
            }
        })

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
}

fun BottomSheetDialogFragment.hideKeyboard() {
    val inputMethodManager = requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    requireActivity().currentFocus?.let {
        inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
    }
}

fun BottomSheetDialogFragment.setupUI(view: View) {
    if (view !is EditText) {
        view.setOnTouchListener { _, _ ->
            hideKeyboard()
            false
        }
    }

    if (view is ViewGroup) {
        for (i in 0 until view.childCount) {
            val innerView = view.getChildAt(i)
            setupUI(innerView)
        }
    }
}