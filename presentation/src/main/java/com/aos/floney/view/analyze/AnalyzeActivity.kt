package com.aos.floney.view.analyze

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.BuildConfig
import com.aos.floney.R
import com.aos.floney.base.BaseActivity
import com.aos.floney.base.BaseViewModel
import com.aos.floney.databinding.ActivityAnalyzeBinding
import com.aos.floney.ext.applyHistoryOpenTransition
import com.aos.floney.ext.applyOpenTransition
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.util.getCurrentDateTimeString
import com.aos.floney.view.book.setting.budget.BookSettingBudgetFragment
import com.aos.floney.view.history.HistoryActivity
import com.aos.floney.view.home.HomeActivity
import com.aos.floney.view.home.HomeDayTypeFragment
import com.aos.floney.view.mypage.MyPageActivity
import com.aos.floney.view.settleup.SettleUpActivity
import com.aos.floney.view.subscribe.SubscribePlanActivity
import com.aos.model.user.UserModel.userNickname
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject
import androidx.core.view.get
import com.aos.floney.util.getAdvertiseCheck
import com.aos.floney.util.getAdvertiseTenMinutesCheck

@AndroidEntryPoint
class AnalyzeActivity :
    BaseActivity<ActivityAnalyzeBinding, AnalyzeViewModel>(R.layout.activity_analyze),
    BookSettingBudgetFragment.OnFragmentInteractionListener {

    @Inject
    lateinit var sharedPreferenceUtil: SharedPreferenceUtil
    private var mInterstitialAd: InterstitialAd? = null
    
    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val isSaved = result.data?.getBooleanExtra("isSave", false) ?: false
                if (isSaved) {
                    viewModel.baseEvent(BaseViewModel.Event.ShowSuccessToast(getString(R.string.toast_save_completed)))
                    result.data?.removeExtra("isSave")

                    // 데이터를 다시 불러온다.
                    viewModel.onClickFlow(viewModel.flow.value!!)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(shouldShowAd()) {
            // 광고를 보여줘야 하는 경우, 광고부터 로드
            setAdMob()
        } else {
            // 광고를 보여주지 않는 경우, 바로 초기화
            initializeScreen()
        }
    }

    private fun initializeScreen() {
        setUpBottomNavigation()
        setUpViewModelObserver()
        setSubscribePopup()
    }

    private fun setUpBottomNavigation() {
        binding.bottomNavigationView.apply {
            menu[2].isEnabled = false
            selectedItemId = R.id.analysisFragment
        }

        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.homeFragment -> {
                    startActivity(Intent(this, HomeActivity::class.java))
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

                R.id.mypageFragment -> {
                    startActivity(Intent(this, MyPageActivity::class.java))
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

    fun goToSubscribePlanActivity() {
        startActivity(Intent(this, SubscribePlanActivity::class.java))
        applyOpenTransition()
    }

    private fun setUpViewModelObserver() {
        viewModel.flow.observe(this) {
            when (it) {
                "지출" -> {
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            androidx.appcompat.R.anim.abc_fade_in,
                            androidx.appcompat.R.anim.abc_fade_out
                        )
                        .replace(R.id.fl_container, AnalyzeOutComeFragment()).commit()
                }

                "수입" -> {
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            androidx.appcompat.R.anim.abc_fade_in,
                            androidx.appcompat.R.anim.abc_fade_out
                        )
                        .replace(R.id.fl_container, AnalyzeIncomeFragment()).commit()
                }

                "예산" -> {
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            androidx.appcompat.R.anim.abc_fade_in,
                            androidx.appcompat.R.anim.abc_fade_out
                        )
                        .replace(R.id.fl_container, AnalyzePlanFragment()).commit()
                }

                "자산" -> {
                    supportFragmentManager.beginTransaction()
                        .setCustomAnimations(
                            androidx.appcompat.R.anim.abc_fade_in,
                            androidx.appcompat.R.anim.abc_fade_out
                        )
                        .replace(R.id.fl_container, AnalyzeAssetFragment()).commit()
                }
            }
        }

        viewModel.onClickSetBudget.observe(this) {
            if (it) {
                binding.flContainer2.isVisible = true
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        androidx.appcompat.R.anim.abc_fade_in,
                        androidx.appcompat.R.anim.abc_fade_out
                    )
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
                if (viewModel.subscribeExpired) {
                    viewModel.showSubscribePopupIfNeeded()
                } else {
                    val intent = Intent(this@AnalyzeActivity, HistoryActivity::class.java).apply {
                        putExtra("date", it)
                        putExtra("nickname", userNickname)
                    }
                    launcher.launch(intent)
                    applyHistoryOpenTransition()
                }
            }
        }
    }

    override fun onFragmentRemoved() {
        viewModel.onClickSetBudget(false)
    }

    private fun setSubscribePopup() {
        binding.includePopupSubscribe.ivExit.setOnClickListener {
            // 진입 시 표시된 팝업 닫기만 시간 체크
            if (viewModel.subscribePopupEnter.value == true)
                sharedPreferenceUtil.setString("subscribeCheckTenMinutes", getCurrentDateTimeString())

            binding.includePopupSubscribe.root.visibility = View.GONE
            binding.dimBackground.visibility = View.GONE
        }
    }
    
    private fun setAdMob() {
        showLoadingDialog()

        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            this,
            BuildConfig.GOOGLE_APP_INTERSTITIAL_KEY,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    dismissLoadingDialog()
                    mInterstitialAd = null
                    Timber.e("광고 로드 실패")
                    // 광고 로드 실패 시에도 화면 초기화
                    initializeScreen()
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    mInterstitialAd = ad
                    showAdMob()
                    Timber.e("광고가 로드됨")
                }
            })
    }

    fun showAdMob() {
        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                dismissLoadingDialog()

                sharedPreferenceUtil.setString("advertiseAnalyzeTenMinutes", getCurrentDateTimeString())
                mInterstitialAd = null
                Timber.e("광고 닫힘")

                // 광고가 닫힌 후에 화면 초기화
                initializeScreen()
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                dismissLoadingDialog()
                mInterstitialAd = null
                Timber.e("광고 표시 실패")

                // 광고 표시 실패 시에도 화면 초기화
                initializeScreen()
            }
        }
        mInterstitialAd?.show(this@AnalyzeActivity)
    }

    private fun shouldShowAd(): Boolean {
        val advertiseTime = sharedPreferenceUtil.getString("advertiseTime", "")
        val tenMinutes = sharedPreferenceUtil.getString("advertiseAnalyzeTenMinutes", "")

        val hasAdFreeBenefit =
            getAdvertiseCheck(advertiseTime) > 0 ||
                    getAdvertiseTenMinutesCheck(tenMinutes) > 0 ||
                    viewModel.subscribeUserActive

        Timber.d("subscribeUserActive ${viewModel.subscribeUserActive}")
        if (getAdvertiseCheck(advertiseTime) <= 0) {
            sharedPreferenceUtil.setString("advertiseTime", "")
        }
        if (getAdvertiseTenMinutesCheck(tenMinutes) <= 0) {
            sharedPreferenceUtil.setString("advertiseAnalyzeTenMinutes", "")
        }

        return !hasAdFreeBenefit
    }
}