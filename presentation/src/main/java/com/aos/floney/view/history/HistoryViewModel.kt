package com.aos.floney.view.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.aos.data.util.CurrencyUtil
import com.aos.data.util.SharedPreferenceUtil
import com.aos.data.util.SubscriptionDataStoreUtil
import com.aos.data.util.checkDecimalPoint
import com.aos.floney.base.BaseViewModel
import com.aos.floney.ext.formatMoneyWithCurrency
import com.aos.floney.ext.formatNumber
import com.aos.floney.ext.parseErrorCode
import com.aos.floney.ext.parseErrorMsg
import com.aos.floney.ext.toCategoryCode
import com.aos.floney.ext.toCategoryName
import com.aos.floney.util.EventFlow
import com.aos.floney.util.MutableEventFlow
import com.aos.floney.util.getAdvertiseTenMinutesCheck
import com.aos.model.book.UiBookCategory
import com.aos.model.home.DayMoneyFavoriteItem
import com.aos.model.home.DayMoneyModifyItem
import com.aos.model.home.ImageUrls
import com.aos.usecase.history.DeleteBookLineUseCase
import com.aos.usecase.history.DeleteBooksLinesAllUseCase
import com.aos.usecase.history.GetBookCategoryUseCase
import com.aos.usecase.history.PostBooksFavoritesUseCase
import com.aos.usecase.history.PostBooksLinesChangeUseCase
import com.aos.usecase.history.PostBooksLinesUseCase
import com.aos.usecase.subscribe.SubscribeDeleteCloudImageUseCase
import com.aos.usecase.subscribe.SubscribePresignedUrlUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    stateHandle: SavedStateHandle,
    private val prefs: SharedPreferenceUtil,
    private val subscriptionDataStoreUtil: SubscriptionDataStoreUtil,
    private val getBookCategoryUseCase: GetBookCategoryUseCase,
    private val postBooksLinesUseCase: PostBooksLinesUseCase,
    private val postBooksLinesChangeUseCase: PostBooksLinesChangeUseCase,
    private val deleteBookLineUseCase: DeleteBookLineUseCase,
    private val deleteBooksLinesAllUseCase: DeleteBooksLinesAllUseCase,
    private val postBooksFavoritesUseCase: PostBooksFavoritesUseCase,
    private val subscribeDeleteCloudImageUseCase: SubscribeDeleteCloudImageUseCase,
    private val subscribePresignedUrlUseCase: SubscribePresignedUrlUseCase,
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

    private var _getBookIsSubscribe = MutableLiveData<Boolean>(false)
    val getIsSubscribe: LiveData<Boolean> get() = _getBookIsSubscribe

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

    // 자산, 지출, 수입, 이체
    private val _flow: MutableLiveData<String> = stateHandle.getLiveData("flow", "지출")
    val flow: LiveData<String> get() = _flow


    // 자산
    var asset = MutableLiveData<String>("자산을 선택하세요")

    // 분류
    var line = MutableLiveData<String>("분류를 선택하세요")

    // 내용
    var content = MutableLiveData<String>("")

    // 반복
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

    // 클라우드 이미지 삭제 여부
    private var _onDeleteComplete = MutableEventFlow<Boolean>()
    val onDeleteComplete: EventFlow<Boolean> get() = _onDeleteComplete

    // 내역 수정 시 해당 아이템 Id
    private var modifyId = 0
    private var modifyItem: DayMoneyModifyItem? = null

    // 구독 만료 여부
    var subscribeExpired = MutableLiveData<Boolean>(false)

    // 메모/사진 존재 여부
    private val _memoOrImageExist = MutableLiveData<Boolean>()
    val memoOrImageExist: LiveData<Boolean> get() = _memoOrImageExist

    // 구독 유도 팝업
    private var _subscribePrompt = MutableEventFlow<Boolean>()
    val subscribePrompt: EventFlow<Boolean> get() = _subscribePrompt

    private var memo = ""
    private var cloudUrlList = mutableListOf<ImageUrls>()
    private var localUrlList = mutableListOf<File>()
    private var deletedCloudImageList = mutableListOf<ImageUrls>()

    init {
        // 구독 여부 조회
        getSubscribeBook()

        // 데이터 세팅
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

    fun getMemo(): String {
        return memo
    }

    fun setCloudUrlList(urlList: List<ImageUrls>) {
        this.cloudUrlList = urlList.toMutableList()
    }

    fun getCloudUrlList(): ArrayList<ImageUrls> {
        return ArrayList(cloudUrlList)
    }

    fun setLocalUrlList(urlList: List<File>) {
        this.localUrlList = urlList.toMutableList()
        Timber.i("this.localUrlList : ${this.localUrlList}")
    }

    fun getLocalUrlList(): ArrayList<File> {
        return ArrayList(localUrlList)
    }

    // 내역 추가 시에는 날짜만 세팅함
    fun setIntentAddData(clickDate: String, nickname: String) {
        date.value = clickDate
        _nickname.value = nickname
    }

    fun setIntentModifyData(item: DayMoneyModifyItem) {
        mode.value = "modify"
        modifyId = item.id
        cost.value = item.money.formatMoneyWithCurrency()
        date.value = item.lineDate
        _flow.value = item.lineCategory.toCategoryName()
        asset.value = item.assetSubCategory
        line.value = item.lineSubCategory
        content.value = item.description
        _nickname.value = item.writerNickName
        deleteChecked.value = item.exceptStatus
        memo = item.memo
        cloudUrlList = item.imageUrls.toMutableList()

        Timber.e("memo $memo")
        Timber.e("url $cloudUrlList")

        _repeatClickItem.value = UiBookCategory(
            idx = 1,
            checked = true,
            name = item.repeatDuration,
            default = true
        )

        modifyItem = item
        modifyItem!!.money =item.money.formatMoneyWithCurrency()
        modifyItem!!.lineCategory = item.lineCategory.toCategoryName()

        _memoOrImageExist.postValue(memo.isNotBlank() || cloudUrlList.isNotEmpty())
    }

    // 즐겨찾기 내역 불러오기
    fun setIntentFavoriteData(item: DayMoneyFavoriteItem) {
        mode.value = "add"
        cost.value = NumberFormat.getNumberInstance()
            .format(if (checkDecimalPoint() && item.money.contains('.')) item.money.toDouble() else item.money.toInt()) + CurrencyUtil.currency
        line.value = item.lineSubcategoryName
        asset.value = item.assetSubcategoryName
        content.value = item.description
        _flow.value = item.lineCategoryName
        deleteChecked.value = item.exceptStatus
    }

    // 즐겨찾기 추가 모드 설정
    fun setFavoriteMode() {
        mode.value = "favorite"
    }

    // 받아온 클라우드/로컬 데이터 셋팅
    fun processUpdatedPictureData(
        newCloudList: ArrayList<ImageUrls>?,
        newLocalList: ArrayList<File>?
    ) {
        newCloudList?.let { newCloud ->
            val oldCloudList = getCloudUrlList()
            val deleted = oldCloudList.filterNot { old ->
                newCloud.any { it.id == old.id }
            }

            // 삭제할 값이 있다면, 삭제할 리스트에 넣어둔다.
            deleted.takeIf { it.isNotEmpty() }?.let {
                deletedCloudImageList.addAll(it)
            }

            setCloudUrlList(newCloud.toMutableList())
        }

        newLocalList?.let { localList ->
            setLocalUrlList(localList.toMutableList())
        }
    }

    private fun getSubscribeBook() {
        viewModelScope.launch {
            Timber.d("checking history ${subscriptionDataStoreUtil.getBookSubscribe().first()}")

            _getBookIsSubscribe.postValue(subscriptionDataStoreUtil.getBookSubscribe().first())
        }
    }

    // 자산/분류 카테고리 항목 가져오기
    private fun getBookCategory() {
        viewModelScope.launch(Dispatchers.IO) {
            getBookCategoryUseCase(prefs.getString("bookKey", ""), parent).onSuccess { list ->

                categoryClickItem = null

                val tempValue = if (parent == "자산") asset.value else line.value
                val isUnselected = tempValue == "자산을 선택하세요" || tempValue == "분류를 선택하세요"

                val item = list.mapIndexed { index, innerItem ->
                    val shouldSelect = if (isUnselected) {
                        index == 0
                    } else {
                        innerItem.name == tempValue
                    }

                    if (shouldSelect) {
                        categoryClickItem = innerItem
                    }

                    UiBookCategory(
                        idx = innerItem.idx,
                        checked = shouldSelect,
                        name = innerItem.name,
                        default = innerItem.default
                    )
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
                "favorite" -> postAddFavorite()
            }
        }
    }

    // 내역 추가
    private fun postAddHistory() {
        // 선택된 로컬 이미지가 있다면 로컬 > 서버 이미지로 변경 후 내역 추가하도록 설정
        when(localUrlList.isNotEmpty()){
            true -> setLocalToCloud()
            false -> goToAddHistory()
        }
    }

    private fun goToAddHistory(){
        viewModelScope.launch(Dispatchers.IO) {
            postBooksLinesUseCase(
                bookKey = prefs.getString("bookKey", ""),
                money = cost.value!!.replace(",", "").replace(CurrencyUtil.currency, "")
                    .toDouble(),
                flow = flow.value!!,
                asset = asset.value!!,
                line = line.value!!,
                lineDate = date.value!!.replace(".", "-"),
                description = if (content.value!! == "") {
                    line.value!!.toString()
                } else {
                    content.value!!
                },
                except = deleteChecked.value!!,
                nickname = nickname.value!!,
                repeatDuration = getConvertSendRepeatValue(),
                memo = memo,
                imageUrl = cloudUrlList.map { it.url }
            ).onSuccess {
                baseEvent(Event.HideLoading)
                _postBooksLines.emit(true)
            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@HistoryViewModel)))
            }
        }
    }

    // 내역 수정
    private fun postModifyHistory() {
        handleImageBeforeModify()
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

    fun isFavoriteAllData(): Boolean {
        return cost.value != "" || asset.value != "자산을 선택하세요" || line.value != "분류를 선택하세요"
    }

    // 에러 메세지 생성
    private fun createErrorMsg() {
        if (cost.value == "") {
            baseEvent(Event.ShowToast("금액을 입력해주세요"))
        } else if (asset.value == "자산을 선택하세요") {
            baseEvent(Event.ShowToast("자산을 선택해주세요"))
        } else if (line.value == "분류를 선택하세요") {
            baseEvent(Event.ShowToast("분류를 선택해주세요"))
        }
    }

    private fun createFavoriteErrorMsg() {
        if (cost.value == "") {
            baseEvent(Event.ShowToast("금액을 입력해주세요"))
        } else if (asset.value == "자산을 선택하세요") {
            baseEvent(Event.ShowToast("자산을 선택해주세요"))
        } else if (line.value == "분류를 선택하세요") {
            baseEvent(Event.ShowToast("분류를 선택해주세요"))
        }
    }

    // 수정된 내용이 있는지 체크
    private fun isExistEdit(): Boolean {
        return date.value != modifyItem!!.lineDate || cost.value != modifyItem!!.money || asset.value != modifyItem!!.assetSubCategory || line.value != modifyItem!!.lineSubCategory || content.value != modifyItem!!.description || memo != modifyItem!!.memo || isImageUrlChange()
    }

    // 추가한 내용이 있는지 체크
    private fun isExistAdd(): Boolean {
        return cost.value != "" || asset.value != "자산을 선택하세요" || line.value != "분류를 선택하세요" || content.value != "" || localUrlList.isNotEmpty() || memo.isNotEmpty()
    }

    // 사진 이미지 변경된 내용 있는 지 체크한 후, 최종 수정
    private fun isImageUrlChange(): Boolean {
        val originalIds = modifyItem?.imageUrls?.map { it.id }?.toSet() ?: emptySet()
        val newIds = cloudUrlList.map { it.id }.toSet()
        return originalIds != newIds || localUrlList.isNotEmpty()
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
            _onClickDelete.emit(
                OnClickedDelete(
                    (!getConvertSendRepeatValue().equals("NONE")),
                    modifyId
                )
            )
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
        if (mode.value == "add" || mode.value == "favorite") {
            line.postValue("분류를 선택하세요")
            _flow.postValue(type)
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
        return if (_repeatClickItem.value == null) {
            "NONE"
        } else {
            when (_repeatClickItem.value!!.name) {
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
        return when (value) {
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

    // 즐겨찾기 버튼 클릭
    fun onClickFavorite() {
        viewModelScope.launch {
            _onClickFavorite.emit(true)
        }
    }

    // 즐겨찾기 추가
    fun postAddFavorite() {
        // 다 입력이 되었는 지 확인
        if (isAllInputData()) {
            applyAddFavorite()
        }
    }

    fun applyAddFavorite() {
        viewModelScope.launch(Dispatchers.IO) {
            postBooksFavoritesUseCase(
                bookKey = prefs.getString("bookKey", ""),
                money = cost.value!!.replace(",", "").replace(CurrencyUtil.currency, "")
                    .toDouble(),
                description = if (content.value == "") line.value!! else content.value!!,
                lineCategoryName = flow.value!!,
                lineSubcategoryName = line.value!!,
                assetSubcategoryName = asset.value!!,
                exceptStatus = deleteChecked.value!!
            ).onSuccess {
                _postBooksFavorites.emit(true)
                baseEvent(Event.ShowSuccessToast("즐겨찾기에 추가되었습니다."))
            }.onFailure {
                if (subscriptionDataStoreUtil.getSubscribeExpired().first()) {
                    subscribeExpired.value = true
                } else if (it.message.parseErrorCode() == "B014"){
                    _subscribePrompt.emit(true)
                } else {
                    baseEvent(Event.ShowToast("${flow.value!!} ${it.message.parseErrorMsg(this@HistoryViewModel)}"))
                }
            }
        }
    }

    private fun setCloudAddAndDelete() {
        viewModelScope.launch(Dispatchers.IO) {
            baseEvent(Event.ShowLoading)
            try {
                // 플래그 변수로 모든 작업의 성공 여부 추적
                var allOperationsSuccessful = true

                // 삭제할 클라우드 이미지가 있는 경우
                if (deletedCloudImageList.isNotEmpty()) {
                    for (imageUrl in deletedCloudImageList) {
                        val result = runCatching { subscribeDeleteCloudImageUseCase(imageUrl.id) }
                        result.onSuccess {
                            Timber.d("삭제 성공: ${imageUrl.id}")
                        }.onFailure {
                            Timber.e("삭제 실패: ${imageUrl.id}")
                            baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@HistoryViewModel)))
                            allOperationsSuccessful = false
                        }
                    }
                }

                Timber.i("this.localUrlList isNotEmpty : $localUrlList}")
                // 추가할 로컬 이미지가 있는 경우
                if (localUrlList.isNotEmpty()) {
                    Timber.d("this.localUrlList isNotEmpty?? : $localUrlList}")
                    for (file in localUrlList) {
                        // presigned URL 가져오기
                        val urlResult = subscribePresignedUrlUseCase(prefs.getString("bookKey", ""))
                        urlResult.onSuccess { presignedData ->
                            val url = presignedData.url
                            // 파일 업로드 시도
                            val uploadSuccess =
                                uploadFileToPresignedUrl(url, file, presignedData.viewUrl)
                            if (!uploadSuccess) {
                                allOperationsSuccessful = false
                            }
                        }.onFailure {
                            baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@HistoryViewModel)))
                            allOperationsSuccessful = false
                        }
                    }
                }

                // 모든 작업이 성공적으로 완료된 경우에만 수정 진행
                if (allOperationsSuccessful) {
                    goModifyHistory()
                } else {
                    baseEvent(Event.ShowToast("이미지 처리 중 오류가 발생했습니다."))
                }
            } finally {
                baseEvent(Event.HideLoading)
            }
        }
    }

    private fun setLocalToCloud() {
        viewModelScope.launch(Dispatchers.IO) {
            baseEvent(Event.ShowLoading)
            try {
                // 플래그 변수로 모든 작업의 성공 여부 추적
                var allOperationsSuccessful = true

                Timber.i("this.localUrlList isNotEmpty : $localUrlList}")
                // 추가할 로컬 이미지가 있는 경우
                if (localUrlList.isNotEmpty()) {
                    Timber.d("this.localUrlList isNotEmpty?? : $localUrlList}")
                    for (file in localUrlList) {
                        // presigned URL 가져오기
                        val urlResult = subscribePresignedUrlUseCase(prefs.getString("bookKey", ""))
                        urlResult.onSuccess { presignedData ->
                            val url = presignedData.url
                            // 파일 업로드 시도
                            val uploadSuccess =
                                uploadFileToPresignedUrl(url, file, presignedData.viewUrl)
                            if (!uploadSuccess) {
                                allOperationsSuccessful = false
                            }
                        }.onFailure {
                            baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@HistoryViewModel)))
                            allOperationsSuccessful = false
                        }
                    }
                }

                // 모든 작업이 성공적으로 완료된 경우에만 추가 진행
                if (allOperationsSuccessful) {
                    goToAddHistory()
                } else {
                    baseEvent(Event.ShowToast("이미지 처리 중 오류가 발생했습니다."))
                }
            } finally {
                baseEvent(Event.HideLoading)
            }
        }
    }

    // 업로드 함수 수정 - 성공 여부를 반환하도록 변경
    private suspend fun uploadFileToPresignedUrl(
        presignedUrl: String,
        file: File,
        viewUrl: String
    ): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                val url = URL(presignedUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "PUT"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "image/jpeg")

                file.inputStream().use { input ->
                    connection.outputStream.use { output ->
                        input.copyTo(output)
                    }
                }

                if (connection.responseCode == 200) {
                    println("File uploaded successfully!")
                    cloudUrlList.add(ImageUrls(-1, viewUrl))
                    true // 성공
                } else {
                    println("Upload failed with response code: ${connection.responseCode}")
                    baseEvent(Event.ShowToast(connection.responseMessage.parseErrorMsg(this@HistoryViewModel)))
                    false // 실패
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false // 예외 발생 시 실패
        }
    }

    fun handleImageBeforeModify() {
        // 클라우드 삭제, 로컬 추가 이미지 없을 경우 바로 수정
        if (deletedCloudImageList.isEmpty() && localUrlList.isEmpty())
            goModifyHistory()

        // 클라우드 삭제 여부, 로컬 추가 이미지 있을 경우 따로 처리 후 수정한다.
        setCloudAddAndDelete()
    }

    fun goModifyHistory() {
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
                memo = memo,
                imageUrls = cloudUrlList.map { it.url }
            ).onSuccess {
                _postModifyBooksLines.emit(true)
            }.onFailure {
                baseEvent(Event.ShowToast(it.message.parseErrorMsg(this@HistoryViewModel)))
            }
        }
    }
}