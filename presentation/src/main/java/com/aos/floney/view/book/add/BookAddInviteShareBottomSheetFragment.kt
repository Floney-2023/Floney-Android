package com.aos.floney.view.book.add

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.aos.floney.BuildConfig
import com.aos.floney.R
import com.aos.floney.base.BaseBottomSheetFragment
import com.aos.floney.base.BaseFragment
import com.aos.floney.databinding.ActivityBookAddBinding
import com.aos.floney.databinding.BottomSheetBookAddInviteShareBinding
import com.aos.floney.databinding.FragmentBookAddInviteCheckBinding
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.view.common.BaseAlertDialog
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class BookAddInviteShareBottomSheetFragment :
    BaseBottomSheetFragment<BottomSheetBookAddInviteShareBinding,BookAddInviteShareViewModel>
        (R.layout.bottom_sheet_book_add_invite_share) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViewModelObserver()

    }
    private fun setUpViewModelObserver() {
        repeatOnStarted {
            // 코드 공유하기 페이지
            viewModel.inviteCodeShare.collect {
                Timber.e("nextPage $it")
                if(it) {
                    onSharedBtnClicked()
                }
            }
        }
        repeatOnStarted {
            // 나중에 하기 -> Bottomsheet 닫기
            viewModel.inviteCodeExit.collect {
                Timber.e("nextPage $it")
                if(it) {
                    dismiss()
                }
            }
        }
        repeatOnStarted {
            // 초대 코드 클립보드 복사
            viewModel.inviteCodeCopy.collect {
                Timber.e("nextPage $it")
                if(it) {
                    val code = viewModel.inviteCode.value ?: ""
                    val clipboard: ClipboardManager =
                        requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("label", code)
                    clipboard.setPrimaryClip(clip)
                    setDialog()
                }
            }
        }
    }
    private fun setDialog()
    {
        BaseAlertDialog(
            title = getString(R.string.invite_code_copy_title),
            info = getString(R.string.invite_code_copy_message),
            false
        ) {
            if(it) {

            }
        }.show(parentFragmentManager, "baseAlertDialog")

    }
    private fun onSharedBtnClicked() {

        val code = "https://floney.onelink.me${BuildConfig.appsflyer_invite_url}?inviteCode=${viewModel.inviteCode.value ?: ""}"
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/html"
        sharingIntent.putExtra(Intent.EXTRA_TEXT, code)
        startActivity(Intent.createChooser(sharingIntent, "Share using text"))
    }
}