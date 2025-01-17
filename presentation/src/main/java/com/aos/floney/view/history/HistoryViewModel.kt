package com.aos.floney.view.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aos.data.util.CurrencyUtil
import com.aos.data.util.SharedPreferenceUtil
import com.aos.data.util.checkDecimalPoint
import com.aos.data.util.getCurrencyCodeBySymbol
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.formatNumber
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.model.book.UiBookCategory
import com.aos.model.home.DayMoneyFavoriteItem
import com.aos.model.home.DayMoneyModifyItem
import com.aos.usecase.history.DeleteBookLineUseCase
import com.aos.usecase.history.DeleteBooksLinesAllUseCase
import com.aos.usecase.history.GetBookCategoryUseCase
import com.aos.usecase.history.GetBookFavoriteUseCase
import com.aos.usecase.history.PostBooksFavoritesUseCase
import com.aos.usecase.history.PostBooksLinesChangeUseCase
import com.aos.usecase.history.PostBooksLinesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.NumberFormat
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val prefs: SharedPreferenceUtil,
    private val getBookCategoryUseCase: GetBookCategoryUseCase,
    private val postBooksLinesUseCase: PostBooksLinesUseCase,
    private val postBooksLinesChangeUseCase: PostBooksLinesChangeUseCase,
    private val deleteBookLineUseCase: DeleteBookLineUseCase,
    private val deleteBooksLinesAllUseCase: DeleteBooksLinesAllUseCase,
    private val postBooksFavoritesUseCase : PostBooksFavoritesUseCase,
    private val getBookFavoriteUseCase: GetBookFavoriteUseCase
) : BaseViewModel() {

    val onCheckedChangeListener: (Boolean) -> Unit = { isChecked ->
        onDeleteCheckedChange(isChecked)
    }

    // 내역 추가 결과
    private var _postBooksLines = MutableEventFlow<Boolean>()
    val postBooksLines: EventFlow<Boolean> get() = _postBooksLines

    // 내역 삭제 결과
    private var _deleteBookLines = MutableEventFlow<Boolean>()
    val deleteBookLines: EventFlow<Boolean> get() = _deleteBookLines

    // 내역 수정 결과
    private var _postModifyBooksLines = MutableEventFlow<Boolean>()
    val postModifyBooksLines: EventFlow<Boolean> get() = _postModifyBooksLines

    // 날짜 클릭 여부
    private var _showCalendar = MutableEventFlow<Boolean>()
    val showCalendar: EventFlow<Boolean> get() = _showCalendar

    // 닫기 클릭
    private var _onClickCloseBtn = MutableEventFlow<Boolean>()
    val onClickCloseBtn: EventFlow<Boolean> get() = _onClickCloseBtn

    // 카테고리 클릭
    private var _onClickCategory = MutableEventFlow<String>()
    val onClickCategory: EventFlow<String> get() = _onClickCategory

    // 반복 설정 클릭
    private var _onClickRepeat = MutableEventFlow<String>()
    val onClickRepeat: EventFlow<String> get() = _onClickRepeat

    // 내역 삭제 버튼 클릭
    private var _onClickDelete = MutableEventFlow<OnClickedDelete>()
    val onClickDelete: EventFlow<OnClickedDelete> get() = _onClickDelete

    // 메모 클릭
    private var _onClickMemo = MutableEventFlow<Boolean>()
    val onClickMemo: EventFlow<Boolean> get() = _onClickMemo

    // 사진 클릭
    private var _onClickPicture = MutableEventFlow<Boolean>()
    val onClickPicture: EventFlow<Boolean> get() = _onClickPicture

    // 즐겨찾기 클릭
    private var _onClickFavorite = MutableEventFlow<Boolean>()
    val onClickFavorite: EventFlow<Boolean> get() = _onClickFavorite

    // 즐겨찾기 추가 결과
    private var _postBooksFavorites = MutableEventFlow<Boolean>()
    val postBooksFavorites: EventFlow<Boolean> get() = _postBooksFavorites


    // 날짜
    private var tempDate = ""
    var date = MutableLiveData<String>("날짜를 선택하세요")

    // 닉네임
    private val _nickname = MutableLiveData<String>()
    val nickname: LiveData<String> get() = _nickname

    // 내역 모드 추가 / 수정
    var mode = MutableLiveData<String>("add")

    // 금액
    var cost = MutableLiveData<String>("")

    // 지출, 수입, 이체
    var flow = MutableLiveData<String>("지출")

    // 자산
    var asset = MutableLiveData<String>("자산을 선택하세요")

    // 분류
    var line = MutableLiveData<String>("분류를 선택하세요")

    // 내용
    var content = MutableLiveData<String>("")

    // 내용
    var repeat = MutableLiveData<String>("")

    // 카테고리 종류
    var _categoryList = MutableLiveData<List<UiBookCategory>>()
    val categoryList: LiveData<List<UiBookCategory>> get() = _categoryList

    // 카테고리 선택 아이템 저장
    private var categoryClickItem: UiBookCategory? = null

    // 반복 설정
    val _repeatItem = MutableLiveData<List<UiBookCategory>>()
    val repeatItem: LiveData<List<UiBookCategory>> get() = _repeatItem
    private var _repeatClickItem = MutableLiveData<UiBookCategory?>()
    val repeatClickItem: LiveData<UiBookCategory?> get() = _repeatClickItem

    // 자산, 지출, 수입, 이체 카테고리 조회에 사용
    private var parent = ""

    // 예산/자산 제외 설정 여부
    val deleteChecked: MutableLiveData<Boolean> = MutableLiveData(false)


    // 내역 수정 시 해당 아이템 Id
    private var modifyId = 0
    private var modifyItem: DayMoneyModifyItem? = null

    // 구독 만료 내역
    var subscribeExpired = MutableLiveData<Boolean>(false)

    private var memo = ""
    private var urlList = listOf<String>()

    init {
        val array = arrayListOf<UiBookCategory>(
            UiBookCategory(0, true, "없음", false),
            UiBookCategory(1, false, "매일", false),
            UiBookCategory(2, false, "매주", false),
            UiBookCategory(3, false, "매달", false),
            UiBookCategory(4, false, "주중", false),
            UiBookCategory(5, false, "주말", false)
        )
        _repeatItem.postValue(array)
    }

    fun setMemo(memo: String) {
        this.memo = memo
    }

    fun setUrlList(urlList: List<String>) {
        this.urlList = urlList
    }

    // 내역 추가 시에는 날짜만 세팅함
    fun setIntentAddData(clickDate: String, nickname: String) {
        date.value = clickDate
        _nickname.value = nickname
    }

    fun setIntentModifyData(item: DayMoneyModifyItem) {
        mode.value = "modify"
        modifyId = item.id
        cost.value = item.money.substring(2, item.money.length).trim() + CurrencyUtil.currency
        date.value = item.lineDate
        flow.value = getCategory(item.lineCategory)
        asset.value = item.assetSubCategory
        line.value = item.lineSubCategory
        content.value = item.description
        _nickname.value = item.writerNickName
        deleteChecked.value = item.exceptStatus

        _repeatClickItem.value = UiBookCategory(
            idx = 1,
            checked = true,
            name = item.repeatDuration,
            default = true
        )

        modifyItem = item
        modifyItem!!.money =
            item.money.substring(2, item.money.length).trim() + CurrencyUtil.currency
        modifyItem!!.lineCategory = getCategory(item.lineCategory)
    }
    // 즐겨찾기 내역 불러오기
    fun setIntentFavoriteData(item: DayMoneyFavoriteItem) {
        mode.value = "add"
        cost.value = NumberFormat.getNumberInstance().format(if (checkDecimalPoint() && item.money.contains('.')) item.money.toDouble() else item.money.toInt()) + CurrencyUtil.currency
        line.value = item.lineSubcategoryName
        asset.value = item.assetSubcategoryName
        content.value = item.description
        flow.value = item.lineCategoryName
        deleteChecked.value = item.exceptStatus
    }
    // 즐겨찾기 추가 모드 설정
    fun setFavoriteMode(){
        mode.value = "favorite"
    }
    // 자산/분류 카테고리 항목 가져오기
    private fun getBookCategory() {
        viewModelScope.launch(Dispatchers.IO) {
            getBookCategoryUseCase(prefs.getString("bookKey", ""), parent).onSuccess { it ->
                // 카테고리 선택 값 초기화
                categoryClickItem = null

                val tempValue = if (parent == "자산") {
                    asset.value
                } else {
                    line.value
                }

                val item = it.mapIndexed { index, innerItem ->

                    if ((asset.value == "자산을 선택하세요" || line.value == "분류를 선택하세요") && index == 0) {
                        categoryClickItem = innerItem
                        UiBookCategory(
                            innerItem.idx, true, innerItem.name, innerItem.default
                        )
                    } else if (innerItem.name == tempValue) {
                        categoryClickItem = innerItem
                        UiBookCategory(
                            innerItem.idx, true, innerItem.name, innerItem.default
                        )
                    } else {
                        UiBookCategory(
                            innerItem.idx, innerItem.checked, innerItem.name, innerItem.default
                        )
                    }
                }

                _categoryList.postValue(item.toMutableList())
                _onClickCategory.emit(parent)
            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@HistoryViewModel)))
            }
        }
    }

    // 저장버튼 클릭
    fun onClickSaveBtn() {
        if (isAllInputData()) {
            when (mode.value) {
                "add" -> postAddHistory()
                "modify" -> postModifyHistory()
            }
        }
    }

    // 내역 추가
    private fun postAddHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            postBooksLinesUseCase(
                bookKey = prefs.getString("bookKey", ""),
                money = cost.value!!.replace(",", "").replace(CurrencyUtil.currency,"")
                    .toDouble(),
                flow = flow.value!!,
                asset = asset.value!!,
                line = line.value!!,
                lineDate = date.value!!.replace(".", "-"),
                description = if(content.value!! == ""){
                    line.value!!.toString()
                } else {
                    content.value!!
                },
                except = deleteChecked.value!!,
                nickname = nickname.value!!,
                repeatDuration = getConvertSendRepeatValue()
            ).onSuccess {
                _postBooksLines.emit(true)
            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@HistoryViewModel)))
            }
        }
    }

    // 내역 수정
    private fun postModifyHistory() {
        subscribeExpired.value = prefs.getBoolean("subscribe_expired", false)
        if(!subscribeExpired.value!!){
            viewModelScope.launch(Dispatchers.IO) {
                val tempMoney = cost.value!!.replace(",", "")
                postBooksLinesChangeUseCase(
                    lineId = modifyId,
                    bookKey = prefs.getString("bookKey", ""),
                    money = tempMoney.replace(CurrencyUtil.currency, "")
                        .toDouble(),
                    flow = flow.value!!,
                    asset = asset.value!!,
                    line = line.value!!,
                    lineDate = date.value!!.replace(".", "-"),
                    description = content.value!!,
                    except = deleteChecked.value!!,
                    nickname = nickname.value!!,
                ).onSuccess {
                    _postModifyBooksLines.emit(true)
                }.onFailure {
                    baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@HistoryViewModel)))
                }
            }
        }
    }

    // 내역 삭제
    fun deleteHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            deleteBookLineUseCase(
                bookLineKey = modifyId.toString()
            ).onSuccess {
                _deleteBookLines.emit(true)
            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@HistoryViewModel)))
            }
        }
    }

    // 반복 내역 삭제
    fun deleteRepeatHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            baseEvent(Event.ShowLoading)
            deleteBooksLinesAllUseCase(
                modifyId
            ).onSuccess {
                _deleteBookLines.emit(true)

                baseEvent(Event.HideLoading)
            }.onFailure {
                baseEvent(Event.HideLoading)
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@HistoryViewModel)))
            }
        }
    }

    // 모든 데이터 입력 되었는지 체크
    private fun isAllInputData(): Boolean {
        createErrorMsg()
        return cost.value != "" && asset.value != "자산을 선택하세요" && line.value != "분류를 선택하세요"
    }

    // 즐겨찾기 데이터 입력 되었는지 체크
    fun isFavoriteInputData(): Boolean {
        return cost.value != "" && asset.value != "자산을 선택하세요" && line.value != "분류를 선택하세요"
    }
    fun isFavoriteAllData(): Boolean {
        return cost.value != "" || asset.value != "자산을 선택하세요" || line.value != "분류를 선택하세요"
    }
    // 에러 메세지 생성
    private fun createErrorMsg() {
        if(cost.value == "") {
            baseEvent(Event.ShowToast("금액을 입력해주세요"))
        } else if (asset.value == "자산을 선택하세요") {
            baseEvent(Event.ShowToast("자산을 선택해주세요"))
        } else if (line.value == "분류를 선택하세요") {
            baseEvent(Event.ShowToast("분류를 선택해주세요"))
        }
    }

    private fun createFavoriteErrorMsg() {
        if(cost.value == "") {
            baseEvent(Event.ShowToast("금액을 입력해주세요"))
        } else if (asset.value == "자산을 선택하세요") {
            baseEvent(Event.ShowToast("자산을 선택해주세요"))
        } else if (line.value == "분류를 선택하세요") {
            baseEvent(Event.ShowToast("분류를 선택해주세요"))
        }
    }

    // 수정된 내용이 있는지 체크
    private fun isExistEdit(): Boolean {
        return date.value != modifyItem!!.lineDate || cost.value != modifyItem!!.money || asset.value != modifyItem!!.assetSubCategory || line.value != modifyItem!!.lineSubCategory || content.value != modifyItem!!.description
    }

    // 추가한 내용이 있는지 체크
    private fun isExistAdd(): Boolean {
        return date.value != "날짜를 선택하세요" || cost.value != "" || asset.value != "자산을 선택하세요" || line.value != "분류를 선택하세요" || content.value != ""
    }

    // 닫기 버튼 클릭
    fun onClickCloseBtn() {
        viewModelScope.launch {
            if (modifyItem != null) {
                _onClickCloseBtn.emit(isExistEdit())
            } else {
                _onClickCloseBtn.emit(isExistAdd())
            }
        }
    }

    // 즐겨찾기 추가 닫기 버튼 클릭
    fun onFavoriteAddClickCloseBtn() {
        viewModelScope.launch {
            _onClickCloseBtn.emit(isFavoriteAllData())
        }
    }

    // 날짜 표시 클릭
    fun onClickShowCalendar() {
        viewModelScope.launch {
            _showCalendar.emit(true)
        }
    }

    // 반복 설정 클릭
    fun onClickRepeat() {
        viewModelScope.launch {
            _onClickRepeat.emit("반복 설정")
        }
    }

    // 삭제 버튼 클릭
    fun onClickDeleteBtn() {
        viewModelScope.launch {
            _onClickDelete.emit(OnClickedDelete((!getConvertSendRepeatValue().equals("NONE")), modifyId))
        }
    }

    // 카테고리 자산 표시 클릭
    fun onClickCategory() {
        parent = "자산"
        getBookCategory()
    }

    // 카테고리 분류 표시 클릭
    fun onClickCategoryDiv() {
        parent = flow.value ?: "지출"
        getBookCategory()
    }

    // 지출, 수입, 이체 클릭
    fun onClickFlow(type: String) {
        if (mode.value == "add") {
            line.postValue("분류를 선택하세요")
            flow.postValue(type)
        }
    }

    // 메모 클릭
    fun onClickMemo() {
        viewModelScope.launch {
            _onClickMemo.emit(true)
        }
    }

    // 사진 클릭
    fun onClickPicture() {
        viewModelScope.launch {
            _onClickPicture.emit(true)
        }
    }

    // 비용 입력 시 저장
    fun onCostTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (count == 0) {
            cost.postValue("${s.toString().formatNumber()}")
        } else {
            cost.postValue("${s.toString().formatNumber()}${CurrencyUtil.currency}")
        }
    }

    // 예산/자산 제외 스위치 값 저장
    fun onDeleteCheckedChange(checked: Boolean) {
        deleteChecked.value = checked
    }

    // 캘린더 날짜 선택 시 값 저장
    fun setCalendarDate(_date: String) {
        tempDate = _date.replace("{", "")
        val dateArr = tempDate.replace("}", "").split("-")

        tempDate = "${dateArr[0]}.${
            if (dateArr[1].toInt() < 10) {
                "0${dateArr[1]}"
            } else {
                dateArr[1]
            }
        }.${
            if (dateArr[2].toInt() < 10) {
                "0${dateArr[2]}"
            } else {
                dateArr[2]
            }
        }"
    }

    // 반복내역 서버로 보내기 위한 값으로 변경
    private fun getConvertSendRepeatValue(): String {
        return if(_repeatClickItem.value == null) {
            "NONE"
        } else {
            when(_repeatClickItem.value!!.name) {
                "없음" -> "NONE"
                "매일" -> "EVERYDAY"
                "매주" -> "WEEK"
                "매달" -> "MONTH"
                "주중" -> "WEEKDAY"
                "주말" -> "WEEKEND"
                else -> ""
            }
        }
    }

    // 반복내역 서버로부터 받은 값을 UI 로 변경
    private fun getConvertReceiveRepeatValue(value: String): String {
        Timber.e("value $value")
        return when(value) {
             "NONE" -> "없음"
             "EVERYDAY" -> "매일"
             "WEEK" -> "매주"
             "MONTH" -> "매달"
             "WEEKDAY" -> "주중"
             "WEEKEND" -> "주말"
            else -> ""
        }
    }

    // 캘린더 선택 버튼 클릭
    fun onClickCalendarChoice() {
        if (tempDate != "") {
            date.postValue(tempDate)
        }
    }

    // 선택 버튼 클릭
    fun onClickCategoryChoiceDate() {
        if (parent == "자산") {
            // 자산 선택
            asset.value = categoryClickItem?.name
        } else {
            // 분류 선택
            line.value = categoryClickItem?.name
        }
    }

    // 반복 설정 확인 클릭
    fun onClickRepeatChoice() {
        repeat.value = repeatClickItem.value?.name
    }

    // 카테고리 아이템 세팅
    fun onClickCategoryItem(_item: UiBookCategory) {
        categoryClickItem = _item

        val item = _categoryList.value?.map {
            UiBookCategory(
                it.idx, false, it.name, it.default
            )
        } ?: listOf()
        item[_item.idx].checked = !item[_item.idx].checked
        _categoryList.postValue(item)
    }

    // 반복 설정 아이템 세팅
    fun onClickRepeatItem(_item: UiBookCategory) {
        val item = _repeatItem.value?.map {
            UiBookCategory(
                it.idx, false, it.name, it.default
            )
        } ?: listOf()
        item[_item.idx].checked = !item[_item.idx].checked
        _repeatItem.postValue(item)
        _repeatClickItem.value = _item
    }

    // 카테고리 선택 여부 확인
    fun isClickedCategoryItem(): Boolean {
        return if (categoryClickItem != null) {
            true
        } else {
            baseEvent(Event.ShowToast("카테고리 항목을 선택해주세요"))
            false
        }
    }

    // 반복 설정 선택 여부 확인
    fun isClickedRepeatItem(): Boolean {
        return if (repeatClickItem.value != null) {
            true
        } else {
            baseEvent(Event.ShowToast("반복 설정 항목을 선택해주세요"))
            false
        }
    }

    // 카테고리 가져오기 영문 -> 한글
    private fun getCategory(category: String): String {
        return when (category) {
            "INCOME" -> {
                "수입"
            }

            "OUTCOME" -> {
                "지출"
            }

            "TRANSFER" -> {
                "이체"
            }

            else -> ""
        }
    }

    // 즐겨찾기 버튼 클릭
    fun onClickFavorite(){
        viewModelScope.launch {
            _onClickFavorite.emit(true)
        }
    }

    // 즐겨찾기 추가
    fun postAddFavorite() {
        if (isFavoriteInputData()) {
            isFavoriteMaxData { isMax ->
                if (isMax) {
                    viewModelScope.launch(Dispatchers.IO) {
                        postBooksFavoritesUseCase(
                            bookKey = prefs.getString("bookKey", ""),
                            money = cost.value!!.replace(",", "").replace(CurrencyUtil.currency,"")
                                .toDouble(),
                            description = if (content.value=="") line.value!! else content.value!!,
                            lineCategoryName = flow.value!!,
                            lineSubcategoryName = line.value!!,
                            assetSubcategoryName = asset.value!!,
                            exceptStatus = deleteChecked.value!!
                        ).onSuccess {
                            _postBooksFavorites.emit(true)
                            baseEvent(Event.ShowSuccessToast("즐겨찾기에 추가되었습니다."))
                        }.onFailure {
                            baseEvent(Event.ShowToast("${flow.value!!} ${it.message.parseErrorMsg(this@HistoryViewModel)}"))
                        }
                    }
                } else {
                    baseEvent(Event.ShowToast("즐겨찾기 개수가 초과 되었습니다."))
                }
            }
        } else {
            createFavoriteErrorMsg()
        }
    }
    fun isFavoriteMaxData(onResult: (Boolean) -> Unit) {
        var sum = 0
        viewModelScope.launch {
            getBookFavoriteUseCase(prefs.getString("bookKey", ""),"INCOME").onSuccess { it ->
                sum+=it.size
                getBookFavoriteUseCase(prefs.getString("bookKey", ""), "OUTCOME").onSuccess { it ->
                    sum+=it.size
                    getBookFavoriteUseCase(prefs.getString("bookKey", ""), "TRANSFER").onSuccess { it ->
                        sum+=it.size

                        if (sum==15){
                            onResult(false)
                        }
                        else {
                            onResult(true)
                        }

                    }.onFailure {
                        baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@HistoryViewModel)))
                    }
                }.onFailure {
                    baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@HistoryViewModel)))
                }
            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@HistoryViewModel)))
            }
        }
    }

}