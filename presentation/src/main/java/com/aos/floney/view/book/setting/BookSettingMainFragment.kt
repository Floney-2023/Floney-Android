package com.aos.floney.view.book.setting

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.databinding.library.baseAdapters.BR
import androidx.navigation.fragment.findNavController
import com.aos.floney.R
import com.aos.floney.base.BaseFragment
import com.aos.floney.databinding.FragmentBookSettingMainBinding
import com.aos.floney.ext.repeatOnStarted
import com.aos.floney.view.book.add.BookAddInviteShareBottomSheetFragment
import com.aos.floney.view.book.setting.asset.BookSettingAssetBottomSheetFragment
import com.aos.floney.view.book.setting.carryinfo.BookSettingCarryInfoSheetFragment
import com.aos.floney.view.book.setting.excel.BookSettingExcelBottomSheetFragment
import com.aos.floney.view.common.BaseAlertDialog
import com.aos.model.book.MyBookUsers
import com.aos.model.book.UiBookSettingModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookSettingMainFragment :
    BaseFragment<FragmentBookSettingMainBinding, BookSettingMainViewModel>(R.layout.fragment_book_setting_main), UiBookSettingModel.OnItemClickListener {
    override fun onItemClick(item: MyBookUsers) {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpUi()
        setUpViewModelObserver()
    }
    override fun onResume() {
        super.onResume()
        viewModel.searchBookSettingItems()
    }

    private fun setUpUi() {
        binding.setVariable(BR.eventHolder, this@BookSettingMainFragment)
        binding.setVariable(BR.vm, viewModel)
    }
    private fun setUpViewModelObserver() {
        repeatOnStarted {
            viewModel.back.collect() {
                if(it) {
                    val activity = requireActivity() as BookSettingActivity
                    activity.startHomeActivity()
                }
            }
        }
        repeatOnStarted {
            viewModel.settingPage.collect() {
                if(it) {
                    val settingAction = BookSettingMainFragmentDirections.actionBookSettingMainFragmentToBookSettingEditFragment(
                        viewModel.bookSettingInfo.value!!.bookImg,
                        viewModel.bookSettingInfo.value!!.seeProfileStatus,
                        viewModel.bookSettingInfo.value!!.ourBookUsers[0].roleOrigin == "방장",
                        viewModel.bookSettingInfo.value!!.ourBookUsers.size,
                        viewModel.bookSettingInfo.value!!.bookName
                    )
                    findNavController().navigate(settingAction)
                }
            }
        }
        repeatOnStarted {
            viewModel.initPage.collect() {
                if(it) {
                    if(it) {
                        val initDialog = BaseAlertDialog(
                            getString(R.string.book_setting_currency_alert_dialog_title),
                            getString(R.string.book_setting_currency_alert_dialog_info),
                            true
                        ) {  checked ->
                            if (checked)
                                viewModel.initBook()
                        }
                        initDialog.show(parentFragmentManager, "initDialog")
                    }
                }
            }
        }
        repeatOnStarted {
            viewModel.currencyPage.collect(){
                if(it){
                    val emailArray: Array<String> = viewModel.bookSettingInfo.value?.ourBookUsers?.map { it.email }?.toTypedArray() ?: emptyArray()
                    viewModel.bookSettingInfo.value!!.ourBookUsers

                    val currencyAction = BookSettingMainFragmentDirections.actionBookSettingMainFragmentToBookSettingCurrencyFragment(
                        viewModel.bookSettingInfo.value!!.bookName,
                        emailArray
                    )
                    findNavController().navigate(currencyAction)
                }
            }
        }
        repeatOnStarted {
            viewModel.assetPage.collect() {
                if(it) {
                    val inviteCode = ""
                    val bottomSheetFragment = BookSettingAssetBottomSheetFragment()
                    bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
                }
            }
        }
        repeatOnStarted {
            viewModel.carryInfoPage.collect() {
                if(it) {
                    val bottomSheetFragment = BookSettingCarryInfoSheetFragment(viewModel.bookSettingInfo.value!!.carryOver) { carryInfo ->
                        viewModel.changeCarryOver(carryInfo)
                    }
                    bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
                }
            }
        }
        repeatOnStarted {
            viewModel.categoryPage.collect() {
                if(it) {
                    val activity = requireActivity() as BookSettingActivity
                    activity.startBookCategoryActivity()
                }
            }
        }
        repeatOnStarted {
            viewModel.invitePage.collect() {
                if(it) {
                    val bottomSheetFragment = BookAddInviteShareBottomSheetFragment()
                    bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
                }
            }
        }
        repeatOnStarted {
            viewModel.excelPage.collect() {
                if(it) {
                    val excelFragment = BookSettingExcelBottomSheetFragment()
                    excelFragment.show(parentFragmentManager, excelFragment.tag)
                }
            }
        }
        repeatOnStarted {
            viewModel.budgetPage.collect() {
                if(it) {
                    val repeatAction = BookSettingMainFragmentDirections.actionBookSettingMainFragmentToBookSettingBudgetFragment()
                    findNavController().navigate(repeatAction)
                }
            }
        }
        repeatOnStarted {
            viewModel.repeatPage.collect() {
                if(it) {
                    val repeatAction = BookSettingMainFragmentDirections.actionBookSettingMainFragmentToBookSettingRepeatFragment()
                    findNavController().navigate(repeatAction)
                }
            }
        }
        repeatOnStarted {
            viewModel.repeatPage.collect() {
                if(it) {
                    val repeatAction = BookSettingMainFragmentDirections.actionBookSettingMainFragmentToBookSettingRepeatFragment()
                    findNavController().navigate(repeatAction)
                }
            }
        }
        repeatOnStarted {
            viewModel.exitPopup.collect(){
                if(it){
                    val initDialog = BaseAlertDialog(
                        getString(R.string.book_setting_exit_alert_dialog_title),
                        getString(R.string.book_setting_exit_alert_dialog_info),
                        true
                    ) {  checked ->
                        if (checked)
                            viewModel.onBookExit()
                    }
                    initDialog.show(parentFragmentManager, "exitDialog")
                }
            }
        }
        repeatOnStarted {
            viewModel.exitPage.collect(){
                if(it) {
                    // 가계부 2개 이상인데 나간 경우 -> 홈 화면으로 이동
                    val activity = requireActivity() as BookSettingActivity
                    activity.startHomeActivity()
                }
                else {
                    // 가계부 1개 있는데 나간 경우 -> 가계부 추가 화면으로 이동
                    val activity = requireActivity() as BookSettingActivity
                    activity.startBookAddActivity()
                }
            }
        }
        repeatOnStarted {
            viewModel.favoritePage.collect(){
                if(it) {
                    val activity = requireActivity() as BookSettingActivity
                    activity.startBookFavoriteActivity()
                }
            }
        }
    }
}