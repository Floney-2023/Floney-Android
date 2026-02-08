package com.aos.data.repository.remote.book

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import com.aos.data.entity.request.book.DeleteBookCategoryBody
import com.aos.data.entity.request.book.PostBooksChangeBody
import com.aos.data.entity.request.settlement.Duration
import com.aos.data.entity.request.book.PostBooksCategoryAddBody
import com.aos.data.entity.request.book.PostBooksCreateBody
import com.aos.data.entity.request.book.PostBooksExcelBody
import com.aos.data.entity.request.book.PostBooksFavoritesBody
import com.aos.data.entity.request.book.PostBooksInfoAssetBody
import com.aos.data.entity.request.book.PostBooksInfoBudgetBody
import com.aos.data.entity.request.book.PostBooksInfoCarryOverBody
import com.aos.data.entity.request.book.PostBooksInfoCurrencyBody
import com.aos.data.entity.request.book.PostBooksInfoSeeProfileBody
import com.aos.data.entity.request.book.PostBooksJoinBody
import com.aos.data.entity.request.book.PostBooksLinesBody
import com.aos.data.entity.request.book.PostBooksNameBody
import com.aos.data.entity.request.book.PostBooksOutBody
import com.aos.data.entity.request.book.PostChangeBookImgBody
import com.aos.data.entity.request.settlement.Outcomes
import com.aos.data.entity.request.settlement.PostBooksOutcomesBody
import com.aos.data.entity.request.settlement.PostSettlementAddBody
import com.aos.data.mapper.toGetBooksCodeModel
import com.aos.data.mapper.toGetBooksInfoCurrencyModel
import com.aos.data.mapper.toGetCheckUserBookModel
import com.aos.data.mapper.toGetsettleUpLastModel
import com.aos.data.mapper.toPostBookFavoriteModel
import com.aos.data.mapper.toPostBooksCategoryAddModel
import com.aos.data.mapper.toUiBookInfoModel
import com.aos.data.mapper.toUiBookMonthModel
import com.aos.data.mapper.toPostBooksCreateModel
import com.aos.data.mapper.toPostBooksInfoCurrencyModel
import com.aos.data.mapper.toPostBooksJoinModel
import com.aos.data.mapper.toPostBooksLinesChangeModel
import com.aos.data.mapper.toPostBooksLinesModel
import com.aos.data.mapper.toUiBookCategory
import com.aos.data.mapper.toPostSettlementAddModel
import com.aos.data.mapper.toUiBookBudgetModel
import com.aos.data.mapper.toUiBookEntranceModel
import com.aos.data.mapper.toUiBookFavorite
import com.aos.data.mapper.toUiBookRepeatModel
import com.aos.data.mapper.toUiBookSettingModel
import com.aos.data.mapper.toUiMemberSelectModel
import com.aos.data.mapper.toUiOutcomesSelectModel
import com.aos.data.mapper.toUiSettlementSeeModel
import com.aos.data.util.RetrofitFailureStateException
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
import com.aos.model.settlement.UiMemberSelectModel
import com.aos.model.settlement.UiOutcomesSelectModel
import com.aos.model.settlement.UiSettlementAddModel
import com.aos.model.settlement.UiSettlementSeeModel
import com.aos.model.settlement.settleOutcomes
import com.aos.repository.BookRepository
import com.aos.util.NetworkState
import okhttp3.ResponseBody
import timber.log.Timber
import javax.inject.Inject

class BookRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bookDataSource: BookRemoteDataSource
) : BookRepository {

    override suspend fun getCheckUserBook(): Result<GetCheckUserBookModel> {
        when (val data = bookDataSource.getCheckUserBook()) {
            is NetworkState.Success -> return Result.success(data.body.toGetCheckUserBookModel())
            is NetworkState.Failure -> return Result.failure(
                RetrofitFailureStateException(data.error, data.code)
            )

            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError -> return Result.failure(IllegalStateException("unKnownError"))
        }
    }

    override suspend fun getBooksMonth(bookKey: String, date: String): Result<UiBookMonthModel> {
        when (val data = bookDataSource.getBooksMonth(bookKey, date)) {
            is NetworkState.Success -> return Result.success(data.body.toUiBookMonthModel())
            is NetworkState.Failure -> return Result.failure(
                RetrofitFailureStateException(data.error, data.code)
            )

            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError -> {
                Timber.e("UnknownError ${data.errorState}, ${data.t}")
                return Result.failure(IllegalStateException("unKnownError"))
            }
        }
    }

    override suspend fun getBooksDays(bookKey: String, date: String): Result<UiBookDayModel> {
        when (val data = bookDataSource.getBooksDays(bookKey, date)) {
            is NetworkState.Success -> return Result.success(data.body.toUiBookMonthModel(context, date))
            is NetworkState.Failure -> return Result.failure(
                RetrofitFailureStateException(data.error, data.code)
            )

            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError -> {
                Timber.e("UnknownError ${data.errorState}, ${data.t}")
                return Result.failure(IllegalStateException("unKnownError"))
            }
        }
    }

    override suspend fun getBookInfo(bookKey: String): Result<UiBookInfoModel> {
        when (val data = bookDataSource.getBookInfo(bookKey)) {
            is NetworkState.Success -> return Result.success(data.body.toUiBookInfoModel())
            is NetworkState.Failure -> return Result.failure(
                RetrofitFailureStateException(data.error, data.code)
            )

            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError -> {
                Timber.e("UnknownError ${data.errorState}, ${data.t}")
                return Result.failure(IllegalStateException("unKnownError"))
            }
        }
    }

    override suspend fun postBooksJoin(code: String): Result<PostBooksJoinModel> {
        when (val data = bookDataSource.postBooksJoin(PostBooksJoinBody(code))) {
            is NetworkState.Success -> return Result.success(data.body.toPostBooksJoinModel())
            is NetworkState.Failure -> return Result.failure(
                RetrofitFailureStateException(data.error, data.code)
            )

            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError -> return Result.failure(IllegalStateException("unKnownError"))
        }
    }

    override suspend fun postBooksCreate(
        name: String, profileImg: String
    ): Result<PostBooksCreateModel> {
        when (val data = bookDataSource.postBooksCreate(PostBooksCreateBody(name, profileImg))) {
            is NetworkState.Success -> return Result.success(data.body.toPostBooksCreateModel())
            is NetworkState.Failure -> return Result.failure(
                RetrofitFailureStateException(data.error, data.code)
            )

            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError -> return Result.failure(IllegalStateException("unKnownError"))
        }
    }

    override suspend fun getBookCategory(
        bookKey: String,
        parent: String,
    ): Result<List<UiBookCategory>> {
        when (val data = bookDataSource.getBookCategory(bookKey, parent)) {
            is NetworkState.Success -> return Result.success(data.body.toUiBookCategory(parent))
            is NetworkState.Failure -> return Result.failure(
                RetrofitFailureStateException(data.error, data.code)
            )

            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError -> return Result.failure(IllegalStateException("unKnownError"))
        }
    }

    override suspend fun postBooksLines(
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
        imageUrl: List<String>,
    ): Result<PostBooksLinesModel> {
        when (val data = bookDataSource.postBooksLines(
            PostBooksLinesBody(
                bookKey,
                money,
                lineType,
                asset,
                line,
                lineDate,
                description,
                except,
                nickname,
                repeatDuration,
                memo,
                imageUrl
            )
        )) {
            is NetworkState.Success -> return Result.success(data.body.toPostBooksLinesModel())
            is NetworkState.Failure -> return Result.failure(
                RetrofitFailureStateException(data.error, data.code)
            )

            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError -> return Result.failure(IllegalStateException("unKnownError"))
        }
    }

    override suspend fun postBooksLinesChange(
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
        imageUrls: List<String>
    ): Result<PostBooksChangeModel> {
        when (val data = bookDataSource.postBooksLinesChange(
            PostBooksChangeBody(
                lineId, bookKey, money, lineType, asset, line, lineDate, description, except, nickname, memo, imageUrls
            )
        )) {
            is NetworkState.Success -> return Result.success(data.body.toPostBooksLinesChangeModel())
            is NetworkState.Failure -> return Result.failure(
                RetrofitFailureStateException(data.error, data.code)
            )

            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError -> {
                Timber.e(data.t?.message)
                return Result.failure(IllegalStateException("unKnownError"))
            }
        }
    }

    override suspend fun deleteBookLines(bookLineKey: String): Result<Void?> {
        when (val data = bookDataSource.deleteBookLines(
            bookLineKey
        )) {
            is NetworkState.Success -> return Result.success(data.body)
            is NetworkState.Failure -> return Result.failure(
                RetrofitFailureStateException(data.error, data.code)
            )

            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError -> {
                return if(data.errorState == "body값이 null로 넘어옴") {
                    Result.success(null)
                } else {
                    Result.failure(IllegalStateException("unKnownError"))
                }
            }
        }
    }

    override suspend fun getSettlementLast(bookKey: String): Result<GetSettlementLastModel> {
        when (val data =
            bookDataSource.getSettlementLast(bookKey)) {
            is NetworkState.Success -> return Result.success(data.body.toGetsettleUpLastModel())
            is NetworkState.Failure -> return Result.failure(
                RetrofitFailureStateException(data.error, data.code)
            )
            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError -> return Result.failure(IllegalStateException("unKnownError"))
        }
    }
    override suspend fun getBooksUsers(bookKey: String): Result<UiMemberSelectModel> {
        when (val data =
            bookDataSource.getBooksUsers(bookKey)) {
            is NetworkState.Success -> return Result.success(data.body.toUiMemberSelectModel())
            is NetworkState.Failure -> return Result.failure(
                RetrofitFailureStateException(data.error, data.code)
            )
            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError ->{
                Timber.e("UnknownError ${data.errorState}, ${data.t}")
                return Result.failure(IllegalStateException("unKnownError"))
            }
        }
    }
    override suspend fun postBooksOutcomes(userEmails: List<String>, startDate: String, endDate: String, bookKey: String): Result<UiOutcomesSelectModel> {
        when (val data =
            bookDataSource.postBooksOutcomes(PostBooksOutcomesBody(userEmails, Duration(startDate, endDate), bookKey))) {
            is NetworkState.Success -> return Result.success(data.body.toUiOutcomesSelectModel())
            is NetworkState.Failure -> return Result.failure(
                RetrofitFailureStateException(data.error, data.code)
            )
            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError ->{
                Timber.e("UnknownError ${data.errorState}, ${data.t}")
                return Result.failure(IllegalStateException("unKnownError"))
            }
        }
    }

    override suspend fun postSettlementAdd(
        bookKey: String,
        startDate: String,
        endDate: String,
        usersEmails: List<String>,
        outcomes: List<settleOutcomes>
    ):  Result<UiSettlementAddModel> {
        val outcomes = outcomes.map {
            Outcomes(it.outcome.toDouble(),it.userEmail)
        }
        when (val data =
            bookDataSource.postSettlementAdd(PostSettlementAddBody(bookKey, startDate, endDate, usersEmails, outcomes))) {
            is NetworkState.Success -> return Result.success(data.body.toPostSettlementAddModel())
            is NetworkState.Failure -> return Result.failure(
                RetrofitFailureStateException(data.error, data.code)
            )
            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError ->{
                Timber.e("UnknownError ${data.errorState}, ${data.t}")
                return Result.failure(IllegalStateException("unKnownError"))
            }
        }
    }
    override suspend fun getSettlementSee(bookKey: String): Result<UiSettlementSeeModel> {
        when (val data =
            bookDataSource.getSettlementSee(bookKey)) {
            is NetworkState.Success -> return Result.success(data.body.toUiSettlementSeeModel(context))
            is NetworkState.Failure -> return Result.failure(
                RetrofitFailureStateException(data.error, data.code)
            )
            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError ->{
                Timber.e("UnknownError ${data.errorState}, ${data.t}")
                return Result.failure(IllegalStateException("unKnownError"))
            }
        }
    }
    override suspend fun getSettlementDetailSee(
        id: Long
    ):  Result<UiSettlementAddModel> {
        when (val data =
            bookDataSource.getSettlementDetailSee(id)) {
            is NetworkState.Success -> return Result.success(data.body.toPostSettlementAddModel())
            is NetworkState.Failure -> return Result.failure(
                RetrofitFailureStateException(data.error, data.code)
            )
            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError ->{
                Timber.e("UnknownError ${data.errorState}, ${data.t}")
                return Result.failure(IllegalStateException("unKnownError"))
            }
        }
    }
    override suspend fun getBooksInfo(bookKey: String): Result<UiBookSettingModel> {
        when (val data =
            bookDataSource.getBooksInfo(bookKey)) {
            is NetworkState.Success -> return Result.success(data.body.toUiBookSettingModel(context))
            is NetworkState.Failure -> return Result.failure(
                RetrofitFailureStateException(data.error, data.code)
            )
            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError ->{

                Timber.e("UnknownError ${data.errorState}, ${data.t}")
                return Result.failure(IllegalStateException("unKnownError"))
            }
        }
    }
    override suspend fun postBooksName(name: String, bookKey: String): Result<Void?> {
        when (val data =
            bookDataSource.postBooksName(PostBooksNameBody(name, bookKey))) {
            is NetworkState.Success -> {
                return Result.success(data.body)
            }
            is NetworkState.Failure -> {
                return Result.failure(
                    RetrofitFailureStateException(data.error, data.code)
                )
            }
            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError -> {
                return if(data.errorState == "body값이 null로 넘어옴") {
                    Result.success(null)
                } else {
                    Result.failure(IllegalStateException("unKnownError"))
                }
            }
        }
    }
    override suspend fun deleteBooks(bookKey: String): Result<Void?> {
        when (val data =
            bookDataSource.deleteBooks(bookKey)) {
            is NetworkState.Success -> {
                return Result.success(data.body)
            }
            is NetworkState.Failure -> {
                return Result.failure(
                    RetrofitFailureStateException(data.error, data.code)
                )
            }
            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError -> {
                return if(data.errorState == "body값이 null로 넘어옴") {
                    Result.success(null)
                } else {
                    Result.failure(IllegalStateException("unKnownError"))
                }
            }
        }
    }
    override suspend fun postBooksInfoSeeProfile(bookKey: String, seeProfileStatus : Boolean): Result<Void?> {
        when (val data =
            bookDataSource.postBooksInfoSeeProfile(PostBooksInfoSeeProfileBody(bookKey,seeProfileStatus))) {
            is NetworkState.Success -> {
                return Result.success(data.body)
            }
            is NetworkState.Failure -> {
                return Result.failure(
                    RetrofitFailureStateException(data.error, data.code)
                )
            }
            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError -> {
                return if(data.errorState == "body값이 null로 넘어옴") {
                    Result.success(null)
                } else {
                    Result.failure(IllegalStateException("unKnownError"))
                }
            }
        }
    }
    override suspend fun deleteBooksInfoAll(bookKey: String): Result<Void?> {
        when (val data =
            bookDataSource.deleteBooksInfoAll(bookKey)) {
            is NetworkState.Success -> {
                return Result.success(data.body)
            }
            is NetworkState.Failure -> {
                return Result.failure(
                    RetrofitFailureStateException(data.error, data.code)
                )
            }
            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError -> {
                return if(data.errorState == "body값이 null로 넘어옴") {
                    Result.success(null)
                } else {
                    Result.failure(IllegalStateException("unKnownError"))
                }
            }
        }
    }
    override suspend fun postBooksInfoCurrency(currency: String, bookKey: String): Result<PostBooksInfoCurrencyModel> {
        when (val data =
            bookDataSource.postBooksInfoCurrency(PostBooksInfoCurrencyBody(currency, bookKey))) {
            is NetworkState.Success -> return Result.success(data.body.toPostBooksInfoCurrencyModel())
            is NetworkState.Failure -> return Result.failure(
                RetrofitFailureStateException(data.error, data.code)
            )
            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError ->{
                Timber.e("UnknownError ${data.errorState}, ${data.t}")
                return Result.failure(IllegalStateException("unKnownError"))
            }
        }
    }
    override suspend fun getBooksInfoCurrency(bookKey: String): Result<GetBooksInfoCurrencyModel> {
        when (val data =
            bookDataSource.getBooksInfoCurrency(bookKey)) {
            is NetworkState.Success -> return Result.success(data.body.toGetBooksInfoCurrencyModel())
            is NetworkState.Failure -> return Result.failure(
                RetrofitFailureStateException(data.error, data.code)
            )
            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError ->{
                Timber.e("UnknownError ${data.errorState}, ${data.t}")
                return Result.failure(IllegalStateException("unKnownError"))
            }
        }
    }
    override suspend fun postBooksInfoAsset(bookKey: String, asset: Long): Result<Void?> {
        when (val data =
            bookDataSource.postBooksInfoAsset(PostBooksInfoAssetBody(bookKey, asset))) {
            is NetworkState.Success -> {
                return Result.success(data.body)
            }
            is NetworkState.Failure -> {
                return Result.failure(
                    RetrofitFailureStateException(data.error, data.code)
                )
            }
            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError -> {
                return if(data.errorState == "body값이 null로 넘어옴") {
                    Result.success(null)
                } else {
                    Result.failure(IllegalStateException("unKnownError"))
                }
            }
        }
    }
    override suspend fun postBooksInfoCarryOver(status: Boolean, bookKey: String): Result<Void?> {
        when (val data =
            bookDataSource.postBooksInfoCarryOver(PostBooksInfoCarryOverBody(status, bookKey))) {
            is NetworkState.Success -> {
                return Result.success(data.body)
            }
            is NetworkState.Failure -> {
                return Result.failure(
                    RetrofitFailureStateException(data.error, data.code)
                )
            }
            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError -> {
                return if(data.errorState == "body값이 null로 넘어옴") {
                    Result.success(null)
                } else {
                    Result.failure(IllegalStateException("unKnownError"))
                }
            }
        }
    }
    override suspend fun getBooksBudget(bookKey: String, date: String): Result<UiBookBudgetModel> {
        when (val data =
            bookDataSource.getBooksBudget(bookKey, date)) {
            is NetworkState.Success -> return Result.success(data.body.toUiBookBudgetModel(context))
            is NetworkState.Failure -> return Result.failure(
                RetrofitFailureStateException(data.error, data.code)
            )
            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError ->{
                Timber.e("UnknownError ${data.errorState}, ${data.t}")
                return Result.failure(IllegalStateException("unKnownError"))
            }
        }
    }
    override suspend fun postBooksInfoBudget(bookKey: String, budget: Long, date: String): Result<Void?> {
        when (val data =
            bookDataSource.postBooksInfoBudget(PostBooksInfoBudgetBody(bookKey, budget, date))) {
            is NetworkState.Success -> {
                return Result.success(data.body)
            }
            is NetworkState.Failure -> {
                return Result.failure(
                    RetrofitFailureStateException(data.error, data.code)
                )
            }
            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError -> {
                return if(data.errorState == "body값이 null로 넘어옴") {
                    Result.success(null)
                } else {
                    Result.failure(IllegalStateException("unKnownError"))
                }
            }
        }
    }
    override suspend fun deleteBookCategory(bookKey: String, parent:String, name:String): Result<Void?> {
        when (val data =
            bookDataSource.deleteBookCategory(bookKey, DeleteBookCategoryBody(parent, name))) {
            is NetworkState.Success -> {
                return Result.success(data.body)
            }
            is NetworkState.Failure -> {
                return Result.failure(
                    RetrofitFailureStateException(data.error, data.code)
                )
            }
            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError -> {
                return if(data.errorState == "body값이 null로 넘어옴") {
                    Result.success(null)
                } else {
                    Result.failure(IllegalStateException("unKnownError"))
                }
            }
        }
    }
    override suspend fun postBooksCategoryAdd(bookKey: String, parent: String, name: String): Result<PostBooksCategoryAddModel> {
        when (val data =
            bookDataSource.postBooksCategoryAdd(bookKey, PostBooksCategoryAddBody(parent, name))) {
            is NetworkState.Success -> return Result.success(data.body.toPostBooksCategoryAddModel())
            is NetworkState.Failure -> return Result.failure(
                RetrofitFailureStateException(data.error, data.code)
            )
            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError ->{
                Timber.e("UnknownError ${data.errorState}, ${data.t}")
                return Result.failure(IllegalStateException("unKnownError"))
            }
        }
    }
    override suspend fun getBooksCode(bookKey: String): Result<GetBooksCodeModel> {
        when (val data =
            bookDataSource.getBooksCode(bookKey)) {
            is NetworkState.Success -> {
                return Result.success(data.body.toGetBooksCodeModel())
            }
            is NetworkState.Failure -> return Result.failure(
                RetrofitFailureStateException(data.error, data.code)
            )
            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError ->{
                Timber.e("UnknownError ${data.errorState}, ${data.t}")
                return Result.failure(IllegalStateException("unKnownError"))
            }
        }
    }
    override suspend fun getBooksRepeat(
        bookKey: String,
        categoryType: String,
    ): Result<List<UiBookRepeatModel>> {
        when (val data = bookDataSource.getBooksRepeat(bookKey, categoryType)) {
            is NetworkState.Success -> return Result.success(data.body.toUiBookRepeatModel())
            is NetworkState.Failure -> return Result.failure(
                RetrofitFailureStateException(data.error, data.code)
            )

            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError -> return Result.failure(IllegalStateException("unKnownError"))
        }
    }
    override suspend fun deleteBooksRepeat(repeatLineId: Int): Result<Void?> {
        when (val data =
            bookDataSource.deleteBooksRepeat(repeatLineId)) {
            is NetworkState.Success -> {
                return Result.success(data.body)
            }
            is NetworkState.Failure -> {
                return Result.failure(
                    RetrofitFailureStateException(data.error, data.code)
                )
            }
            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError -> {
                return if(data.errorState == "body값이 null로 넘어옴") {
                    Result.success(null)
                } else {
                    Result.failure(IllegalStateException("unKnownError"))
                }
            }
        }
    }

    override suspend fun deleteBooksLinesAll(bookLineKey: Int): Result<Void?> {
        when (val data =
            bookDataSource.deleteBookLinesAll(bookLineKey)) {
            is NetworkState.Success -> {
                return Result.success(data.body)
            }
            is NetworkState.Failure -> {
                return Result.failure(
                    RetrofitFailureStateException(data.error, data.code)
                )
            }
            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError -> {
                return if(data.errorState == "body값이 null로 넘어옴") {
                    Result.success(null)
                } else {
                    Result.failure(IllegalStateException("unKnownError"))
                }
            }
        }
    }

    override suspend fun postBooksExcel(
        bookKey: String,
        excelDuration: String,
        currentDate : String
    ): Result<ResponseBody> {
        when (val data = bookDataSource.postBooksExcel(PostBooksExcelBody(bookKey, excelDuration, currentDate))) {
            is NetworkState.Success -> return Result.success(data.body)
            is NetworkState.Failure -> return Result.failure(
                RetrofitFailureStateException(data.error, data.code)
            )

            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError -> return Result.failure(IllegalStateException("unKnownError"))
        }
    }
    override suspend fun postBooksOut(bookKey: String): Result<Void?> {
        when (val data =
            bookDataSource.postBooksOut(PostBooksOutBody(bookKey))) {
            is NetworkState.Success -> {
                return Result.success(data.body)
            }
            is NetworkState.Failure -> {
                return Result.failure(
                    RetrofitFailureStateException(data.error, data.code)
                )
            }
            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError -> {
                return if(data.errorState == "body값이 null로 넘어옴") {
                    Result.success(null)
                } else {
                    Result.failure(IllegalStateException("unKnownError"))
                }
            }
        }
    }
    override suspend fun getBooks(
        code: String
    ): Result<UiBookEntranceModel> {
        when (val data = bookDataSource.getBooks(code)) {
            is NetworkState.Success -> return Result.success(data.body.toUiBookEntranceModel(context))
            is NetworkState.Failure -> return Result.failure(
                RetrofitFailureStateException(data.error, data.code)
            )

            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError -> return Result.failure(IllegalStateException("unKnownError"))
        }
    }

    override suspend fun getBookFavorite(
        bookKey: String,
        categoryType: String
    ): Result<List<UiBookFavoriteModel>> {
        when (val data = bookDataSource.getBookFavorite(bookKey, categoryType)) {
            is NetworkState.Success -> return Result.success(data.body.toUiBookFavorite())
            is NetworkState.Failure -> return Result.failure(
                RetrofitFailureStateException(data.error, data.code)
            )

            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError -> return Result.failure(IllegalStateException("unKnownError"))
        }
    }

    override suspend fun deleteBookFavorite(bookKey: String, favoriteId: Int): Result<Void?> {
        when (val data =
            bookDataSource.deleteBookFavorite(bookKey, favoriteId)) {
            is NetworkState.Success -> {
                return Result.success(data.body)
            }
            is NetworkState.Failure -> {
                return Result.failure(
                    RetrofitFailureStateException(data.error, data.code)
                )
            }
            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError -> {
                return if(data.errorState == "body값이 null로 넘어옴") {
                    Result.success(null)
                } else {
                    Result.failure(IllegalStateException("unKnownError"))
                }
            }
        }
    }

    override suspend fun postChangeBookImg(bookKey: String, url: String): Result<Void?> {
        when (val data =
            bookDataSource.postChangeBookImg(PostChangeBookImgBody(url, bookKey))) {
            is NetworkState.Success -> {
                return Result.success(data.body)
            }
            is NetworkState.Failure -> {
                return Result.failure(
                    RetrofitFailureStateException(data.error, data.code)
                )
            }
            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError -> {
                return if(data.errorState == "body값이 null로 넘어옴") {
                    Result.success(null)
                } else {
                    Result.failure(IllegalStateException("unKnownError"))
                }
            }
        }
    }

    override suspend fun postBooksFavorites(
        bookKey: String,
        money:Double,
        description: String,
        lineCategoryName : String,
        lineSubcategoryName : String,
        assetSubcategoryName : String,
        exceptStatus : Boolean
    ): Result<PostBookFavoriteModel> {
        when (val data = bookDataSource.postBooksFavorites(bookKey,
            PostBooksFavoritesBody(money, description, lineCategoryName, lineSubcategoryName, assetSubcategoryName, exceptStatus))
        ) {
            is NetworkState.Success -> return Result.success(data.body.toPostBookFavoriteModel())
            is NetworkState.Failure -> return Result.failure(
                RetrofitFailureStateException(data.error, data.code)
            )

            is NetworkState.NetworkError -> return Result.failure(IllegalStateException("NetworkError"))
            is NetworkState.UnknownError -> return Result.failure(IllegalStateException("unKnownError"))
        }
    }
}