package com.aos.floney.view.home

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.lifecycleScope
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.BuildConfig
import com.aos.floney.R
import com.aos.floney.base.BaseActivity
import com.aos.floney.databinding.ActivityHomeBinding
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.view.analyze.AnalyzeActivity
import com.aos.floney.view.analyze.ChoiceDatePickerBottomSheet
import com.aos.floney.view.book.setting.BookSettingActivity
import com.aos.floney.view.history.HistoryActivity
import com.aos.floney.view.mypage.MyPageActivity
import com.aos.floney.view.settleup.SettleUpActivity
import com.aos.model.home.DayMoney
import com.aos.model.home.DayMoneyModifyItem
import com.aos.model.home.MonthMoney
import com.aos.model.home.UiBookDayModel
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dagger.hilt.android.AndroidEntryPoint
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.applyHistoryOpenTransition
import com.aos.floney.ext.applyOpenTransition
import com.aos.floney.util.getCurrentDateTimeString
import com.aos.floney.view.common.WarningPopupDialog
import com.aos.floney.view.login.LoginActivity
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : BaseActivity<ActivityHomeBinding, HomeViewModel>(R.layout.activity_home),
    UiBookDayModel.OnItemClickListener {

    @Inject
    lateinit var sharedPreferenceUtil: SharedPreferenceUtil
    private val fragmentManager = supportFragmentManager
    private var mRewardAd: RewardedAd? = null


    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val isSaved = result.data?.getBooleanExtra("isSave", false) ?: false
            if (isSaved) {
                viewModel.baseEvent(BaseViewModel.Event.ShowSuccessToast("저장이 완료되었습니다."))
                result.data?.removeExtra("isSave")
            }

            if (binding.clShowDetail.isVisible) // 일별 bottomSheet이 열려있는 경우 다시 일별 데이터 호출한다.
                viewModel.getBookDays(viewModel.getFormatDateDay())
        }
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            viewModel.setUserSubscribeChecking()
            viewModel.getBookInfoData() // 정보 업데이트
        }
    }

    override fun onStart() {
        super.onStart()

        Timber.e("intent.getStringExtra(\"isSave\") ${intent.getStringExtra("isSave")}")
        if (intent.getStringExtra("isSave") != null) {
            viewModel.baseEvent(BaseViewModel.Event.ShowSuccessToast("저장이 완료되었습니다."))
            intent.removeExtra("isSave")
        } else if (intent.getStringExtra("isDelete") != null) {
            viewModel.baseEvent(BaseViewModel.Event.ShowToast("내역 삭제가 완료되었습니다."))
            intent.removeExtra("isDelete")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setUpUi()
        setUpViewModelObserver()
        setUpBottomNavigation()
        setUpAccessCheck()
        setUpAdMob()
        setSubscribePopup()
        setUpBackPressedCallBack()
    }

    private fun setUpBackPressedCallBack()
    {
        onBackPressedDispatcher.addCallback(this) {
            if (binding.includePopupSubscribe.root.isVisible){
                viewModel.changeSubscribePopupShow(false)
            }
            else if (binding.clShowDetail.isVisible) {
                viewModel.onClickCloseShowDetail() // clShowDetail 숨기기 위한 처리
            } else {
                isEnabled = false // 콜백 비활성화 → 시스템 기본 백 동작 수행
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun setUpUi() {
        binding.setVariable(BR.eventHolder, this)
        viewModel.getBookInfoData() // 정보 업데이트

        setStatusBarColor(ContextCompat.getColor(this, R.color.background3))

        if (isDarkMode()) {
            binding.root.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.background3
                )
            )  // 다크 모드 대응
        }
    }

    private fun setUpViewModelObserver() {
        viewModel.clickedShowType.observe(this) { showType ->
            when (showType) {
                "month" -> {
                    viewModel.initCalendarMonth()
                    fragmentManager.beginTransaction()
                        .replace(R.id.fl_container, HomeMonthTypeFragment()).commit()
                }

                "day" -> {
                    viewModel.initCalendarDay()
                    fragmentManager.beginTransaction()
                        .replace(R.id.fl_container, HomeDayTypeFragment()).commit()
                }
            }
        }

        repeatOnStarted {
            viewModel.clickedAddHistory.collect {
                if (viewModel.subscribeExpired.value!!) // 구독 만료일 경우, 구독 유도 팝업 표시
                {
                    viewModel.changeSubscribePopupShow(true) // 팝업 표시
                    viewModel.subscribePopupEnter.value = false // 진입 시 표시되는 팝업이 아님
                    viewModel.onClickCloseShowDetail() // bottomSheet 올라왔을 경우 닫기
                }else { // 만료안된 경우, 내역 추가 화면 이동
                    val intent = Intent(this@HomeActivity, HistoryActivity::class.java).apply {
                        putExtra("date", viewModel.getClickDate())
                        putExtra("nickname", viewModel.getMyNickname())
                    }
                    launcher.launch(intent)
                    applyHistoryOpenTransition()
                }
            }
        }
        repeatOnStarted {
            viewModel.settingPage.collect {
                if (it) {
                    if (mRewardAd != null) {
                        showAdMob()
                    } else {
                        resetUpAdMob()
                    }
                } else {
                    goToBookSettingActivity()
                }
            }
        }
        repeatOnStarted {
            // 가계부 설정 페이지 이동
            viewModel.clickedChoiceDate.collect {
                ChoiceDatePickerBottomSheet(this@HomeActivity, it) {
                    // 결과값
                    val item = it.split("-")
                    viewModel.updateCalendarClickedItem(item[0].toInt(), item[1].toInt(), 1)
                }.show()
            }
        }
        repeatOnStarted {
            viewModel.accessCheck.collect {
                if (it) {
                    val exitDialogFragment = WarningPopupDialog(
                        getString(R.string.home_dialog_title),
                        getString(R.string.home_dialog_info),
                        "",
                        getString(R.string.home_dialog_right_button),
                        true
                    ) { checked ->
                        val intent = Intent(this@HomeActivity, LoginActivity::class.java)
                        startActivity(intent)
                        applyOpenTransition()
                        finishAffinity()
                    }
                    exitDialogFragment.show(fragmentManager, "clickDialog")
                }

            }
        }

        viewModel.showOverlay.observe(this) { show ->
            changeStatusBarColor(show)
        }
    }

    private fun changeStatusBarColor(isShow :Boolean){
        if (isShow) {
            setStatusBarColor(ContextCompat.getColor(this@HomeActivity, R.color.background_dim))
        } else {
            setStatusBarColor(ContextCompat.getColor(this@HomeActivity, R.color.background3))
        }
    }

    fun goToBookSettingActivity() {
        startActivity(Intent(this@HomeActivity, BookSettingActivity::class.java))
        if (Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(
                Activity.OVERRIDE_TRANSITION_OPEN,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
        } else {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    // 캘린더 아이템이 표시됨
    fun onClickCalendarItem(item: MonthMoney) {
        viewModel.onClickShowDetail(item)
    }

    // 일별내역 아이템 클릭
    fun onClickDayItem(item: DayMoney) {
        val intent = Intent(this@HomeActivity, HistoryActivity::class.java).apply {
            putExtra(
                "dayItem", DayMoneyModifyItem(
                    id = item.id,
                    money = item.money,
                    description = item.description,
                    lineDate = viewModel.getClickDate(),
                    lineCategory = item.lineCategory,
                    lineSubCategory = item.lineSubCategory,
                    assetSubCategory = item.assetSubCategory,
                    exceptStatus = item.exceptStatus,
                    writerNickName = item.writerNickName,
                    repeatDuration = item.repeatDuration,
                    memo = item.memo,
                    imageUrls = item.imageUrls
                )
            )
        }
        launcher.launch(intent)
        applyHistoryOpenTransition()
    }

    override fun onItemClick(item: DayMoney) {
        onClickDayItem(item)
    }

    private fun setUpBottomNavigation() {
        // 가운데 메뉴(제보하기)에 대한 터치 이벤트를 막기 위한 로직
        binding.bottomNavigationView.apply {
            menu.getItem(2).isEnabled = false
            selectedItemId = R.id.homeFragment
        }

        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.analysisFragment -> {
                    startActivity(Intent(this, AnalyzeActivity::class.java))
                    if (Build.VERSION.SDK_INT >= 34) {
                        overrideActivityTransition(
                            Activity.OVERRIDE_TRANSITION_OPEN,
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                        )
                    } else {
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                    finish()
                    false
                }

                R.id.settleUpFragment -> {
                    startActivity(Intent(this, SettleUpActivity::class.java))
                    if (Build.VERSION.SDK_INT >= 34) {
                        overrideActivityTransition(
                            Activity.OVERRIDE_TRANSITION_OPEN,
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                        )
                    } else {
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                    finish()
                    false
                }

                R.id.mypageFragment -> {
                    startActivity(Intent(this, MyPageActivity::class.java))
                    if (Build.VERSION.SDK_INT >= 34) {
                        overrideActivityTransition(
                            Activity.OVERRIDE_TRANSITION_OPEN,
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                        )
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

    private fun setUpAdMob() {
        showLoadingDialog()
        MobileAds.initialize(this) { initializationStatus ->
            loadBannerAd()
            loadRewardedAd()
        }
    }

    private fun loadBannerAd() {
        val adView = AdView(this)
        adView.setAdSize(AdSize.BANNER)
        adView.adUnitId = BuildConfig.GOOGLE_APP_BANNER_KEY

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        binding.adBanner.addView(adView)
    }

    private fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            this,
            BuildConfig.GOOGLE_APP_REWARD_KEY,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mRewardAd = null
                    Timber.e("광고 로드 실패: ${adError.message}")
                    dismissLoadingDialog()
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    mRewardAd = ad
                    Timber.d("리워드 광고 로드 성공")
                    dismissLoadingDialog()
                }
            }
        )
    }

    private fun resetUpAdMob() {
        showLoadingDialog()
        MobileAds.initialize(this)

        val adRequest = AdRequest.Builder().build()
        //binding.adBanner.loadAd(adRequest)

        RewardedAd.load(
            this,
            BuildConfig.GOOGLE_APP_REWARD_KEY,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    mRewardAd = null
                    // 광고 로드 실패하더라도 페이지 이동
                    Timber.e("광고가 아직 로드되지 않음 reset")
                    dismissLoadingDialog()
                    goToBookSettingActivity()
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    dismissLoadingDialog()
                    mRewardAd = ad
                    showAdMob()
                    // 광고 로드 성공 시 로그 출력
                    Timber.e("광고가 로드됨")
                }
            })
    }

    fun showAdMob() {
        mRewardAd?.show(this@HomeActivity, OnUserEarnedRewardListener {
            fun onUserEarnedReward(rewardItem: RewardItem) {
                val rewardAmount = rewardItem.amount
                val rewardType = rewardItem.type
            }
        })
        mRewardAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                dismissLoadingDialog()

                viewModel.updateAdvertiseTenMinutes()
                goToBookSettingActivity()
                mRewardAd = null
                Timber.e("광고가 로드됨")
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                dismissLoadingDialog()
                mRewardAd = null
                Timber.e("광고가 아직 로드되지 않음 1-2")
                goToBookSettingActivity()
            }
        }
    }

    private fun setUpAccessCheck() {
        viewModel.setAccessCheck(intent.getBooleanExtra("accessCheck", false))
    }

    fun setSubscribePopup() {
        binding.includePopupSubscribe.ivExit.setOnClickListener {
            // 진입 시 표시되는 팝업일 경우에만 시간 체크
            if (viewModel.subscribePopupEnter.value == true)
                sharedPreferenceUtil.setString(
                    "subscribeCheckTenMinutes",
                    getCurrentDateTimeString()
                )
            viewModel.changeSubscribePopupShow(false)
        }
    }
}