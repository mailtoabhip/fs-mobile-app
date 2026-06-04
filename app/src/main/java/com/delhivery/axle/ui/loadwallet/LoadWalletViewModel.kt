package com.delhivery.axle.ui.loadwallet

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.WalletRepository
import com.delhivery.axle.api.response.UserWalletResponse
import com.delhivery.axle.api.service.WalletApiService
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.extensions.convertResponse
import com.delhivery.axle.utils.extensions.errorResponseBody
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.extensions.safeDispose
import com.delhivery.axle.utils.prefs.UserPrefs
import com.google.gson.JsonObject
import io.reactivex.disposables.Disposable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel for [LoadWalletActivity]
 */
class LoadWalletViewModel @Inject constructor(
    private val walletApiService: WalletApiService,
    private val walletRepository: WalletRepository,
    private val userPrefs: UserPrefs
) : BaseViewModel() {

    var walletBalance = MutableLiveData<String>()
    var historyLiveData = MutableLiveData<List<BaseLoadWalletRVAdapterItem<*>>>()
    var rechargesLiveData = MutableLiveData<List<BaseLoadWalletRVAdapterItem<*>>>()
    var errorLiveData = MutableLiveData<Boolean>()

    // In-memory accumulators for pagination — DiffUtil needs the full list each time
    private val transactionAccumulator = mutableListOf<BaseLoadWalletRVAdapterItem<*>>()
    private val rechargeAccumulator = mutableListOf<BaseLoadWalletRVAdapterItem<*>>()

    // Cursor-based pagination state for wallet transactions
    private val pageSize = 10
    private var transactionCursor: String? = null
    private var isFetchingTransactions = false
    private var hasMoreTransactions = true
    private var currentTransactionFilter = WalletFilter()
    private var transactionDisposable: Disposable? = null

    /** Emits true when a page is loading, false when done */
    var transactionLoadingLiveData = MutableLiveData<Boolean>()

    /** Emits true only during the initial/reset load (not pagination) */
    var transactionInitialLoadingLiveData = MutableLiveData<Boolean>(false)

    // Cursor-based pagination state for recharges
    private var rechargeCursor: String? = null
    private var isFetchingRecharges = false
    private var hasMoreRecharges = true
    private var currentRechargeFilter = WalletFilter()
    private var rechargeDisposable: Disposable? = null

    /** Emits true when a recharge page is loading, false when done */
    var rechargeLoadingLiveData = MutableLiveData<Boolean>()

    /** Emits true only during the initial/reset recharge load (not pagination) */
    var rechargeInitialLoadingLiveData = MutableLiveData<Boolean>(false)

    /** true = wallet exists, false = wallet not found */
    var walletExistsLiveData = MutableLiveData<Boolean>()
    var walletDetailsLiveData = MutableLiveData<UserWalletResponse?>()
    var walletErrorLiveData = MutableLiveData<String?>()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    /**
     * Calculate start and end dates from filter
     */
    private fun getDateRange(filter: WalletFilter): Pair<String, String> {
        val now = Calendar.getInstance()
        val end = dateFormat.format(now.time)
        val start: String

        when (filter.dateRange) {
            FilterDateRange.TODAY -> {
                val startOfToday = Calendar.getInstance().apply { resetTime() }
                start = dateFormat.format(startOfToday.time)
            }
            FilterDateRange.YESTERDAY -> {
                val startOfYesterday = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -1)
                    resetTime()
                }
                val endOfYesterday = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -1)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                return Pair(dateFormat.format(startOfYesterday.time), dateFormat.format(endOfYesterday.time))
            }
            FilterDateRange.THIS_WEEK -> {
                val startOfWeek = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                    resetTime()
                }
                start = dateFormat.format(startOfWeek.time)
            }
            FilterDateRange.LAST_WEEK -> {
                val startOfLastWeek = Calendar.getInstance().apply {
                    add(Calendar.WEEK_OF_YEAR, -1)
                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                    resetTime()
                }
                val endOfLastWeek = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                    add(Calendar.DAY_OF_YEAR, -1)
                    resetTime()
                }
                start = dateFormat.format(startOfLastWeek.time)
                return Pair(start, dateFormat.format(endOfLastWeek.time))
            }
            FilterDateRange.THIS_MONTH -> {
                val startOfMonth = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    resetTime()
                }
                start = dateFormat.format(startOfMonth.time)
            }
            FilterDateRange.LAST_MONTH -> {
                val startOfLastMonth = Calendar.getInstance().apply {
                    add(Calendar.MONTH, -1)
                    set(Calendar.DAY_OF_MONTH, 1)
                    resetTime()
                }
                val endOfLastMonth = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    add(Calendar.DAY_OF_YEAR, -1)
                    resetTime()
                }
                start = dateFormat.format(startOfLastMonth.time)
                return Pair(start, dateFormat.format(endOfLastMonth.time))
            }
            FilterDateRange.LAST_90_DAYS -> {
                val ninetyDaysAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -90) }
                start = dateFormat.format(ninetyDaysAgo.time)
            }
            FilterDateRange.CUSTOM -> {
                val customStart = filter.customStartDate
                val customEnd = filter.customEndDate
                if (customStart != null && customEnd != null) {
                    return Pair(dateFormat.format(customStart.time), dateFormat.format(customEnd.time))
                }
                val ninetyDaysAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -90) }
                start = dateFormat.format(ninetyDaysAgo.time)
            }
            else -> {
                val ninetyDaysAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -90) }
                start = dateFormat.format(ninetyDaysAgo.time)
            }
        }
        return Pair(start, end)
    }

    /**
     * Fetch wallet data and transaction listing — resets pagination and loads first page
     */
    fun fetchWalletData(filter: WalletFilter = WalletFilter()) {
        transactionDisposable.safeDispose()
        transactionCursor = null
        hasMoreTransactions = true
        isFetchingTransactions = false
        currentTransactionFilter = filter
        transactionAccumulator.clear()
        loadTransactionPage(filter, reset = true)
    }

    /**
     * Load the next page of transactions (call when user scrolls to bottom)
     */
    fun loadNextTransactionPage() {
        if (isFetchingTransactions || !hasMoreTransactions) return
        loadTransactionPage(currentTransactionFilter, reset = false)
    }

    private fun loadTransactionPage(filter: WalletFilter, reset: Boolean) {
        val (start, end) = getDateRange(filter)
        val type = when (filter.type) {
            FilterType.CREDIT -> "credit"
            FilterType.DEBIT -> "debit"
            else -> null
        }
        isFetchingTransactions = true
        transactionLoadingLiveData.postValue(true)
        if (reset) transactionInitialLoadingLiveData.postValue(true)

        transactionDisposable = walletApiService.fetchTransactions(
            start = start,
            end = end,
            type = type,
            limit = pageSize,
            cursor = transactionCursor
        )
            .convertResponse()
            .onBackground()
            .subscribe({ result ->
                isFetchingTransactions = false
                transactionLoadingLiveData.postValue(false)
                if (reset) transactionInitialLoadingLiveData.postValue(false)

                val items = result.transactions
                    .map { txn ->
                        WalletHistoryItemData(
                            title = txn.transactionHeading(),
                            amount = txn.amountDouble(),
                            dateTime = txn.createdAt,
                            status = txn.status,
                            txnNumber = txn.transactionId,
                            type = txn.transactionType,
                            transactionReason = txn.txnReason
                        )
                    }

                hasMoreTransactions = result.hasNext
                transactionCursor = result.nextCursor

                val newItems = items.map { WalletHistoryItem(it) as BaseLoadWalletRVAdapterItem<*> }
                transactionAccumulator.addAll(newItems)
                historyLiveData.postValue(transactionAccumulator.toList())
            }, {
                isFetchingTransactions = false
                transactionLoadingLiveData.postValue(false)
                if (reset) {
                    transactionInitialLoadingLiveData.postValue(false)
                    errorLiveData.postValue(true)
                }
            })
        compositeDisposable += transactionDisposable!!
    }

    private fun Calendar.resetTime() {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    /**
     * Fetch wallet recharges — resets pagination and loads first page
     */
    fun fetchRecharges(filter: WalletFilter = WalletFilter()) {
        rechargeDisposable.safeDispose()
        rechargeCursor = null
        hasMoreRecharges = true
        isFetchingRecharges = false
        currentRechargeFilter = filter
        rechargeAccumulator.clear()
        loadRechargePage(filter, reset = true)
    }

    /**
     * Load the next page of recharges (call when user scrolls to bottom)
     */
    fun loadNextRechargePage() {
        if (isFetchingRecharges || !hasMoreRecharges) return
        loadRechargePage(currentRechargeFilter, reset = false)
    }

    private fun loadRechargePage(filter: WalletFilter, reset: Boolean) {
        val (start, end) = getDateRange(filter)
        isFetchingRecharges = true
        rechargeLoadingLiveData.postValue(true)
        if (reset) rechargeInitialLoadingLiveData.postValue(true)

        rechargeDisposable = walletApiService.fetchRechargeHistory(
            start = start,
            end = end,
            limit = pageSize,
            cursor = rechargeCursor
        )
            .convertResponse()
            .onBackground()
            .subscribe({ result ->
                isFetchingRecharges = false
                rechargeLoadingLiveData.postValue(false)
                if (reset) rechargeInitialLoadingLiveData.postValue(false)

                val items = result.recharges
                    .map { recharge ->
                        WalletHistoryItemData(
                            title = "Recharge",
                            amount = recharge.amount.toDoubleOrNull() ?: 0.0,
                            dateTime = recharge.createdAt,
                            status = recharge.status,
                            txnNumber = recharge.rechargeId,
                            type = "credit"
                        )
                    }

                hasMoreRecharges = result.hasNext
                rechargeCursor = result.nextCursor

                val newItems = items.map { WalletHistoryItem(it) as BaseLoadWalletRVAdapterItem<*> }
                rechargeAccumulator.addAll(newItems)
                rechargesLiveData.postValue(rechargeAccumulator.toList())
            }, {
                isFetchingRecharges = false
                rechargeLoadingLiveData.postValue(false)
                if (reset) {
                    rechargeInitialLoadingLiveData.postValue(false)
                    errorLiveData.postValue(true)
                }
            })
        compositeDisposable += rechargeDisposable!!
    }

    /**
     * Fetch wallet details from GET /api/v1/wallet
     * If wallet exists → set balance, notify walletExistsLiveData(true)
     * If 404 → notify walletExistsLiveData(false)
     */
    fun fetchWalletDetails() {
        compositeDisposable += walletApiService.fetchWalletInfo(
        )
            .convertResponse()
            .onBackground()
            .subscribe({ result ->
                userPrefs.walletId = result.walletId
                walletDetailsLiveData.postValue(result)
                walletBalance.postValue(result.balanceFormatted())
                walletExistsLiveData.postValue(true)
            }, { error ->
                val errorBody = error.errorResponseBody()
                if (errorBody != null && errorBody.errorBody.errorCode() == 404) {
                    walletExistsLiveData.postValue(false)
                } else {
                    walletErrorLiveData.postValue("Unable to fetch wallet details")
                }
            })
    }

    /**
     * Create wallet via POST /api/v1/wallet
     * On success → set balance, notify walletExistsLiveData(true)
     */
    fun createWallet(email: String = "") {
        val request = JsonObject().apply {
            addProperty("user_type", "SUPPLIER")
            addProperty("phone", userPrefs.phoneNumber ?: "")
            addProperty("email", email)
        }
        compositeDisposable += walletApiService.createWallet(
            request = request
        )
            .convertResponse()
            .onBackground()
            .subscribe({ result ->
                userPrefs.walletId = result.walletId
                walletDetailsLiveData.postValue(result)
                walletBalance.postValue(result.balanceFormatted())
                walletExistsLiveData.postValue(true)
            }, {
                walletErrorLiveData.postValue("Unable to create wallet")
            })
    }

    /** LiveData for single transaction status refresh result */
    var refreshStatusLiveData = MutableLiveData<Pair<String, String>?>()
    var refreshStatusErrorLiveData = MutableLiveData<String?>()

    /** LiveData for single recharge status refresh result */
    var refreshRechargeStatusLiveData = MutableLiveData<Pair<String, String>?>()
    var refreshRechargeStatusErrorLiveData = MutableLiveData<String?>()

    /**
     * Refresh status of a single pending transaction via txn_id filter
     */
    fun refreshTransactionStatus(txnId: String, createdAt: String) {
        compositeDisposable += walletApiService.fetchTransactions(
            txnId = txnId,
            limit = 1
        )
            .convertResponse()
            .onBackground()
            .subscribe({ result ->
                val txn = result.transactions.firstOrNull()
                if (txn != null) {
                    refreshStatusLiveData.postValue(Pair(txn.transactionId, txn.status))
                } else {
                    refreshStatusErrorLiveData.postValue(txnId)
                }
            }, {
                refreshStatusErrorLiveData.postValue(txnId)
            })
    }

    /**
     * Refresh status of a single pending recharge
     */
    fun refreshRechargeStatus(rechargeId: String, createdAt: String) {
        val request = JsonObject().apply {
            addProperty("recharge_id", rechargeId)
        }
        compositeDisposable += walletApiService.fetchRechargeStatus(
            request = request
        )
            .convertResponse()
            .onBackground()
            .subscribe({ result ->
                refreshRechargeStatusLiveData.postValue(Pair(result.rechargeId, result.status))
            }, {
                refreshRechargeStatusErrorLiveData.postValue(rechargeId)
            })
    }
}
