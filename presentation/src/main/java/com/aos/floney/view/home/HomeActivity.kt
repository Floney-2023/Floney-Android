package com.aos.floney.view.home

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
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
import com.aos.floney.util.getCurrentDateTimeString
import com.aos.floney.view.common.SuccessToastDialog
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

    override fun onResume() {
        super.onResume()

        val prefs = SharedPreferenceUtil(this)
        lifecycleScope.launch {
            viewModel.getBookInfo(prefs.getString("bookKey", ""))
            viewModel.getSubscribeChecking()
        }
    }

    override fun onStart() {
        super.onStart()

        Timber.e("intent.getStringExtra(\"isSave\") ${intent.getStringExtra("isSave")}")
        if(intent.getStringExtra("isSave") != null) {
            viewModel.baseEvent(BaseViewModel.Event.ShowSuccessToast("저장이 완료되었습니다."))
            intent.removeExtra("isSave")
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
    }
    private fun setUpUi() {
        binding.setVariable(BR.eventHolder, this)
        setStatusBarColor(ContextCompat.getColor(this, R.color.background3))

        if (isDarkMode()) {
            binding.root.setBackgroundColor(ContextCompat.getColor(this, R.color.background3))  // 다크 모드 대응
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
            // 내역추가
            viewModel.clickedAddHistory.collect {
                startActivity(
                    Intent(
                        this@HomeActivity,
                        HistoryActivity::class.java
                    ).putExtra("date", viewModel.getClickDate())
                        .putExtra("nickname", viewModel.getMyNickname())
                )
                if (Build.VERSION.SDK_INT >= 34) {
                    overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, R.anim.slide_in, R.anim.slide_out_down)
                } else {
                    overridePendingTransition(R.anim.slide_in, R.anim.slide_out_down)
                }
            }
        }
        repeatOnStarted {
            viewModel.settingPage.collect {
                if (it) {
                    if (mRewardAd != null) {
                        showAdMob()
                    }else{
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
                if(it) {
                    val exitDialogFragment = WarningPopupDialog(
                        getString(R.string.home_dialog_title),
                        getString(R.string.home_dialog_info),
                        getString(R.string.home_dialog_right_button),
                        getString(R.string.home_dialog_right_button),
                        true
                    ) { checked ->
                        val intent = Intent(this@HomeActivity, LoginActivity::class.java)
                        startActivity(intent)
                        if (Build.VERSION.SDK_INT >= 34) {
                            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
                        } else {
                            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        }
                        finishAffinity()
                    }
                    exitDialogFragment.show(fragmentManager, "clickDialog")
                }

            }
        }
    }
    fun goToBookSettingActivity()
    {
        startActivity(Intent(this@HomeActivity, BookSettingActivity::class.java))
        if (Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
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
        startActivity(
            Intent(
                this@HomeActivity,
                HistoryActivity::class.java
            ).putExtra(
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
                    repeatDuration = item.repeatDuration
                )
            )
        )
        if (Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, R.anim.slide_in, R.anim.slide_out_down)
        } else {
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out_down)
        }
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

        RewardedAd.load(this, BuildConfig.GOOGLE_APP_REWARD_KEY, adRequest, object : RewardedAdLoadCallback() {
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
    fun showAdMob(){
        mRewardAd?.show(this@HomeActivity, OnUserEarnedRewardListener {
            fun onUserEarnedReward(rewardItem: RewardItem) {
                val rewardAmount = rewardItem.amount
                val rewardType = rewardItem.type
            }
        })
        mRewardAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
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
    private fun setUpAccessCheck(){
        viewModel.setAccessCheck(intent.getBooleanExtra("accessCheck", false))
    }
    fun setSubscribePopup() {
        binding.includePopupSubscribe.ivExit.setOnClickListener {
            sharedPreferenceUtil.setString("subscribeCheckTenMinutes", getCurrentDateTimeString())
            binding.includePopupSubscribe.root.visibility = View.GONE
            binding.view2.visibility = View.GONE
        }
    }
}