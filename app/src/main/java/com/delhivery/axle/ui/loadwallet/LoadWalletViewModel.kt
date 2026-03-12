package com.delhivery.axle.ui.loadwallet

import androidx.lifecycle.MutableLiveData
import com.delhivery.axle.api.repository.LoadboardRepository
import com.delhivery.axle.api.repository.WalletRepository
import com.delhivery.axle.api.response.UserWalletResponse
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.ui.loadwallet.amountDouble
import com.delhivery.axle.ui.loadwallet.balanceFormatted
import com.delhivery.axle.ui.loadwallet.transactionHeading
import com.delhivery.axle.utils.StringUtils
import com.delhivery.axle.utils.extensions.errorResponseBody
import com.delhivery.axle.utils.extensions.not
import com.delhivery.axle.utils.extensions.onBackground
import com.delhivery.axle.utils.extensions.plusAssign
import com.delhivery.axle.utils.extensions.safeDispose
import com.delhivery.axle.utils.prefs.UserPrefs
import io.reactivex.disposables.Disposable
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel for [LoadWalletActivity]
 */
class LoadWalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository,
    private val loadboardRepository: LoadboardRepository,
    private val userPrefs: UserPrefs
) : BaseViewModel() {

    var walletBalance = MutableLiveData<String>()
    var historyLiveData = MutableLiveData<List<BaseLoadWalletRVAdapterItem<*>>>()
    var rechargesLiveData = MutableLiveData<List<BaseLoadWalletRVAdapterItem<*>>>()
    var errorLiveData = MutableLiveData<Boolean>()

    // In-memory accumulators for pagination — DiffUtil needs the full list each time
    private val transactionAccumulator = mutableListOf<BaseLoadWalletRVAdapterItem<*>>()
    private val rechargeAccumulator = mutableListOf<BaseLoadWalletRVAdapterItem<*>>()

    // Pagination state for wallet transactions
    private val pageSize = 10
    private var currentOffset = 0
    private var isFetchingTransactions = false
    private var hasMoreTransactions = true
    private var currentTransactionFilter = WalletFilter()
    // Tracks the active transaction page request — disposed when filter changes or new reset fires
    private var transactionDisposable: Disposable? = null

    /** Emits true when a page is loading, false when done */
    var transactionLoadingLiveData = MutableLiveData<Boolean>()

    /** Emits true only during the initial/reset load (not pagination) */
    var transactionInitialLoadingLiveData = MutableLiveData<Boolean>(false)

    // Pagination state for recharges
    private var rechargeOffset = 0
    private var isFetchingRecharges = false
    private var hasMoreRecharges = true
    private var currentRechargeFilter = WalletFilter()
    // Tracks the active recharge page request — disposed when filter changes or new reset fires
    private var rechargeDisposable: Disposable? = null

    /** Emits true when a recharge page is loading, false when done */
    var rechargeLoadingLiveData = MutableLiveData<Boolean>()

    /** Emits true only during the initial/reset recharge load (not pagination) */
    var rechargeInitialLoadingLiveData = MutableLiveData<Boolean>(false)

    /** true = wallet exists, false = wallet not found */
    var walletExistsLiveData = MutableLiveData<Boolean>()
    var walletDetailsLiveData = MutableLiveData<UserWalletResponse?>()
    var walletErrorLiveData = MutableLiveData<String?>()

    private fun walletId(): String = userPrefs.walletId

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS", Locale.getDefault())
    private val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Calculate start and end dates from filter
     */
    private fun getDateRange(filter: WalletFilter): Pair<String, String> {
        val now = Calendar.getInstance()
        val end = dateFormat.format(now.time)
        val start: String

        when (filter.dateRange) {
            FilterDateRange.TODAY -> {
                start = end
            }
            FilterDateRange.YESTERDAY -> {
                val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                start = dateFormat.format(yesterday.time)
                return Pair(start, start)
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
                // Fallback to last 90 days
                val ninetyDaysAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -90) }
                start = dateFormat.format(ninetyDaysAgo.time)
            }
            else -> {
                // No date filter — default to last 90 days
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
        currentOffset = 0
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
        val wId = walletId()
        isFetchingTransactions = true
        transactionLoadingLiveData.postValue(true)
        if (reset) transactionInitialLoadingLiveData.postValue(true)

        transactionDisposable = loadboardRepository.fetchWalletTransactionList(start, end, wId, pageSize, currentOffset)
            .onBackground()
            .subscribe { result, error ->
                isFetchingTransactions = false
                transactionLoadingLiveData.postValue(false)
                if (reset) transactionInitialLoadingLiveData.postValue(false)
                if (!error) {
                    val items = result.transactions
                        .map { txn ->
                            WalletHistoryItemData(
                                title = txn.transactionHeading(),
                                amount = txn.amountDouble(),
                                dateTime = txn.createdAt,
                                status = txn.status,
                                txnNumber = txn.transactionId,
                                type = txn.transactionType,
                                txnDetails = txn.txnDetails,
                                transactionReason = txn.txnReason
                            )
                        }
                        .filter { applyTypeFilter(it, filter) }

                    hasMoreTransactions = result.transactions.size >= pageSize
                    currentOffset += result.transactions.size

                    val newItems = items.map { WalletHistoryItem(it) as BaseLoadWalletRVAdapterItem<*> }
                    transactionAccumulator.addAll(newItems)
                    historyLiveData.postValue(transactionAccumulator.toList())
                } else {
                    if (reset) {
                        transactionInitialLoadingLiveData.postValue(false)
                        errorLiveData.postValue(true)
                    }
                }
            }
        compositeDisposable += transactionDisposable!!
    }

    private fun applyTypeFilter(item: WalletHistoryItemData, filter: WalletFilter): Boolean {
        return when (filter.type) {
            FilterType.CREDIT -> item.type.lowercase().contains("credit")
            FilterType.DEBIT -> !item.type.lowercase().contains("credit")
            else -> true
        }
    }

    private fun Calendar.resetTime() {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    /**
     * Refresh only wallet balance
     */
    fun refreshBalance() {
        compositeDisposable += loadboardRepository.fetchWalletDetails()
            .onBackground()
            .subscribe { result, error ->
                if (!error) {
                    walletBalance.postValue("₹${StringUtils.formatAmount(result.currentBalance)}")
                }
            }
    }

    /**
     * Fetch wallet recharges — resets pagination and loads first page
     */
    fun fetchRecharges(filter: WalletFilter = WalletFilter()) {
        rechargeDisposable.safeDispose()
        rechargeOffset = 0
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
        val wId = walletId()
        isFetchingRecharges = true
        rechargeLoadingLiveData.postValue(true)
        if (reset) rechargeInitialLoadingLiveData.postValue(true)

        rechargeDisposable = loadboardRepository.fetchWalletRechargeList(wId, start, end, pageSize, rechargeOffset)
            .onBackground()
            .subscribe { result, error ->
                isFetchingRecharges = false
                rechargeLoadingLiveData.postValue(false)
                if (reset) rechargeInitialLoadingLiveData.postValue(false)
                if (!error) {
                    val items = result.recharges
                        .map { recharge ->
                            WalletHistoryItemData(
                                title = "Recharge",
                                amount = recharge.amount,
                                dateTime = recharge.createdAt,
                                status = recharge.status,
                                txnNumber = recharge.rechargeId,
                                type = "credit",
                                bankReferenceNo = recharge.bankReferenceNo,
                                addedVia = recharge.addedVia
                            )
                        }

                    hasMoreRecharges = result.recharges.size >= pageSize
                    rechargeOffset += result.recharges.size

                    val newItems = items.map { WalletHistoryItem(it) as BaseLoadWalletRVAdapterItem<*> }
                    rechargeAccumulator.addAll(newItems)
                    rechargesLiveData.postValue(rechargeAccumulator.toList())
                } else {
                    if (reset) {
                        rechargeInitialLoadingLiveData.postValue(false)
                        errorLiveData.postValue(true)
                    }
                }
            }
        compositeDisposable += rechargeDisposable!!
    }

    /**
     * Fetch wallet details from GET /finance/users/wallet/
     * If wallet exists → set balance, notify walletExistsLiveData(true)
     * If 404 → notify walletExistsLiveData(false)
     */
    fun fetchWalletDetails() {
        compositeDisposable += loadboardRepository.fetchWalletDetails()
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
     * Create wallet via POST /finance/users/wallet/
     * On success → set balance, notify walletExistsLiveData(true)
     */
    fun createWallet() {
        compositeDisposable += loadboardRepository.createWallet()
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
    var refreshStatusErrorLiveData = MutableLiveData<Boolean>()

    /** LiveData for single recharge status refresh result */
    var refreshRechargeStatusLiveData = MutableLiveData<Pair<String, String>?>()
    var refreshRechargeStatusErrorLiveData = MutableLiveData<Boolean>()

    /**
     * Refresh status of a single pending transaction
     */
    fun refreshTransactionStatus(txnId: String, createdAt: String) {
        val start = createdAt.substring(0, 10)

        compositeDisposable += loadboardRepository.fetchTransactionStatus(start, txnId)
            .onBackground()
            .subscribe({ result ->
                refreshStatusLiveData.postValue(Pair(result.txnId, result.status))
            }, {
                refreshStatusErrorLiveData.postValue(true)
            })
    }

    /**
     * Refresh status of a single pending recharge
     */
    fun refreshRechargeStatus(rechargeId: String, createdAt: String) {
        val start = createdAt.substring(0, 10)

        compositeDisposable += loadboardRepository.fetchRechargeStatus(rechargeId, start)
            .onBackground()
            .subscribe({ result ->
                refreshRechargeStatusLiveData.postValue(Pair(result.rechargeId, result.status))
            }, {
                refreshRechargeStatusErrorLiveData.postValue(true)
            })
    }

}
