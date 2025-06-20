package com.aos.floney.view.mypage.main.service

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.databinding.library.baseAdapters.BR
import androidx.navigation.fragment.findNavController
import com.aos.floney.BuildConfig
import com.aos.floney.R
import com.aos.floney.base.BaseFragment
import com.aos.floney.databinding.FragmentMyPageMainBinding
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.ext.setViewTouchEffect
import com.aos.floney.view.common.BaseAlertDialog
import com.aos.floney.view.common.ErrorToastDialog
import com.aos.floney.view.common.WarningPopupDialog
import com.aos.floney.view.mypage.MyPageActivity
import com.aos.model.user.MyBooks
import com.aos.model.user.UiMypageSearchModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MyPageMainFragment : BaseFragment<FragmentMyPageMainBinding, MyPageMainViewModel>(R.layout.fragment_my_page_main), UiMypageSearchModel.OnItemClickListener {

    private var mRewardAd: RewardedAd? = null

    override fun onItemClick(item: MyBooks) {
        viewModel.settingBookKey(item.bookKey)
    }

    override fun onResume() {
        super.onResume()

        viewModel.settingAdvertiseTime()
        viewModel.searchMypageItems()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpUi()
        setUpViewModelObserver()
    }
    private fun setUpUi() {
        binding.liUserInformView.setViewTouchEffect()
        binding.setVariable(BR.eventHolder, this@MyPageMainFragment)
    }

    private fun setUpViewModelObserver() {
        repeatOnStarted {
            viewModel.alarmPage.collect {
                if(it) {
                    val activity = requireActivity() as MyPageActivity
                    activity.startAlarmActivity()
                }
            }
        }
        repeatOnStarted {
            viewModel.informPage.collect {
                if(it) {
                    val activity = requireActivity() as MyPageActivity
                    activity.startInformActivity()
                }
            }
        }
        repeatOnStarted {
            viewModel.reviewPage.collect {
                if(it) {
                    val manager = ReviewManagerFactory.create(requireContext())
                    val request = manager.requestReviewFlow()
                    request.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val reviewInfo = task.result
                            val flow = manager.launchReviewFlow(requireActivity(), reviewInfo)
                            flow.addOnCompleteListener {
                                // 리뷰 플로우가 종료된 후의 처리
                            }
                        } else {
                            // 에러 발생 시 처리
                            ErrorToastDialog(requireContext(), "리뷰 작성하기 페이지 이동 실패하였습니다.").show()
                        }
                    }
                }
            }
        }
        repeatOnStarted {
            viewModel.settingPage.collect {
                if(it) {

                    val activity = requireActivity() as MyPageActivity
                    activity.startSettingActivity()
                }
            }
        }
        repeatOnStarted {
            viewModel.bookAddBottomSheet.collect { shouldShowBottomSheet ->
                if (shouldShowBottomSheet) {

                    val activity = requireActivity() as MyPageActivity
                    activity.startBottomSheet()
                }
            }
        }
        repeatOnStarted {
            viewModel.mailPage.collect {
                if (it){
                    val askAction = MyPageMainFragmentDirections.actionMyPageMainFragmentToMyPageServiceAskFragment()
                    findNavController().navigate(askAction)
                }
            }
        }
        repeatOnStarted {
            viewModel.noticePage.collect {
                if (it){
                    val noticeAction = MyPageMainFragmentDirections.actionMyPageMainFragmentToMyPageServiceNoticeFragment()
                    findNavController().navigate(noticeAction)
                }
            }
        }
        repeatOnStarted {
            viewModel.privatePage.collect {
                if (it){
                    val privateAction = MyPageMainFragmentDirections.actionMyPageMainFragmentToMyPageServicePrivacyFragment()
                    findNavController().navigate(privateAction)
                }
            }
        }
        repeatOnStarted {
            viewModel.usageRightPage.collect {
                if (it){
                    val rightAction = MyPageMainFragmentDirections.actionMyPageMainFragmentToMyPageServiceTermsFragment()
                    findNavController().navigate(rightAction)
                }
            }
        }
        repeatOnStarted {
            viewModel.adMobPage.collect {
                if (it){
                    setUpAdMob()
                }
            }
        }
        repeatOnStarted {
            viewModel.unsubscribePage.collect{
                if (it){
                    // 구독 해지 여부 확인 팝업
                    BaseAlertDialog(title = getString(R.string.unsubscribe_notice_pop_title), info = getString(R.string.unsubscribe_notice_pop_info), true) {
                        if(it) {

                            val packageName = requireActivity().packageName // 현재 앱의 패키지 이름
                            val uri = Uri.parse("https://play.google.com/store/account/subscriptions?package=$packageName")

                            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                                setPackage("com.android.vending") // Play Store 앱으로만 열리도록 설정
                            }

                            // Play Store 앱이 설치되어 있는지 확인
                            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                                requireActivity().startActivity(intent)
                            } else {
                                // Play Store 앱이 없는 경우 팝업
                                val errorToastDialog = ErrorToastDialog(requireContext(), "플레이 스토어가 설치 되어 있지 않습니다.")
                                errorToastDialog.show()

                                Handler(Looper.myLooper()!!).postDelayed({
                                    errorToastDialog.dismiss()
                                }, 2000)
                            }

                        }
                    }.show(parentFragmentManager, "baseAlertDialog")
                }
            }
        }
        repeatOnStarted {
            viewModel.supposePage.collect {
                if (it){
                    val url = "https://m.cafe.naver.com/ca-fe/web/cafes/31054271/menus/5"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

                    // Chrome 브라우저 패키지명 설정
                    intent.setPackage("com.android.chrome")
                    if (intent.resolveActivity(requireActivity().packageManager) != null) {
                        startActivity(intent)
                    }
                }
            }
        }
        repeatOnStarted {
            viewModel.loadCheck.collect {
                if(it) {
                    loadProfileImage()
                }
            }
        }
        repeatOnStarted {
            viewModel.subscribePage.collect {
                if(it) { // 구독 중인 경우, 구독 정보 보기로 이동
                    val activity = requireActivity() as MyPageActivity
                    activity.startSubscribeInformActivity()
                } else{ // 구독 중이 아닐 경우, 구독하기 화면으로 이동
                    val activity = requireActivity() as MyPageActivity
                    activity.startSubscribePlanActivity()
                }
            }
        }
        repeatOnStarted {
            viewModel.unsubscribePopup.collect {
                if(it) {
                    // 구독 해지 완료 팝업
                    val exitDialogFragment = WarningPopupDialog(
                        getString(R.string.unsubscribe_popup_title),
                        getString(R.string.unsubscribe_popup_info),
                        "",getString(R.string.already_pick_button),
                        true
                    ){}
                    exitDialogFragment.show(parentFragmentManager, "initDialog")
                }
            }
        }
    }
    private fun settingAdvertiseTime(){
        viewModel.updateAdvertiseTime()
    }
    private fun setUpAdMob(){
        showLoadingDialog()

        MobileAds.initialize(requireContext())
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(requireContext(),
            BuildConfig.GOOGLE_APP_REWARD_KEY, adRequest, object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    dismissLoadingDialog()
                    mRewardAd = null
                    Timber.e("광고가 아직 로드되지 않음 4")
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    dismissLoadingDialog()
                    mRewardAd = ad
                    showAdMob()
                    Timber.e("광고가 아직 로드되지 않음 5")
                }
            })
    }
    fun showAdMob(){
        if (mRewardAd != null) {
            mRewardAd?.show(requireActivity(), OnUserEarnedRewardListener {
                fun onUserEarnedReward(rewardItem: RewardItem) {
                    val rewardAmount = rewardItem.amount
                    var rewardType = rewardItem.type

                }
            })
            mRewardAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    settingAdvertiseTime()
                    mRewardAd = null

                }
                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    mRewardAd = null
                    Timber.e("광고가 아직 로드되지 않음 2")
                }
            }

        }else{
            showLoadingDialog()
            Timber.e("광고가 아직 로드되지 않음 3")
            setUpAdMob()
        }
    }
    fun loadProfileImage(){
        if(viewModel.getUserProfile().equals("user_default")) {
            Glide.with(requireContext())
                .load(R.drawable.icon_default_profile)
                .fitCenter()
                .centerCrop()
                .into(binding.ivProfile)
        } else {
            Glide.with(requireContext())
                .load(viewModel.getUserProfile())
                .fitCenter()
                .centerCrop()
                .format(DecodeFormat.PREFER_RGB_565)
                .into(binding.ivProfile)
        }
    }
}