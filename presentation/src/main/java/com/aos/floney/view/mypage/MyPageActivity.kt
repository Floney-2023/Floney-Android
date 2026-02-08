package com.aos.floney.view.mypage

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.library.baseAdapters.BR
import com.aos.floney.R
import com.aos.floney.base.BaseActivity
import com.aos.floney.base.BaseViewModel
import com.aos.floney.databinding.ActivityMyPageBinding
import com.aos.floney.ext.applyHistoryOpenTransition
import com.aos.floney.ext.applyOpenTransition
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.util.getCurrentDateTimeString
import com.aos.floney.view.analyze.AnalyzeActivity
import com.aos.floney.view.history.HistoryActivity
import com.aos.floney.view.home.HomeActivity
import com.aos.floney.view.home.HomeMonthTypeFragment
import com.aos.floney.view.mypage.alarm.MyPageAlarmActivity
import com.aos.floney.view.mypage.bookadd.MypageBookAddSelectBottomSheetFragment
import com.aos.floney.view.mypage.bookadd.codeinput.MyPageBookCodeInputActivity
import com.aos.floney.view.mypage.bookadd.create.MyPageBookCreateActivity
import com.aos.floney.view.mypage.inform.MyPageInformActivity
import com.aos.floney.view.mypage.main.service.MyPageServicePrivacyFragment
import com.aos.floney.view.mypage.main.service.MyPageServiceTermsFragment
import com.aos.floney.view.mypage.main.service.MyPageServiceTermsViewModel
import com.aos.floney.view.mypage.setting.MyPageSettingActivity
import com.aos.floney.view.settleup.SettleUpActivity
import com.aos.floney.view.subscribe.SubscribeInformActivity
import com.aos.floney.view.subscribe.SubscribePlanActivity
import com.aos.model.user.MyBooks
import com.aos.model.user.UiMypageSearchModel
import com.aos.model.user.UserModel.userNickname
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyPageActivity : BaseActivity<ActivityMyPageBinding, MyPageViewModel>(R.layout.activity_my_page) {

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val isSaved = result.data?.getBooleanExtra("isSave", false) ?: false
            if (isSaved) {
                viewModel.baseEvent(BaseViewModel.Event.ShowSuccessToast(getString(R.string.toast_save_completed)))
                result.data?.removeExtra("isSave")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setUpUi()
        setUpBottomNavigation()
        setUpViewModelObserver()
        setSubscribePopup()
    }
    private fun setUpViewModelObserver() {
        repeatOnStarted {
            // 내역추가
            viewModel.clickedAddHistory.collect {
                val intent = Intent(this@MyPageActivity, HistoryActivity::class.java).apply {
                    putExtra("date", it)
                    putExtra("nickname", userNickname)
                }
                launcher.launch(intent)
                applyHistoryOpenTransition()
            }
        }
    }

    private fun setUpUi() {
        binding.setVariable(BR.eventHolder, this@MyPageActivity)
    }

    fun startAlarmActivity() {
        startActivity(Intent(this, MyPageAlarmActivity::class.java))
        applyOpenTransition()
    }
    
    fun startInformActivity(){
        startActivity(Intent(this, MyPageInformActivity::class.java))
        applyOpenTransition()
    }

    fun startSettingActivity() {
        startActivity(Intent(this, MyPageSettingActivity::class.java))
        applyOpenTransition()
    }

    fun startBottomSheet() {
        val bottomSheetFragment = MypageBookAddSelectBottomSheetFragment { checked ->
            val intentClass = if (checked) MyPageBookCreateActivity::class.java else MyPageBookCodeInputActivity::class.java
            startActivity(Intent(this, intentClass))
            applyOpenTransition()
        }
        bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
    }

    fun startSubscribeInformActivity() {
        startActivity(Intent(this, SubscribeInformActivity::class.java))
        applyOpenTransition()
    }

    fun startSubscribePlanActivity() {
        startActivity(Intent(this, SubscribePlanActivity::class.java))
        applyOpenTransition()
    }

    private fun setUpBottomNavigation() {
        // 가운데 메뉴(내역추가)에 대한 터치 이벤트를 막기 위한 로직
        binding.bottomNavigationView.apply {
            menu.getItem(2).isEnabled = false
            selectedItemId = R.id.mypageFragment
        }

        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.homeFragment -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    applyOpenTransition()
                    finish()
                    false
                }
                R.id.analysisFragment -> {
                    startActivity(Intent(this, AnalyzeActivity::class.java))
                    applyOpenTransition()
                    finish()
                    false
                }
                R.id.settleUpFragment -> {
                    startActivity(Intent(this, SettleUpActivity::class.java))
                    applyOpenTransition()
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
    
    private fun setSubscribePopup() {
        binding.includePopupSubscribe.ivExit.setOnClickListener {
            viewModel.showSubscribePopupIfNeeded(false)
        }
    }
}