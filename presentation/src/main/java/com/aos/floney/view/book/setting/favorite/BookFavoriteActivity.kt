package com.aos.floney.view.book.setting.favorite

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.aos.floney.R
import com.aos.floney.base.BaseActivity
import com.aos.floney.databinding.ActivityBookFavoriteBinding
import com.aos.floney.ext.applyOpenTransition
import com.aos.floney.view.book.setting.BookSettingActivity
import com.aos.floney.view.home.HomeActivity
import com.aos.floney.view.subscribe.SubscribePlanActivity
import com.aos.model.book.UiBookFavoriteModel
import com.aos.model.home.DayMoneyFavoriteItem
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class BookFavoriteActivity : BaseActivity<ActivityBookFavoriteBinding, BookFavoriteViewModel>(R.layout.activity_book_favorite) {
    private lateinit var navController: NavController
    private var entryPoint = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupJetpackNavigation()
        setupEntryPoint()
        setUpAnimation()
    }
    fun setUpAnimation(){
        if (Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, R.anim.anim_slide_in_from_right_fade_in, android.R.anim.fade_out)
        } else {
            overridePendingTransition(R.anim.anim_slide_in_from_right_fade_in, android.R.anim.fade_out)
        }
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
    private fun setupJetpackNavigation() {

        val host = supportFragmentManager.findFragmentById(R.id.nav_host_book_category_favorite_container) as NavHostFragment
        navController = host.navController

    }
    private fun setupEntryPoint(){
        entryPoint = intent.getStringExtra("entryPoint").toString()
        viewModel.setEntryPoint(entryPoint)
    }
    // 홈 화면으로 이동
    fun startHomeActivity() {
        startActivity(Intent(this, HomeActivity::class.java))
        finishAffinity()
    }

    // 내역 추가 화면으로 이동
    fun startHistoryAddActivity(item: UiBookFavoriteModel) {
        // 정규식을 사용하여 숫자 부분만 추출
        val moneyNumbersOnly = item.money.replace(Regex("[^0-9.]"), "")

        val intent = Intent()
        intent
            .putExtra(
                "favoriteItem", DayMoneyFavoriteItem(
                    money = moneyNumbersOnly,
                    description = item.description,
                    lineCategoryName = item.lineCategoryName,
                    lineSubcategoryName = item.lineSubcategoryName,
                    assetSubcategoryName = item.assetSubcategoryName,
                    exceptStatus = item.exceptStatus
                ))

        setResult(RESULT_OK, intent)
        finish()
    }

    // 가계부 설정 화면으로 이동
    fun startBookSettingActivity() {
        if(entryPoint != "") {
            finish()
        } else {
            startActivity(Intent(this, BookSettingActivity::class.java).putExtra("entryPoint", "history"))
            finishAffinity()
        }
    }

    // 구독 플랜 화면으로 이동
    fun goToSubscribePlanActivity(){
        startActivity(Intent(this, SubscribePlanActivity::class.java))
        applyOpenTransition()
    }
}