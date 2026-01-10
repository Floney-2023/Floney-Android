package com.aos.floney.view.mypage.alarm

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.databinding.library.baseAdapters.BR
import com.aos.floney.R
import com.aos.floney.base.BaseActivity
import com.aos.floney.databinding.ActivityMyPageAlarmBinding
import com.aos.floney.ext.repeatOnStarted
import com.aos.model.alarm.UiAlarmGetModel
import com.aos.model.user.MyBooks
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyPageAlarmActivity : BaseActivity<ActivityMyPageAlarmBinding, MyPageAlarmViewModel>(R.layout.activity_my_page_alarm) {

    private lateinit var accountPagerAdapter: AccountPagerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setUpUi()
        setUpViewModelObserver()
    }

    override fun finish() {
        super.finish()
        if (Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(
                Activity.OVERRIDE_TRANSITION_CLOSE,
                R.anim.anim_slide_in_from_left_fade_in,
                android.R.anim.fade_out)
        } else {
            overridePendingTransition(R.anim.anim_slide_in_from_left_fade_in, android.R.anim.fade_out)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(
                Activity.OVERRIDE_TRANSITION_CLOSE,
                R.anim.anim_slide_in_from_left_fade_in,
                android.R.anim.fade_out)
        } else {
            overridePendingTransition(R.anim.anim_slide_in_from_left_fade_in, android.R.anim.fade_out)
        }
    }

    private fun setUpUi() {
        binding.setVariable(BR.vm, viewModel)
    }

    private fun setUpAlarmData(booksData: List<MyBooks>) {

        // 가계부 bookKey 리스트 추출
        val bookKeyList: List<String> = booksData.map { it.bookKey }

        // Adapter 설정
        accountPagerAdapter = AccountPagerAdapter(this, bookKeyList)
        binding.viewpagerAlarmView.adapter = accountPagerAdapter

        // TabLayout과 ViewPager2 연결
        TabLayoutMediator(binding.tabLayout, binding.viewpagerAlarmView) { tab, position ->
            tab.text = booksData[position].name // 가계부 탭 이름 설정
        }.attach()

        // 탭 개수에 따라 모드 변경 및 너비 설정
        when (booksData.size) {
            1 -> {
                binding.tabLayout.tabMode = TabLayout.MODE_FIXED
            }
            2 -> {
                binding.tabLayout.tabMode = TabLayout.MODE_FIXED
                adjustTabLayoutWidth(booksData.size)  // 반반씩 나누는 함수 호출
            }
            else -> {
                binding.tabLayout.tabMode = TabLayout.MODE_SCROLLABLE
                adjustTabLayoutWidth(booksData.size) // 100dp씩 너비 설정
            }
        }
    }
    // 탭의 너비를 동적으로 설정하는 함수
    private fun adjustTabLayoutWidth(tabCount: Int) {
        val tabLayoutViewGroup = binding.tabLayout.getChildAt(0) as ViewGroup
        for (i in 0 until binding.tabLayout.tabCount) {
            val tab = tabLayoutViewGroup.getChildAt(i)
            val layoutParams = tab.layoutParams as ViewGroup.MarginLayoutParams

            if (binding.tabLayout.tabCount == 2) {
                // 2개일 때는 반반씩 나누기
                layoutParams.width = 0
                binding.viewpagerAlarmView.isUserInputEnabled = false
                (layoutParams as LinearLayout.LayoutParams).weight = 1f
            } else if (binding.tabLayout.tabCount >= 3) {
                // 3개 이상일 때는 각 탭당 129dp로 고정
                layoutParams.width = dpToPx(129)  // 129dp로 고정
            }

            // 각 탭 간 간격 추가 (마지막 탭 제외)
            if (i < binding.tabLayout.tabCount - 1) {
                layoutParams.setMargins(0, 0, dpToPx(16), 0)  // 16dp 간격 추가
            }

            tab.layoutParams = layoutParams
        }
    }

    // dp를 px로 변환하는 함수
    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun setUpViewModelObserver() {
        repeatOnStarted {
            viewModel.back.collect {
                if(it) {
                    finish()
                }
            }
        }
        repeatOnStarted {
            // 가계부 정보 불러올 경우 실행
            viewModel.complete.collect {
                if(it) {
                    viewModel.bookList.value?.let { it1 -> setUpAlarmData(it1) } // 가계부 이름 탭바 설정
                }
            }
        }
    }
}