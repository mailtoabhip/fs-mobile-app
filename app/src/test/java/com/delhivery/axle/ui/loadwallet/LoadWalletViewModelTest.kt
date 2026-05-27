package com.delhivery.axle.ui.loadwallet

import com.delhivery.axle.api.repository.WalletRepository
import com.delhivery.axle.api.response.BaseErrorResponse
import com.delhivery.axle.api.response.UserWalletResponse
import com.delhivery.axle.api.response.WalletTransactionHistoryResponse
import com.delhivery.axle.api.response.WalletRechargeHistoryResponse
import com.delhivery.axle.api.response.RechargeStatusResponse
import com.delhivery.axle.api.service.WalletApiService
import com.delhivery.axle.testdata.WalletTestDataFactory
import com.delhivery.axle.testutils.ViewModelTestSetup
import com.delhivery.axle.testutils.captureValues
import com.delhivery.axle.testutils.relaxedMock
import com.delhivery.axle.testutils.strictMock
import com.google.gson.JsonObject
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldEndWith
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import io.reactivex.Single
import okhttp3.MediaType
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import com.delhivery.axle.utils.prefs.UserPrefs

/**
 * Unit tests for LoadWalletViewModel using Kotest and MockK.
 *
 * Covers:
 * - Wallet details fetching (exists / not found / error)
 * - Wallet creation
 * - Transaction history with cursor-based pagination
 * - Recharge history with cursor-based pagination
 * - Filter-based date range calculations
 * - Transaction status refresh
 * - Recharge status refresh
 * - Loading state transitions
 * - Error handling
 */
class LoadWalletViewModelTest : FunSpec({

    lateinit var walletApiService: WalletApiService
    lateinit var walletRepository: WalletRepository
    lateinit var userPrefs: UserPrefs
    lateinit var viewModel: LoadWalletViewModel

    beforeSpec {
        ViewModelTestSetup.setup()
    }

    afterSpec {
        ViewModelTestSetup.teardown()
    }

    beforeEach {
        walletApiService = strictMock()
        walletRepository = relaxedMock()
        userPrefs = relaxedMock()
        every { userPrefs.phoneNumber } returns "9876543210"
        every { userPrefs.walletId = any() } returns Unit
        viewModel = LoadWalletViewModel(walletApiService, walletRepository, userPrefs)
    }

    // ==================== fetchWalletDetails Tests ====================

    context("fetchWalletDetails") {

        context("success scenarios") {
            test("wallet exists updates balance and walletExistsLiveData") {
                val walletResponse = WalletTestDataFactory.createUserWalletResponse(
                    currentBalance = "5000.00",
                    walletId = "W123"
                )
                every { walletApiService.fetchWalletInfo() } returns Single.just(
                    com.delhivery.axle.api.response.BaseResponse(
                        responseData = walletResponse,
                        isSuccess = true,
                        errorBody = null
                    )
                )

                viewModel.fetchWalletDetails()

                viewModel.walletExistsLiveData.value shouldBe true
                viewModel.walletDetailsLiveData.value shouldBe walletResponse
                viewModel.walletBalance.value.shouldNotBeNull()
                verify { userPrefs.walletId = "W123" }
            }

            test("zero balance wallet still reports exists") {
                val walletResponse = WalletTestDataFactory.createUserWalletResponse(
                    currentBalance = "0.00"
                )
                every { walletApiService.fetchWalletInfo() } returns Single.just(
                    com.delhivery.axle.api.response.BaseResponse(
                        responseData = walletResponse,
                        isSuccess = true,
                        errorBody = null
                    )
                )

                viewModel.fetchWalletDetails()

                viewModel.walletExistsLiveData.value shouldBe true
            }
        }

        context("wallet not found") {
            test("404 error sets walletExistsLiveData to false") {
                val errorBody = ResponseBody.create(
                    MediaType.parse("application/json"),
                    """{"success":false,"error":{"message":"Not found","code":404},"data":{"success":false}}"""
                )
                val httpException = HttpException(Response.error<Any>(404, errorBody))
                every { walletApiService.fetchWalletInfo() } returns Single.error(httpException)

                viewModel.fetchWalletDetails()

                viewModel.walletExistsLiveData.value shouldBe false
            }
        }

        context("error handling") {
            test("non-404 error sets walletErrorLiveData") {
                val error = RuntimeException("Network error")
                every { walletApiService.fetchWalletInfo() } returns Single.error(error)

                viewModel.fetchWalletDetails()

                viewModel.walletErrorLiveData.value shouldBe "Unable to fetch wallet details"
            }
        }
    }

    // ==================== createWallet Tests ====================

    context("createWallet") {

        context("success scenarios") {
            test("creates wallet and updates state") {
                val walletResponse = WalletTestDataFactory.createUserWalletResponse(
                    walletId = "NEW_WALLET"
                )
                val requestSlot = slot<JsonObject>()
                every { walletApiService.createWallet(capture(requestSlot)) } returns Single.just(
                    com.delhivery.axle.api.response.BaseResponse(
                        responseData = walletResponse,
                        isSuccess = true,
                        errorBody = null
                    )
                )

                viewModel.createWallet()

                viewModel.walletExistsLiveData.value shouldBe true
                viewModel.walletDetailsLiveData.value shouldBe walletResponse
                verify { userPrefs.walletId = "NEW_WALLET" }
            }

            test("sends correct request payload") {
                val walletResponse = WalletTestDataFactory.createUserWalletResponse()
                val requestSlot = slot<JsonObject>()
                every { walletApiService.createWallet(capture(requestSlot)) } returns Single.just(
                    com.delhivery.axle.api.response.BaseResponse(
                        responseData = walletResponse,
                        isSuccess = true,
                        errorBody = null
                    )
                )

                viewModel.createWallet()

                requestSlot.captured.get("user_type").asString shouldBe "SUPPLIER"
                requestSlot.captured.get("phone").asString shouldBe "9876543210"
            }
        }

        context("error handling") {
            test("error sets walletErrorLiveData") {
                every { walletApiService.createWallet(any()) } returns
                    Single.error(RuntimeException("Server error"))

                viewModel.createWallet()

                viewModel.walletErrorLiveData.value shouldBe "Unable to create wallet"
            }
        }

        context("email parameter") {
            test("provided email is included in request payload") {
                val walletResponse = WalletTestDataFactory.createUserWalletResponse()
                val requestSlot = slot<JsonObject>()
                every { walletApiService.createWallet(capture(requestSlot)) } returns Single.just(
                    com.delhivery.axle.api.response.BaseResponse(
                        responseData = walletResponse,
                        isSuccess = true,
                        errorBody = null
                    )
                )

                viewModel.createWallet(email = "supplier@example.com")

                requestSlot.captured.get("email").asString shouldBe "supplier@example.com"
            }

            test("empty email is sent when not provided") {
                val walletResponse = WalletTestDataFactory.createUserWalletResponse()
                val requestSlot = slot<JsonObject>()
                every { walletApiService.createWallet(capture(requestSlot)) } returns Single.just(
                    com.delhivery.axle.api.response.BaseResponse(
                        responseData = walletResponse,
                        isSuccess = true,
                        errorBody = null
                    )
                )

                viewModel.createWallet()

                requestSlot.captured.get("email").asString shouldBe ""
            }
        }
    }

    // ==================== fetchWalletData (Transactions) Tests ====================

    context("fetchWalletData - transactions") {

        context("success scenarios") {
            test("fetches first page of transactions") {
                val response = WalletTestDataFactory.createTransactionHistoryResponse()
                every {
                    walletApiService.fetchTransactions(
                        start = any(),
                        end = any(),
                        txnId = null,
                        type = null,
                        status = null,
                        limit = 10,
                        cursor = null
                    )
                } returns Single.just(
                    com.delhivery.axle.api.response.BaseResponse(
                        responseData = response,
                        isSuccess = true,
                        errorBody = null
                    )
                )

                viewModel.fetchWalletData()

                viewModel.historyLiveData.value.shouldNotBeNull()
                viewModel.historyLiveData.value!! shouldHaveSize 2
            }

            test("empty transactions returns empty list") {
                val response = WalletTestDataFactory.createEmptyTransactionResponse()
                every {
                    walletApiService.fetchTransactions(
                        start = any(), end = any(), txnId = null,
                        type = null, status = null, limit = 10, cursor = null
                    )
                } returns Single.just(
                    com.delhivery.axle.api.response.BaseResponse(
                        responseData = response,
                        isSuccess = true,
                        errorBody = null
                    )
                )

                viewModel.fetchWalletData()

                viewModel.historyLiveData.value.shouldNotBeNull()
                viewModel.historyLiveData.value!! shouldHaveSize 0
            }
        }

        context("pagination") {
            test("loads next page when hasNext is true") {
                val page1 = WalletTestDataFactory.createPaginatedTransactionResponse(
                    page = 1, hasNext = true, nextCursor = "cursor_1"
                )
                val page2 = WalletTestDataFactory.createTransactionHistoryResponse(
                    hasNext = false, nextCursor = null,
                    transactions = listOf(WalletTestDataFactory.createTransactionItem(transactionId = "TXN_P2"))
                )

                every {
                    walletApiService.fetchTransactions(
                        start = any(), end = any(), txnId = null,
                        type = null, status = null, limit = 10, cursor = null
                    )
                } returns Single.just(
                    com.delhivery.axle.api.response.BaseResponse(responseData = page1, isSuccess = true, errorBody = null)
                )
                every {
                    walletApiService.fetchTransactions(
                        start = any(), end = any(), txnId = null,
                        type = null, status = null, limit = 10, cursor = "cursor_1"
                    )
                } returns Single.just(
                    com.delhivery.axle.api.response.BaseResponse(responseData = page2, isSuccess = true, errorBody = null)
                )

                viewModel.fetchWalletData()
                viewModel.loadNextTransactionPage()

                viewModel.historyLiveData.value!! shouldHaveSize 11
            }

            test("does not load next page when hasNext is false") {
                val response = WalletTestDataFactory.createTransactionHistoryResponse(hasNext = false)
                every {
                    walletApiService.fetchTransactions(
                        start = any(), end = any(), txnId = null,
                        type = null, status = null, limit = 10, cursor = null
                    )
                } returns Single.just(
                    com.delhivery.axle.api.response.BaseResponse(responseData = response, isSuccess = true, errorBody = null)
                )

                viewModel.fetchWalletData()
                viewModel.loadNextTransactionPage()

                // Should only have called API once (no second page fetch)
                verify(exactly = 1) {
                    walletApiService.fetchTransactions(
                        start = any(), end = any(), txnId = any(),
                        type = any(), status = any(), limit = any(), cursor = any()
                    )
                }
            }
        }

        context("filters") {
            test("credit filter passes type=credit to API") {
                val response = WalletTestDataFactory.createTransactionHistoryResponse()
                every {
                    walletApiService.fetchTransactions(
                        start = any(), end = any(), txnId = null,
                        type = "credit", status = null, limit = 10, cursor = null
                    )
                } returns Single.just(
                    com.delhivery.axle.api.response.BaseResponse(responseData = response, isSuccess = true, errorBody = null)
                )

                val filter = WalletFilter(type = FilterType.CREDIT, dateRange = FilterDateRange.LAST_90_DAYS)
                viewModel.fetchWalletData(filter)

                verify {
                    walletApiService.fetchTransactions(
                        start = any(), end = any(), txnId = null,
                        type = "credit", status = null, limit = 10, cursor = null
                    )
                }
            }

            test("debit filter passes type=debit to API") {
                val response = WalletTestDataFactory.createTransactionHistoryResponse()
                every {
                    walletApiService.fetchTransactions(
                        start = any(), end = any(), txnId = null,
                        type = "debit", status = null, limit = 10, cursor = null
                    )
                } returns Single.just(
                    com.delhivery.axle.api.response.BaseResponse(responseData = response, isSuccess = true, errorBody = null)
                )

                val filter = WalletFilter(type = FilterType.DEBIT, dateRange = FilterDateRange.LAST_90_DAYS)
                viewModel.fetchWalletData(filter)

                verify {
                    walletApiService.fetchTransactions(
                        start = any(), end = any(), txnId = null,
                        type = "debit", status = null, limit = 10, cursor = null
                    )
                }
            }

            test("no type filter passes null to API") {
                val response = WalletTestDataFactory.createTransactionHistoryResponse()
                every {
                    walletApiService.fetchTransactions(
                        start = any(), end = any(), txnId = null,
                        type = null, status = null, limit = 10, cursor = null
                    )
                } returns Single.just(
                    com.delhivery.axle.api.response.BaseResponse(responseData = response, isSuccess = true, errorBody = null)
                )

                viewModel.fetchWalletData(WalletFilter())

                verify {
                    walletApiService.fetchTransactions(
                        start = any(), end = any(), txnId = null,
                        type = null, status = null, limit = 10, cursor = null
                    )
                }
            }
        }

        context("loading state") {
            test("emits loading true then false on success") {
                val response = WalletTestDataFactory.createTransactionHistoryResponse()
                every {
                    walletApiService.fetchTransactions(
                        start = any(), end = any(), txnId = null,
                        type = null, status = null, limit = 10, cursor = null
                    )
                } returns Single.just(
                    com.delhivery.axle.api.response.BaseResponse(responseData = response, isSuccess = true, errorBody = null)
                )

                val loadingStates = viewModel.transactionLoadingLiveData.captureValues()
                val initialLoadingStates = viewModel.transactionInitialLoadingLiveData.captureValues()

                viewModel.fetchWalletData()

                loadingStates shouldContainExactly listOf(true, false)
                initialLoadingStates shouldContainExactly listOf(true, false)
            }
        }

        context("error handling") {
            test("error on first page sets errorLiveData") {
                every {
                    walletApiService.fetchTransactions(
                        start = any(), end = any(), txnId = null,
                        type = null, status = null, limit = 10, cursor = null
                    )
                } returns Single.error(RuntimeException("Network error"))

                viewModel.fetchWalletData()

                viewModel.errorLiveData.value shouldBe true
            }
        }

        context("reset behavior") {
            test("fetchWalletData resets accumulator and cursor") {
                val page1 = WalletTestDataFactory.createPaginatedTransactionResponse(
                    page = 1, hasNext = true, nextCursor = "cursor_1"
                )
                val freshPage = WalletTestDataFactory.createTransactionHistoryResponse(
                    transactions = listOf(WalletTestDataFactory.createTransactionItem(transactionId = "FRESH"))
                )

                every {
                    walletApiService.fetchTransactions(
                        start = any(), end = any(), txnId = null,
                        type = null, status = null, limit = 10, cursor = null
                    )
                } returnsMany listOf(
                    Single.just(com.delhivery.axle.api.response.BaseResponse(responseData = page1, isSuccess = true, errorBody = null)),
                    Single.just(com.delhivery.axle.api.response.BaseResponse(responseData = freshPage, isSuccess = true, errorBody = null))
                )

                viewModel.fetchWalletData()
                viewModel.fetchWalletData() // reset

                viewModel.historyLiveData.value!! shouldHaveSize 1
            }
        }
    }

    // ==================== fetchRecharges Tests ====================

    context("fetchRecharges") {

        context("success scenarios") {
            test("fetches first page of recharges") {
                val response = WalletTestDataFactory.createRechargeHistoryResponse()
                every {
                    walletApiService.fetchRechargeHistory(
                        start = any(), end = any(),
                        rechargeId = null, limit = 10, cursor = null
                    )
                } returns Single.just(
                    com.delhivery.axle.api.response.BaseResponse(responseData = response, isSuccess = true, errorBody = null)
                )

                viewModel.fetchRecharges()

                viewModel.rechargesLiveData.value.shouldNotBeNull()
                viewModel.rechargesLiveData.value!! shouldHaveSize 2
            }

            test("empty recharges returns empty list") {
                val response = WalletTestDataFactory.createEmptyRechargeResponse()
                every {
                    walletApiService.fetchRechargeHistory(
                        start = any(), end = any(),
                        rechargeId = null, limit = 10, cursor = null
                    )
                } returns Single.just(
                    com.delhivery.axle.api.response.BaseResponse(responseData = response, isSuccess = true, errorBody = null)
                )

                viewModel.fetchRecharges()

                viewModel.rechargesLiveData.value!! shouldHaveSize 0
            }
        }

        context("pagination") {
            test("loads next page of recharges") {
                val page1 = WalletTestDataFactory.createPaginatedRechargeResponse(
                    page = 1, hasNext = true, nextCursor = "rch_cursor_1"
                )
                val page2 = WalletTestDataFactory.createRechargeHistoryResponse(
                    hasNext = false, nextCursor = null,
                    recharges = listOf(WalletTestDataFactory.createRechargeItem(rechargeId = "RCH_P2"))
                )

                every {
                    walletApiService.fetchRechargeHistory(
                        start = any(), end = any(),
                        rechargeId = null, limit = 10, cursor = null
                    )
                } returns Single.just(
                    com.delhivery.axle.api.response.BaseResponse(responseData = page1, isSuccess = true, errorBody = null)
                )
                every {
                    walletApiService.fetchRechargeHistory(
                        start = any(), end = any(),
                        rechargeId = null, limit = 10, cursor = "rch_cursor_1"
                    )
                } returns Single.just(
                    com.delhivery.axle.api.response.BaseResponse(responseData = page2, isSuccess = true, errorBody = null)
                )

                viewModel.fetchRecharges()
                viewModel.loadNextRechargePage()

                viewModel.rechargesLiveData.value!! shouldHaveSize 11
            }

            test("does not load next recharge page when hasNext is false") {
                val response = WalletTestDataFactory.createRechargeHistoryResponse(hasNext = false)
                every {
                    walletApiService.fetchRechargeHistory(
                        start = any(), end = any(),
                        rechargeId = null, limit = 10, cursor = null
                    )
                } returns Single.just(
                    com.delhivery.axle.api.response.BaseResponse(responseData = response, isSuccess = true, errorBody = null)
                )

                viewModel.fetchRecharges()
                viewModel.loadNextRechargePage()

                verify(exactly = 1) {
                    walletApiService.fetchRechargeHistory(
                        start = any(), end = any(),
                        rechargeId = any(), limit = any(), cursor = any()
                    )
                }
            }
        }

        context("loading state") {
            test("emits loading true then false on success") {
                val response = WalletTestDataFactory.createRechargeHistoryResponse()
                every {
                    walletApiService.fetchRechargeHistory(
                        start = any(), end = any(),
                        rechargeId = null, limit = 10, cursor = null
                    )
                } returns Single.just(
                    com.delhivery.axle.api.response.BaseResponse(responseData = response, isSuccess = true, errorBody = null)
                )

                val loadingStates = viewModel.rechargeLoadingLiveData.captureValues()
                val initialLoadingStates = viewModel.rechargeInitialLoadingLiveData.captureValues()

                viewModel.fetchRecharges()

                loadingStates shouldContainExactly listOf(true, false)
                initialLoadingStates shouldContainExactly listOf(true, false)
            }
        }

        context("error handling") {
            test("error on first page sets errorLiveData") {
                every {
                    walletApiService.fetchRechargeHistory(
                        start = any(), end = any(),
                        rechargeId = null, limit = 10, cursor = null
                    )
                } returns Single.error(RuntimeException("Network error"))

                viewModel.fetchRecharges()

                viewModel.errorLiveData.value shouldBe true
            }
        }
    }

    // ==================== date range filter calculations ====================

    context("date range calculations in fetchWalletData") {

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        fun stubTransactions(startSlot: CapturingSlot<String>, endSlot: CapturingSlot<String>) {
            val response = WalletTestDataFactory.createTransactionHistoryResponse()
            every {
                walletApiService.fetchTransactions(
                    start = capture(startSlot),
                    end = capture(endSlot),
                    txnId = null, type = null, status = null, limit = 10, cursor = null
                )
            } returns Single.just(
                com.delhivery.axle.api.response.BaseResponse(responseData = response, isSuccess = true, errorBody = null)
            )
        }

        test("TODAY: start is today at midnight") {
            val startSlot = slot<String>()
            val endSlot = slot<String>()
            stubTransactions(startSlot, endSlot)

            viewModel.fetchWalletData(WalletFilter(dateRange = FilterDateRange.TODAY))

            val today = dateFormat.format(Calendar.getInstance().time)
            startSlot.captured shouldContain today
            startSlot.captured shouldEndWith "T00:00:00"
        }

        test("YESTERDAY: start is yesterday at midnight and end is yesterday at 23:59:59") {
            val startSlot = slot<String>()
            val endSlot = slot<String>()
            stubTransactions(startSlot, endSlot)

            viewModel.fetchWalletData(WalletFilter(dateRange = FilterDateRange.YESTERDAY))

            val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
            val expectedDate = dateFormat.format(yesterday.time)
            startSlot.captured shouldContain expectedDate
            startSlot.captured shouldEndWith "T00:00:00"
            endSlot.captured shouldContain expectedDate
            endSlot.captured shouldEndWith "T23:59:59"
        }

        test("THIS_WEEK: start is first day of current week at midnight") {
            val startSlot = slot<String>()
            val endSlot = slot<String>()
            stubTransactions(startSlot, endSlot)

            viewModel.fetchWalletData(WalletFilter(dateRange = FilterDateRange.THIS_WEEK))

            startSlot.captured shouldEndWith "T00:00:00"
            val today = dateFormat.format(Calendar.getInstance().time)
            endSlot.captured shouldContain today
        }

        test("LAST_WEEK: start is first day of last week at midnight") {
            val startSlot = slot<String>()
            val endSlot = slot<String>()
            stubTransactions(startSlot, endSlot)

            viewModel.fetchWalletData(WalletFilter(dateRange = FilterDateRange.LAST_WEEK))

            startSlot.captured shouldEndWith "T00:00:00"
            endSlot.captured shouldEndWith "T00:00:00"
        }

        test("THIS_MONTH: start is first day of current month at midnight") {
            val startSlot = slot<String>()
            val endSlot = slot<String>()
            stubTransactions(startSlot, endSlot)

            viewModel.fetchWalletData(WalletFilter(dateRange = FilterDateRange.THIS_MONTH))

            startSlot.captured shouldContain "-01T00:00:00"
        }

        test("LAST_MONTH: start is first day of last month, end is last day of last month, both at midnight") {
            val startSlot = slot<String>()
            val endSlot = slot<String>()
            stubTransactions(startSlot, endSlot)

            viewModel.fetchWalletData(WalletFilter(dateRange = FilterDateRange.LAST_MONTH))

            val lastMonthPrefix = SimpleDateFormat("yyyy-MM", Locale.getDefault())
                .format(Calendar.getInstance().apply { add(Calendar.MONTH, -1) }.time)
            startSlot.captured shouldContain "-01T00:00:00"
            endSlot.captured shouldContain lastMonthPrefix
            endSlot.captured shouldEndWith "T00:00:00"
        }

        test("CUSTOM with dates: uses provided start and end Calendar values") {
            val startSlot = slot<String>()
            val endSlot = slot<String>()
            stubTransactions(startSlot, endSlot)

            val customStart = Calendar.getInstance().apply {
                set(2024, Calendar.JANUARY, 1, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val customEnd = Calendar.getInstance().apply {
                set(2024, Calendar.JANUARY, 31, 23, 59, 59)
                set(Calendar.MILLISECOND, 0)
            }
            val filter = WalletFilter(
                dateRange = FilterDateRange.CUSTOM,
                customStartDate = customStart,
                customEndDate = customEnd
            )

            viewModel.fetchWalletData(filter)

            startSlot.captured shouldContain "2024-01-01T"
            endSlot.captured shouldContain "2024-01-31T"
        }

        test("CUSTOM without dates falls back to last 90 days") {
            val startSlot = slot<String>()
            val endSlot = slot<String>()
            stubTransactions(startSlot, endSlot)

            viewModel.fetchWalletData(WalletFilter(dateRange = FilterDateRange.CUSTOM))

            val ninetyDaysAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -90) }
            val expectedDate = dateFormat.format(ninetyDaysAgo.time)
            startSlot.captured shouldContain expectedDate
        }
    }

    // ==================== refreshTransactionStatus Tests ====================

    context("refreshTransactionStatus") {

        test("success updates refreshStatusLiveData with id and status") {
            val txn = WalletTestDataFactory.createTransactionItem(
                transactionId = "TXN_REFRESH",
                status = "success"
            )
            val response = WalletTestDataFactory.createTransactionHistoryResponse(
                transactions = listOf(txn)
            )
            every {
                walletApiService.fetchTransactions(
                    start = null, end = null, txnId = "TXN_REFRESH",
                    type = null, status = null, limit = 1, cursor = null
                )
            } returns Single.just(
                com.delhivery.axle.api.response.BaseResponse(responseData = response, isSuccess = true, errorBody = null)
            )

            viewModel.refreshTransactionStatus("TXN_REFRESH", "2024-01-15T14:30:00")

            viewModel.refreshStatusLiveData.value shouldBe Pair("TXN_REFRESH", "success")
        }

        test("empty transactions list sets error") {
            val response = WalletTestDataFactory.createEmptyTransactionResponse()
            every {
                walletApiService.fetchTransactions(
                    start = null, end = null, txnId = "TXN_MISSING",
                    type = null, status = null, limit = 1, cursor = null
                )
            } returns Single.just(
                com.delhivery.axle.api.response.BaseResponse(responseData = response, isSuccess = true, errorBody = null)
            )

            viewModel.refreshTransactionStatus("TXN_MISSING", "2024-01-15T14:30:00")

            viewModel.refreshStatusErrorLiveData.value shouldBe "TXN_MISSING"
        }

        test("network error sets refreshStatusErrorLiveData") {
            every {
                walletApiService.fetchTransactions(
                    start = null, end = null, txnId = "TXN_ERR",
                    type = null, status = null, limit = 1, cursor = null
                )
            } returns Single.error(RuntimeException("Timeout"))

            viewModel.refreshTransactionStatus("TXN_ERR", "2024-01-15T14:30:00")

            viewModel.refreshStatusErrorLiveData.value shouldBe "TXN_ERR"
        }
    }

    // ==================== refreshRechargeStatus Tests ====================

    context("refreshRechargeStatus") {

        test("success updates refreshRechargeStatusLiveData") {
            val response = WalletTestDataFactory.createRechargeStatusResponse(
                rechargeId = "RCH_REFRESH",
                status = "success"
            )
            every {
                walletApiService.fetchRechargeStatus(any())
            } returns Single.just(
                com.delhivery.axle.api.response.BaseResponse(responseData = response, isSuccess = true, errorBody = null)
            )

            viewModel.refreshRechargeStatus("RCH_REFRESH", "2024-01-15T14:30:00")

            viewModel.refreshRechargeStatusLiveData.value shouldBe Pair("RCH_REFRESH", "success")
        }

        test("sends correct request body with recharge_id") {
            val response = WalletTestDataFactory.createRechargeStatusResponse()
            val requestSlot = slot<JsonObject>()
            every {
                walletApiService.fetchRechargeStatus(capture(requestSlot))
            } returns Single.just(
                com.delhivery.axle.api.response.BaseResponse(responseData = response, isSuccess = true, errorBody = null)
            )

            viewModel.refreshRechargeStatus("RCH_CHECK", "2024-01-15T14:30:00")

            requestSlot.captured.get("recharge_id").asString shouldBe "RCH_CHECK"
        }

        test("error sets refreshRechargeStatusErrorLiveData") {
            every {
                walletApiService.fetchRechargeStatus(any())
            } returns Single.error(RuntimeException("Server error"))

            viewModel.refreshRechargeStatus("RCH_ERR", "2024-01-15T14:30:00")

            viewModel.refreshRechargeStatusErrorLiveData.value shouldBe "RCH_ERR"
        }
    }
})
