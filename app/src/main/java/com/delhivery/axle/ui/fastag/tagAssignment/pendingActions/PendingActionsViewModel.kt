package com.delhivery.axle.ui.fastag.tagAssignment.pendingActions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.delhivery.axle.api.repository.FastagRepository
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.ui.base.BaseViewModel
import com.delhivery.axle.utils.prefs.UserPrefs
import kotlinx.coroutines.launch
import javax.inject.Inject

class PendingActionsViewModel @Inject constructor(
    private val fastagRepository: FastagRepository,
    private val userPrefs: UserPrefs
) : BaseViewModel() {

    private val _pendingOrders = MutableLiveData<List<PendingOrder>>()
    val pendingOrders: LiveData<List<PendingOrder>> = _pendingOrders

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun fetchPendingOrders() {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            when (val result = fastagRepository.getPendingActions()) {
                is Resource.Success -> {
                    _isLoading.value = false
                    val response = result.data
                    if (response != null && response.pendingActions.isNotEmpty()) {
                        _pendingOrders.value = mapResponseToOrders(response.pendingActions)
                    } else {
                        _pendingOrders.value = emptyList()
                    }
                }
                is Resource.Failure -> {
                    _isLoading.value = false
                    _error.value = "Unable to fetch pending actions"
                }
                Resource.Loading -> { /* no-op */ }
            }
        }
    }

    /**
     * Maps pre-grouped API response orders into UI PendingOrder list.
     */
    private fun mapResponseToOrders(orders: List<PendingActionOrder>): List<PendingOrder> {
        return orders.map { order ->
            val breakupItems = order.vehicleClassSummary?.map { summary ->
                com.delhivery.axle.api.request.PaymentBreakupItem(
                    vehicleClass = summary.vehicleClass,
                    quantity = summary.count
                )
            }?.let { ArrayList(it) }
            PendingOrder(
                orderId = order.orderId,
                date = formatDate(order.orderDate),
                pendingCount = order.items.size,
                vehicles = order.items.map { item ->
                    PendingVehicle(
                        vehicleClass = item.displayName,
                        referenceId = item.tagId,
                        vehicleNumber = item.vrn,
                        actionType = mapActionType(item.nextAction.key),
                        actionLabel = item.nextAction.label,
                        colorCode = item.colorCode,
                        barcodeId = item.barcodeId,
                        salesCode = order.salesCode,
                        orderId = order.orderId,
                        journeyId = item.journeyId,
                        itemId = item.itemId,
                        items = breakupItems
                    )
                },
                isExpanded = true
            )
        }
    }

    /**
     * Formats "2026-05-29" → "29 May 2026"
     */
    private fun formatDate(isoDate: String): String {
        return try {
            val input = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH)
            val output = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.ENGLISH)
            val date = input.parse(isoDate)
            if (date != null) output.format(date) else isoDate
        } catch (e: Exception) {
            isoDate
        }
    }

    private fun mapActionType(key: String): PendingActionType {
        return when (key) {
            "ADD_VEHICLE" -> PendingActionType.ADD_VEHICLE
            "ORDER_CREATED" -> PendingActionType.ORDER_CREATED
            "KYC_DONE" -> PendingActionType.KYC_DONE
            "FULL_PAYMENT/PARTIAL_PAYMENT" -> PendingActionType.FULL_PAYMENT_PARTIAL_PAYMENT
            "HOTO_DONE" -> PendingActionType.HOTO_DONE
            "TAG_ASSIGNMENT" -> PendingActionType.TAG_ASSIGNMENT
            "KYV" -> PendingActionType.KYV
            else -> PendingActionType.ADD_VEHICLE
        }
    }


    private fun getMockPendingOrders(): List<PendingOrder> {
        val orderItems = arrayListOf(
            com.delhivery.axle.api.request.PaymentBreakupItem("VC4", 2),
            com.delhivery.axle.api.request.PaymentBreakupItem("VC5", 1)
        )
        return listOf(
            PendingOrder(
                orderId = "DLV4dbeb855",
                date = "03 Jun 2026",
                pendingCount = 7,
                vehicles = listOf(
                    PendingVehicle("Vehicle Class 4", null, "UP16CP4301", PendingActionType.ADD_VEHICLE, "Add Vehicle", "BLUE", salesCode = "TP7472", orderId = "DLV4dbeb855", items = orderItems),
                    PendingVehicle("Vehicle Class 5", null, "DL01CA1269", PendingActionType.ORDER_CREATED, "Order Created", "ORANGE", salesCode = "TP7472", orderId = "DLV4dbeb855", items = orderItems),
                    PendingVehicle("Vehicle Class 7", null, "MH02XY9876", PendingActionType.KYC_DONE, "KYC", "GREEN", salesCode = "TP7472", orderId = "DLV4dbeb855", items = orderItems),
                    PendingVehicle("Vehicle Class 6", null, "KA03AB4567", PendingActionType.FULL_PAYMENT_PARTIAL_PAYMENT, "Payment Pending", "YELLOW", salesCode = "TP7472", orderId = "DLV4dbeb855", items = orderItems),
                    PendingVehicle("Vehicle Class 12", null, "TN04CD7890", PendingActionType.HOTO_DONE, "FASTag Collection", "PINK", salesCode = "TP7472", orderId = "DLV4dbeb855", items = orderItems),
                    PendingVehicle("Vehicle Class 4", "TAG123456", "HR38AL2395", PendingActionType.TAG_ASSIGNMENT, "Vehicle Assignment", "BLUE", barcodeId = "348934587348578347534", salesCode = "TP7472", orderId = "DLV4dbeb855", items = orderItems),
                    PendingVehicle("Vehicle Class 5", null, "RJ14CP6543", PendingActionType.KYV, "KYV", "ORANGE", salesCode = "TP7472", orderId = "DLV4dbeb855", journeyId = "JRN001", items = orderItems),
                ),
                isExpanded = true
            )
        )
    }
}
