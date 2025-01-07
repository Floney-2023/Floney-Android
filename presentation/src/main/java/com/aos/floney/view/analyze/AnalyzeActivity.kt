package com.aos.floney.view.analyze

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.aos.floney.R
import com.aos.floney.base.BaseActivity
import com.aos.floney.databinding.ActivityAnalyzeBinding
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.view.book.setting.budget.BookSettingBudgetFragment
import com.aos.floney.view.history.HistoryActivity
import com.aos.floney.view.home.HomeActivity
import com.aos.floney.view.home.HomeDayTypeFragment
import com.aos.floney.view.mypage.MyPageActivity
import com.aos.floney.view.settleup.SettleUpActivity
import com.aos.model.user.UserModel.userNickname
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class AnalyzeActivity : BaseActivity<ActivityAnalyzeBinding, AnalyzeViewModel>(R.layout.activity_analyze), BookSettingBudgetFragment.OnFragmentInteractionListener{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setUpBottomNavigation()
        setUpViewModelObserver()
        setSubscribePopup()
    }

    private fun setUpBottomNavigation() {
        // 가운데 메뉴(제보하기)에 대한 터치 이벤트를 막기 위한 로직
        binding.bottomNavigationView.apply {
            menu.getItem(2).isEnabled = false
            selectedItemId = R.id.analysisFragment
        }

        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.homeFragment -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    if (Build.VERSION.SDK_INT >= 34) {
                        overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
                    } else {
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                    finish()
                    false
                }
                R.id.settleUpFragment -> {
                    startActivity(Intent(this, SettleUpActivity::class.java))
                    if (Build.VERSION.SDK_INT >= 34) {
                        overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
                    } else {
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                    finish()
                    false
                }
                R.id.mypageFragment -> {
                    startActivity(Intent(this, MyPageActivity::class.java))
                    if (Build.VERSION.SDK_INT >= 34) {
                        overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
                    } else {
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                    finish()
                    false
                }

                else -> false
            }
        }

        binding.bottomNavigationView.setOnItemReselectedListener {
            when (it.itemId) {
                R.id.homeFragment -> {}
                R.id.analysisFragment -> {}
                R.id.settleUpFragment -> {}
                R.id.mypageFragment -> {}
            }
        }
    }
    
    private fun setUpViewModelObserver() {
        viewModel.flow.observe(this) {
            when(it) {
                "지출" -> {
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
                        .replace(R.id.fl_container, AnalyzeOutComeFragment()).commit()
                }
                "수입" -> {
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
                        .replace(R.id.fl_container, AnalyzeIncomeFragment()).commit()
                }
                "예산" -> {
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
                        .replace(R.id.fl_container, AnalyzePlanFragment()).commit()
                }
                "자산" -> {
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
                        .replace(R.id.fl_container, AnalyzeAssetFragment()).commit()
                }
            }
        }

        viewModel.onClickSetBudget.observe(this) {
            if(it) {
                binding.flContainer2.isVisible = true
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
                    .replace(R.id.fl_container2, BookSettingBudgetFragment()).commit()
            } else {
                binding.flContainer2.isVisible = false
                val fragmentToRemove = supportFragmentManager.findFragmentById(R.id.fl_container2)
                if (fragmentToRemove != null) {
                    supportFragmentManager.beginTransaction().remove(fragmentToRemove).commit()
                }
            }
        }

        repeatOnStarted {
            viewModel.onClickChoiceDate.collect {
                ChoiceDatePickerBottomSheet(this@AnalyzeActivity, it) {
                    // 결과값
                    val item = it.split("-")
                    viewModel.updateCalendarClickedItem(item[0].toInt(), item[1].toInt())
                }.show()
            }
        }

        repeatOnStarted {
            // 내역추가
            viewModel.clickedAddHistory.collect {
                startActivity(
                    Intent(
                        this@AnalyzeActivity,
                        HistoryActivity::class.java
                    ).putExtra("date", it)
                        .putExtra("nickname", userNickname)
                )
                if (Build.VERSION.SDK_INT >= 34) {
                    overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, R.anim.slide_in, R.anim.slide_out_down)
                } else {
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out_down)
                }
            }
        }
    }

    override fun onFragmentRemoved() {
        viewModel.onClickSetBudget(false)
    }

    fun setSubscribePopup() {
        if(true){
            binding.includePopupSubscribe.ivExit.setOnClickListener {
                binding.includePopupSubscribe.root.visibility = View.GONE
                binding.dimBackground.visibility = View.GONE
            }
        }
    }
}