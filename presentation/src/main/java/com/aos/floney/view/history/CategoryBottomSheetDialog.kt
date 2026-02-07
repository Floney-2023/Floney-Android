package com.aos.floney.view.history

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import com.aos.floney.BR
import com.aos.floney.R
import com.aos.floney.base.BaseViewModel
import com.aos.floney.databinding.BottomSheetCategoryBinding
import com.aos.floney.util.BookCategory
import com.aos.model.book.UiBookCategory
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import okhttp3.internal.notifyAll
import timber.log.Timber


class CategoryBottomSheetDialog(
    context: Context,
    private val category: String,
    private val viewModel: HistoryViewModel,
    private val activityLifecycleOwner: LifecycleOwner,
    private val clickedChoiceBtn: () -> Unit,
    private val clickedEditeBtn: () -> Unit,
) : BottomSheetDialog(context), UiBookCategory.OnItemClickListener {
    lateinit var binding: BottomSheetCategoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BottomSheetCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpUi()
        setUpGridLayoutManger()
        setBottomSheetHeight()
    }

    private fun setUpUi() {
        with(binding) {
            binding.dialog = this@CategoryBottomSheetDialog
            setVariable(BR.vm, viewModel)
            setVariable(BR.eventHolder, this@CategoryBottomSheetDialog)
            lifecycleOwner = activityLifecycleOwner

            binding.tvCategory.text =
                BookCategory.fromRaw(category)?.let { context.getString(it.labelRes) }
                    ?: context.getString(R.string.bottom_sheet_categories)
        }
    }

    private fun setUpGridLayoutManger() {

        val flexboxLayoutManager = FlexboxLayoutManager(context)
        flexboxLayoutManager.flexWrap = FlexWrap.WRAP
        flexboxLayoutManager.flexDirection = FlexDirection.ROW
        flexboxLayoutManager.justifyContent = JustifyContent.FLEX_START
        binding.rvCategory.layoutManager = flexboxLayoutManager
        binding.rvCategory.itemAnimator = null
    }

    private fun setBottomSheetHeight() {
        val bottomSheet = findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            it.layoutParams.height = (context.resources.displayMetrics.heightPixels * 0.65).toInt()
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    fun onClickChoiceBtn() {
        if (viewModel.isClickedCategoryItem()) {
            // 선택 버튼 클릭 리스너
            clickedChoiceBtn()
            this.dismiss()
        }
    }

    fun onClickEditBtn() {
        clickedEditeBtn()
        this.dismiss()
    }

    override fun onItemClick(item: UiBookCategory) {
        viewModel.onClickCategoryItem(item)
    }
}
