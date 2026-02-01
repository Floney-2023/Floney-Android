package com.aos.repository

import com.aos.model.book.GetBooksCodeModel
import com.aos.model.book.GetBooksInfoCurrencyModel
import com.aos.model.book.PostBookFavoriteModel
import com.aos.model.book.PostBooksCategoryAddModel
import com.aos.model.book.PostBooksChangeModel
import com.aos.model.book.PostBooksCreateModel
import com.aos.model.book.PostBooksInfoCurrencyModel
import com.aos.model.book.PostBooksJoinModel
import com.aos.model.book.PostBooksLinesModel
import com.aos.model.book.UiBookBudgetModel
import com.aos.model.book.UiBookCategory
import com.aos.model.book.UiBookEntranceModel
import com.aos.model.book.UiBookFavoriteModel
import com.aos.model.book.UiBookRepeatModel
import com.aos.model.book.UiBookSettingModel
import com.aos.model.home.GetCheckUserBookModel
import com.aos.model.home.UiBookDayModel
import com.aos.model.home.UiBookInfoModel
import com.aos.model.home.UiBookMonthModel
import com.aos.model.settlement.GetSettlementLastModel
import com.aos.model.settlement.NaverShortenUrlModel
import com.aos.model.settlement.UiMemberSelectModel
import com.aos.model.settlement.UiOutcomesSelectModel
import com.aos.model.settlement.UiSettlementAddModel
import com.aos.model.settlement.UiSettlementSeeModel
import com.aos.model.settlement.settleOutcomes
import okhttp3.ResponseBody

interface BookRepository {

    // 유저 가계부 유효 확인
    suspend fun getCheckUserBook(): Result<GetCheckUserBookModel>

    // 캘린더 조회
    suspend fun getBooksMonth(bookKey: String, date: String): Result<UiBookMonthModel>

    // 일별 내역 조회
    suspend fun getBooksDays(bookKey: String, date: String): Result<UiBookDayModel>

    // 가계부 정보 조회
    suspend fun getBookInfo(bookKey: String): Result<UiBookInfoModel>

    // 가계부 참여
    suspend fun postBooksJoin(code: String): Result<PostBooksJoinModel>

    // 가계부 생성
    suspend fun postBooksCreate(name: String, profileImg: String): Result<PostBooksCreateModel>

    // 가계부 생성
    suspend fun getBookCategory(bookKey: String, parent: String): Result<List<UiBookCategory>>

    // 가계부 내역 추가
    suspend fun postBooksLines(
        bookKey: String,
        money: Double,
        lineType: String,
        asset: String,
        line: String,
        lineDate: String,
        description: String,
        except: Boolean,
        nickname: String,
        repeatDuration: String,
        memo: String,
        imageUrl: List<String>
    ): Result<PostBooksLinesModel>

    // 가계부 내역 수정
    suspend fun postBooksLinesChange(
        lineId: Int,
        bookKey: String,
        money: Double,
        lineType: String,
        asset: String,
        line: String,
        lineDate: String,
        description: String,
        except: Boolean,
        nickname: String,
        memo : String,
        imageUrls : List<String>
    ): Result<PostBooksChangeModel>

    // 내역 삭제
    suspend fun deleteBookLines(bookLineKey: String): Result<Void?>

    // 가계부의 마지막 정산일 조회
    suspend fun getSettlementLast(bookKey: String): Result<GetSettlementLastModel>

    // 특정 가계부의 유저들 조회
    suspend fun getBooksUsers(bookKey: String): Result<UiMemberSelectModel>

    // 정산 지출 내역 조회
    suspend fun postBooksOutcomes(usersEmails : List<String>, startDate : String, endDate : String, bookKey: String) : Result<UiOutcomesSelectModel>

    // 정산 추가
    suspend fun postSettlementAdd(bookKey: String, startDate : String, endDate : String, usersEmails : List<String>, outcomes: List<settleOutcomes>) : Result<UiSettlementAddModel>

    // 가계부의 정산 내역 조회
    suspend fun getSettlementSee(bookKey: String): Result<UiSettlementSeeModel>

    // 정산 세부 내역 조회
    suspend fun getSettlementDetailSee(id: Long): Result<UiSettlementAddModel>

    // 가계부 설정 조회하기
    suspend fun getBooksInfo(bookKey: String): Result<UiBookSettingModel>

    // 가계부 이름 변경
    suspend fun postBooksName(name: String, bookKey: String): Result<Void?>

    // 가계부 삭제
    suspend fun deleteBooks(bookKey: String): Result<Void?>

    // 내역 프로필 보기 설정
    suspend fun postBooksInfoSeeProfile(bookKey: String, seeProfileStatus: Boolean): Result<Void?>

    // 가계부 초기화
    suspend fun deleteBooksInfoAll(bookKey: String): Result<Void?>

    // 화폐설정 변경
    suspend fun postBooksInfoCurrency(currency: String, bookKey: String): Result<PostBooksInfoCurrencyModel>

    // 화폐설정 조회
    suspend fun getBooksInfoCurrency(bookKey: String): Result<GetBooksInfoCurrencyModel>

    // 가계부 자산 설정하기
    suspend fun postBooksInfoAsset(bookKey: String, asset: Long): Result<Void?>

    // 가계부 이월설정 On/Off
    suspend fun postBooksInfoCarryOver(status: Boolean, bookKey: String): Result<Void?>

    // 예산조회하기
    suspend fun getBooksBudget(bookKey: String, date: String): Result<UiBookBudgetModel>

    // 가계부 예산 설정하기
    suspend fun postBooksInfoBudget(bookKey: String, budget: Long, date: String): Result<Void?>

    // 하위 카테고리 삭제
    suspend fun deleteBookCategory(bookKey: String,  parent: String, name: String): Result<Void?>

    // 카테고리 추가하기
    suspend fun postBooksCategoryAdd(bookKey: String, parent: String, name: String): Result<PostBooksCategoryAddModel>

    // 가계부 코드 조회
    suspend fun getBooksCode(bookKey: String): Result<GetBooksCodeModel>

    // 반복 내역 조회
    suspend fun getBooksRepeat(bookKey: String, categoryType: String): Result<List<UiBookRepeatModel>>

    // 반복 내역 삭제
    suspend fun deleteBooksRepeat(repeatLineId: Int): Result<Void?>

    // 가계부 이후 내역 삭제
    suspend fun deleteBooksLinesAll(bookLineKey: Int): Result<Void?>

    // 가계부 엑셀 다운로드
    suspend fun postBooksExcel(bookKey: String, excelDuration: String, currentDate: String): Result<ResponseBody>

    // 가계부 나가기
    suspend fun postBooksOut(bookKey: String): Result<Void?>

    // 참여코드로 가계부 정보 불러오기
    suspend fun getBooks(code: String): Result<UiBookEntranceModel>

    // 가계부 단축 URl
    suspend fun postShortenUrl(id: String, secretKey: String, url: String): Result<NaverShortenUrlModel>

    // 즐겨찾기 분류 항목 별 조회
    suspend fun getBookFavorite(bookKey: String, categoryType: String): Result<List<UiBookFavoriteModel>>

    // 즐겨찾기 추가
    suspend fun postBooksFavorites(bookKey: String, money:Double, description: String, lineCategoryName : String, lineSubcategoryName : String, assetSubcategoryName : String, exceptStatus : Boolean): Result<PostBookFavoriteModel>

    // 즐겨찾기 삭제
    suspend fun deleteBookFavorite(bookKey: String,  favoriteId: Int): Result<Void?>

    // 가계부 프로필 변경
    suspend fun postChangeBookImg(bookKey: String, url: String): Result<Void?>
}
