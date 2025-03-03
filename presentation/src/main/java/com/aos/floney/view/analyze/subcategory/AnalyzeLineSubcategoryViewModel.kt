package com.aos.floney.view.analyze.subcategory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.aos.data.util.CurrencyUtil
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.formatNumber
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.model.analyze.UiAnalyzeLineSubCategoryModel
import com.aos.model.book.UiBookSettingModel
import com.aos.usecase.analyze.PostAnalyzeAssetUseCase
import com.aos.usecase.analyze.PostAnalyzeLineSubCategoryUseCase
import com.aos.usecase.booksetting.BooksInfoAssetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import java.util.Calendar

@HiltViewModel
class AnalyzeLineSubcategoryViewModel @Inject constructor(
    private val prefs: SharedPreferenceUtil,
    private val postAnalyzeLineSubCategoryUseCase: PostAnalyzeLineSubCategoryUseCase,
    private val booksInfoAssetUseCase: BooksInfoAssetUseCase,
    private val postAnalyzeAssetUseCase: PostAnalyzeAssetUseCase
): BaseViewModel() {


    // 상세 분석 데이터
    private var _analyzeSubCategoryData = MutableLiveData<UiAnalyzeLineSubCategoryModel>()
    val analyzeSubCategoryData: LiveData<UiAnalyzeLineSubCategoryModel> get() = _analyzeSubCategoryData

    // 카테고리
    var category = MutableLiveData<String>("")

    // 카테고리 상세
    var subCategory = MutableLiveData<String>("")

    // 사용자 리스트
    var emails = MutableLiveData<List<String>>(emptyList())

    // 정렬 타입
    var sortType = MutableLiveData<String>("최신 순")

    // 검색한 달
    var month = MutableLiveData<String>("2025.01")

    private val mutex = Mutex()

    init {
        settingLineSubcategory()
    }

    // 상세 지출/수입 정보 읽어오기
    fun settingLineSubcategory() {
        viewModelScope.launch {
            // Calendar 인스턴스 생성
            val calendar = Calendar.getInstance()
            // 현재 연도와 월을 가져옴
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH는 0부터 시작하므로 +1 해줌
            // "YYYY-MM" 형식으로 포맷
            val currentYearMonth = String.format("%d-%02d", year, month)

            postAnalyzeLineSubCategoryUseCase(
                bookKey = prefs.getString("bookKey",""),
                category = category.value!!,
                subcategory = subCategory.value!!,
                emails = emails.value!!,
                sortingType = SortType.fromDisplayName(sortType.value!!).toString(),
                yearMonth = currentYearMonth
            ).onSuccess {
                _analyzeSubCategoryData.postValue(it)
            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@AnalyzeLineSubcategoryViewModel)))
            }
        }
    }

    // 사용자 필터 변경
    fun onClickedTypeUser(){

    }

    // 정렬 필터 변경
    fun onClickedTypeSort(){

    }
}

enum class SortType(val serverValue: String, val displayName: String) {
    LATEST("LATEST", "최신순"),
    OLDEST("OLDEST", "오래된 순"),
    USER_NICKNAME("USER_NICKNAME", "사용자 닉네임 가나다 순"),
    LINE_SUBCATEGORY_NAME("LINE_SUBCATEGORY_NAME", "분류 가나다 순");

    companion object {
        // 서버에서 받은 값으로 Enum 찾기
        fun fromServerValue(value: String): SortType? {
            return values().find { it.serverValue == value }
        }

        // UI에서 표시하는 값으로 Enum 찾기
        fun fromDisplayName(value: String): SortType? {
            return values().find { it.displayName == value }
        }
    }
}
