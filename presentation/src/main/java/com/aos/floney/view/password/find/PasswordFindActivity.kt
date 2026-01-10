package com.aos.floney.view.password.find

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.aos.floney.R
import com.aos.floney.base.BaseActivity
import com.aos.floney.databinding.ActivityPasswordFindBinding
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.view.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PasswordFindActivity : BaseActivity<ActivityPasswordFindBinding, PasswordFindViewModel>(R.layout.activity_password_find) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupViewModelObserver()
    }

    private fun setupViewModelObserver() {
        repeatOnStarted {
            viewModel.previousPage.collect {
                moveLoginActivity()
            }
        }
        repeatOnStarted {
            viewModel.showSendDialog.collect {
                SendPasswordBottomSheetFragment {
                    moveLoginActivity()
                }.show(supportFragmentManager, "")
            }
        }
    }

    private fun moveLoginActivity() {
        startActivity(Intent(this@PasswordFindActivity, LoginActivity::class.java))
        if (Build.VERSION.SDK_INT >= 34) {
            overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, android.R.anim.fade_in, android.R.anim.fade_out)
        } else {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        finishAffinity()
    }
}