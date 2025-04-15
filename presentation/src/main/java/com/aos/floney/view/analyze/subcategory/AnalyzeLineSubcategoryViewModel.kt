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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    // 선택된 월
    var month = MutableLiveData<String>("")

    // 사용자 리스트
    private var _booksUsersList = MutableLiveData<UiMemberSelectModel>()
    val booksUsersList: LiveData<UiMemberSelectModel> get() = _booksUsersList

    // 사용자 필터 리스트
    private var _booksUsersFilterCount = MutableLiveData<Int>()
    val booksUsersFilterCount: LiveData<Int> get() = _booksUsersFilterCount

    // 사용자 chip 텍스트 설정
    private var _userChip = MutableLiveData<String>()
    val userChip: LiveData<String> get() = _userChip

    // 사용자 email 리스트 파싱
    var email = MutableLiveData<List<String>>(emptyList())


    // 정렬 타입 숫자
    // 기존 MutableLiveData → MutableStateFlow 사용 (데이터 유지됨)
    private var _flow = MutableStateFlow(1) // ✅ 정렬 타입 (StateFlow 사용)
    val flow: StateFlow<Int> get() = _flow

    // 정렬 타입

    private var _sortType = MutableLiveData<String>()
    val sortType: LiveData<String> get() = _sortType

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
                // 처음 불러온 값은 모두 check된 것으로 설정하도록 한다.
                val updatedList = it.booksUsers.map { user ->
                    user.copy(isCheck = true)
                }
                _booksUsersList.postValue(it.copy(booksUsers = updatedList))
                _booksUsersFilterCount.postValue(it.booksUsers.size)
            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@AnalyzeLineSubcategoryViewModel)))
            }
        }
    }


    // 멤버 클릭 시, 정산 멤버 count
    fun settingSettlementMember(bookUsers: BookUsers) {
        val updatedList = booksUsersList.value?.booksUsers?.map { user ->
            if (user.email == bookUsers.email) {
                user.copy(isCheck = !user.isCheck)
            } else {
                user
            }
        } ?: return

        _booksUsersList.value = _booksUsersList.value?.copy(booksUsers = updatedList)

        val emails = getSelectedUserEmails()
        _booksUsersFilterCount.value = emails.size
    }

    fun getSelectedUserEmails() : List<String>{
        return booksUsersList.value?.booksUsers
            ?.filter { it.isCheck } // isCheck가 true인 사용자만 필터링
            ?.map { it.email } // 각 사용자의 이메일만 추출하여 리스트로 변환
            ?: emptyList()
    }

    // 선택된 사용자 chip 텍스트 설정
    fun getSelectedUserChip() : String{
        val selectedUsers = booksUsersList.value?.booksUsers?.filter { it.isCheck }.orEmpty()

        return when {
            selectedUsers.isEmpty() -> "사용자 전체"
            selectedUsers.size == 1 -> selectedUsers.first().nickname
            selectedUsers.size == booksUsersList.value?.booksUsers?.size -> "사용자 전체"
            else -> {
                val sortedNames = selectedUsers.map { it.nickname }.sorted()
                "${sortedNames.first()} 외 ${selectedUsers.size - 1}명"
            }
        }
    }

    // 카테고리 설정
    fun setCategory(selectedCategory: String, selectedSubCategory: String, selectedDate: String){
        category.value = selectedCategory
        subCategory.value = selectedSubCategory

        // ✅ "yyyy-MM-dd" → "yyyy-MM" 형식으로 변환
        val formattedDate = selectedDate.substring(0, 7) // 앞의 7자리 ("yyyy-MM")만 추출
        month.value = formattedDate

        settingLineSubcategory()
    }

    // 상세 지출/수입 정보 읽어오기
    fun settingLineSubcategory() {
        viewModelScope.launch {

            Timber.i("flowState :${flow.value}")
            Timber.i("bookUser :${booksUsersList.value?.booksUsers}")
            val sortType = flow.value?.let { fromIntToDisplayName(it) } ?: "최신 순"

            postAnalyzeLineSubCategoryUseCase(
                bookKey = prefs.getString("bookKey",""),
                category = category.value!!,
                subcategory = subCategory.value!!,
                emails = email.value!!,
                sortingType = SortType.fromDisplayName(sortType).toString(),
                yearMonth = month.value!!
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
            _flow.emit(type)
        }
    }

    // 사용자 필터 전체 선택/ 전체 취소 (적용 버튼 클릭)
    fun onClickUserAll(flow: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            // 클릭 이벤트 발생
            // flow가 true면 전체 선택된 상태(전체 삭제 표시), 아니면 하나라도 선택되지 않은 상태(전체 선택 표시)

            val updatedList = booksUsersList.value?.booksUsers?.map { user ->
                user.copy(isCheck = !flow) // ✅ 모든 사용자의 isCheck 값을 동일하게 변경
            }

            _booksUsersList.postValue(_booksUsersList.value?.copy(booksUsers = updatedList!!))

            val memberCount = if(flow) 0 else booksUsersList.value?.booksUsers?.size
            _booksUsersFilterCount.postValue(memberCount ?: 0)
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
