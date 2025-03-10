package com.aos.floney.view.analyze.subcategory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aos.data.util.SharedPreferenceUtil
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.model.analyze.UiAnalyzeLineSubCategoryModel
import com.aos.model.settlement.BookUsers
import com.aos.model.settlement.UiMemberSelectModel
import com.aos.usecase.analyze.PostAnalyzeLineSubCategoryUseCase
import com.aos.usecase.settlement.BooksUsersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import java.util.Calendar

@HiltViewModel
class AnalyzeLineSubcategoryViewModel @Inject constructor(
    private val prefs: SharedPreferenceUtil,
    private val postAnalyzeLineSubCategoryUseCase: PostAnalyzeLineSubCategoryUseCase,
    private val booksUsersUseCase : BooksUsersUseCase
): BaseViewModel() {


    // 상세 분석 데이터
    private var _analyzeSubCategoryData = MutableLiveData<UiAnalyzeLineSubCategoryModel>()
    val analyzeSubCategoryData: LiveData<UiAnalyzeLineSubCategoryModel> get() = _analyzeSubCategoryData

    // 카테고리
    var category = MutableLiveData<String>("")

    // 카테고리 상세
    var subCategory = MutableLiveData<String>("")

    // 사용자 리스트
    private var _booksUsersList = MutableLiveData<UiMemberSelectModel>()
    val booksUsersList: LiveData<UiMemberSelectModel> get() = _booksUsersList

    // 사용자 chip 텍스트 설정
    private var _userChip = MutableLiveData<String>()
    val userChip: LiveData<String> get() = _userChip

    // 사용자 email 리스트 파싱
    var email = MutableLiveData<List<String>>(emptyList())


    // 정렬 타입 숫자
    var flow = MutableLiveData<Int>(1)

    // 정렬 타입

    private var _sortType = MutableLiveData<String>()
    val sortType: LiveData<String> get() = _sortType

    // 검색한 달
    var month = MutableLiveData<String>("2025.01")

    // 사용자 필터 bottomSheet
    private var _userSelectBottomSheet = MutableEventFlow<Boolean>()
    val userSelectBottomSheet: EventFlow<Boolean> get() = _userSelectBottomSheet


    // 정렬 필터 bottomSheet
    private var _sortBottomSheet = MutableEventFlow<Boolean>()
    val sortBottomSheet: EventFlow<Boolean> get() = _sortBottomSheet


    // bottomSheet 닫기
    private var _closeSheet = MutableEventFlow<Boolean>()
    val closeSheet: EventFlow<Boolean> get() = _closeSheet


    // 가계부 인원 불러오기
    fun getUserList(){
        viewModelScope.launch(Dispatchers.IO) {
            booksUsersUseCase(prefs.getString("bookKey","")).onSuccess {
                // 불러오기 성공
                _booksUsersList.postValue(it)
            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@AnalyzeLineSubcategoryViewModel)))
            }
        }
    }


    // 멤버 클릭 시, 정산 멤버 count
    fun settingSettlementMember(bookUsers: BookUsers)
    {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedList = _booksUsersList.value?.booksUsers?.map { user ->
                if (user.email == bookUsers.email) {
                    user.copy(isCheck = !user.isCheck) // 선택된 멤버의 isCheck를 true로 설정
                } else {
                    user
                }
            }
            _booksUsersList.postValue(_booksUsersList.value?.copy(booksUsers = updatedList!!))

        }
    }

    fun getSelectedUserEmails() : List<String>{
        return _booksUsersList.value?.booksUsers
            ?.filter { it.isCheck } // isCheck가 true인 사용자만 필터링
            ?.map { it.email } // 각 사용자의 이메일만 추출하여 리스트로 변환
            ?: emptyList()
    }

    // 선택된 사용자 chip 텍스트 설정
    fun getSelectedUserChip() : String{
        val selectedUsers = _booksUsersList.value?.booksUsers?.filter { it.isCheck }.orEmpty()

        return when {
            selectedUsers.isEmpty() -> "사용자 전체"
            selectedUsers.size == _booksUsersList.value?.booksUsers?.size -> "사용자 전체"
            selectedUsers.size == 1 -> selectedUsers.first().nickname
            else -> {
                val sortedNames = selectedUsers.map { it.nickname }.sorted()
                "${sortedNames.first()} 외 ${selectedUsers.size - 1}명"
            }
        }
    }

    // 카테고리 설정
    fun setCategory(selectedCategory: String, selectedSubCategory: String){
        category.value = selectedCategory
        subCategory.value = selectedSubCategory
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


            Timber.i("flowState :${flow.value}")
            val sortType = flow.value?.let { fromIntToDisplayName(it) } ?: "최신 순"

            postAnalyzeLineSubCategoryUseCase(
                bookKey = prefs.getString("bookKey",""),
                category = category.value!!,
                subcategory = subCategory.value!!,
                emails = email.value!!,
                sortingType = SortType.fromDisplayName(sortType).toString(),
                yearMonth = currentYearMonth
            ).onSuccess {
                _sortType.postValue(sortType)
                _userChip.postValue(getSelectedUserChip())
                _analyzeSubCategoryData.postValue(it)
            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@AnalyzeLineSubcategoryViewModel)))
            }
        }
    }

    // 사용자 필터 선택
    fun onClickedTypeUser(){
        viewModelScope.launch {
            _userSelectBottomSheet.emit(true)
        }
    }

    // 정렬 필터 선택
    fun onClickedTypeSortChip(){
        viewModelScope.launch {
            _sortBottomSheet.emit(true)
        }
    }
    // 정렬 필터 변경
    fun onClickedTypeSort(type : Int){
        viewModelScope.launch {
            flow.postValue(type)
        }
    }

    // 사용자 필터 변경 (적용 버튼 클릭)
    fun onClickUserSaveButton(){
        // 사용자 이메일 값 변경 (서버에 보낼 사용자 리스트)
        email.value = getSelectedUserEmails()

        viewModelScope.launch {
            _closeSheet.emit(true)
        }
    }

    fun fromIntToDisplayName(value: Int): String? {
        return when (value) {
            1 -> SortType.LATEST.displayName
            2 -> SortType.OLDEST.displayName
            3 -> SortType.LINE_SUBCATEGORY_NAME.displayName
            4 -> SortType.USER_NICKNAME.displayName
            else -> null // ✅ 예외 처리 (잘못된 값이 들어왔을 때)
        }
    }
    // 정렬 필터 변경 (적용 버튼 클릭)
    fun onClickSortSaveButton(){
        viewModelScope.launch {
            _closeSheet.emit(true)
        }
    }
}

enum class SortType(val serverValue: String, val displayName: String) {
    LATEST("LATEST", "최신 순"),
    OLDEST("OLDEST", "오래된 순"),
    LINE_SUBCATEGORY_NAME("LINE_SUBCATEGORY_NAME", "분류 가나다 순"),
    USER_NICKNAME("USER_NICKNAME", "사용자 닉네임 가나다 순");

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
