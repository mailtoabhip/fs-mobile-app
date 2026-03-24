package com.delhivery.axle.ui.invoicereview

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityInvoiceReviewBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.utils.WindowInsetsUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Activity for GST vendors to review and accept/reject invoices
 */
class InvoiceReviewActivity : BaseActivity<ActivityInvoiceReviewBinding, InvoiceReviewViewModel>(), DatePickerDialog.OnDateSetListener {

    companion object {
        const val EXTRA_TRANSACTION_ID = "transaction_id"
        const val RESULT_INVOICE_REVIEWED = 100
        private const val API_DATE_FORMAT = "yyyy-MM-dd"
        private const val DISPLAY_DATE_FORMAT = "dd/MM/yyyy"

        fun invoiceReviewIntent(transactionId: String, context: Context): Intent {
            return Intent(context, InvoiceReviewActivity::class.java).apply {
                putExtra(EXTRA_TRANSACTION_ID, transactionId)
            }
        }
    }

    override fun getViewModelClass() = InvoiceReviewViewModel::class.java
    override fun layoutId() = R.layout.activity_invoice_review

    private lateinit var particularsAdapter: InvoiceParticularAdapter
    private var selectedYear: Int = 0
    private var selectedMonth: Int = 0
    private var selectedDay: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val transactionId = intent?.getStringExtra(EXTRA_TRANSACTION_ID)?:""

        viewModel.transactionId = transactionId

        setupToolbar()
        setupRecyclerView()
        setupDatePicker()
        setupClickListeners()
        setupBackNavigation()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Handle window insets for edge-to-edge display (API 35+)
        if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
        }

        observeViewModel()
        viewModel.fetchInvoiceDetails()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        particularsAdapter = InvoiceParticularAdapter()
        binding.rvInvoiceParticulars.apply {
            layoutManager = LinearLayoutManager(this@InvoiceReviewActivity)
            adapter = particularsAdapter
        }
    }

    private fun setupDatePicker() {
        // Prefill with current date
        val today = Calendar.getInstance()
        selectedYear = today.get(Calendar.YEAR)
        selectedMonth = today.get(Calendar.MONTH)
        selectedDay = today.get(Calendar.DAY_OF_MONTH)
        
        updateDateDisplay()
        
        binding.tvInvoiceDate.setOnClickListener {
            dialogUtils.datePicker(this, minDate = -30, maxDate = 30)
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        selectedYear = year
        selectedMonth = monthOfYear
        selectedDay = dayOfMonth
        updateDateDisplay()
    }

    private fun updateDateDisplay() {
        val displayFormat = SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault())
        val calendar = Calendar.getInstance().apply {
            set(selectedYear, selectedMonth, selectedDay)
        }
        binding.tvInvoiceDate.setText(displayFormat.format(calendar.time))
    }

    private fun getApiFormattedDate(): String {
        val apiFormat = SimpleDateFormat(API_DATE_FORMAT, Locale.getDefault())
        val calendar = Calendar.getInstance().apply {
            set(selectedYear, selectedMonth, selectedDay)
        }
        return apiFormat.format(calendar.time)
    }

    private fun setupClickListeners() {
        binding.btnAccept.setOnClickListener {
            val invoiceNumber = binding.etInvoiceNo.text?.toString()?.trim() ?: ""
            val invoiceDate = getApiFormattedDate()

            if (viewModel.validateInputs(invoiceNumber, false)) {
                showAcceptConfirmationDialog(invoiceNumber, invoiceDate)
            }
        }

        binding.btnReject.setOnClickListener {
            val invoiceNumber = binding.etInvoiceNo.text?.toString()?.trim() ?: ""
            val invoiceDate = getApiFormattedDate()
            if (viewModel.validateInputs(invoiceNumber, true)) {
                showRejectConfirmationDialog(invoiceNumber, invoiceDate)
            }
        }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    private fun observeViewModel() {
        viewModel.invoiceDetailsLiveData.observe(this, Observer { invoice ->
            binding.invoice = invoice
            binding.bottomButtons.visibility = View.VISIBLE
            binding.scrollContent.visibility = View.VISIBLE
            val particulars = invoice.invoiceParticulars
            if (particulars.isNullOrEmpty()) {
                binding.rvInvoiceParticulars.visibility = android.view.View.GONE
            } else {
                binding.rvInvoiceParticulars.visibility = android.view.View.VISIBLE
                particularsAdapter.submitList(particulars)
            }
        })

        viewModel.isLoadingLiveData.observe(this, Observer { isLoading ->
            if (isLoading) {
                uiUtils.showProgress()
                binding.btnAccept.isEnabled = false
                binding.btnReject.isEnabled = false
            } else {
                uiUtils.hideProgress()
                binding.btnAccept.isEnabled = true
                binding.btnReject.isEnabled = true
            }
        })

        viewModel.invoiceActionResponseMsgLiveData.observe(this, Observer { message ->
                uiUtils.showSnackbar(message)
                setResult(RESULT_INVOICE_REVIEWED)
                finish()
        })

        viewModel.errorLiveData.observe(this, Observer { error ->
            uiUtils.showSnackbar(error)
        })
        viewModel.invoiceNumberErrorLiveData.observe(this, Observer { error ->
            error?.let{uiUtils.showSnackbar(error)}
        })

        viewModel.alreadyProcessedLiveData.observe(this, Observer { alreadyProcessed ->
            if (alreadyProcessed) {
                uiUtils.showSnackbar("This invoice has already been processed")
                viewModel.fetchInvoiceDetails() // Refresh to get latest status
            }
        })
    }

    private fun showAcceptConfirmationDialog(invoiceNumber: String, invoiceDate: String) {
        InvoiceConfirmationDialog(
            context = this,
            type = InvoiceConfirmationDialog.ConfirmationType.ACCEPT,
            onConfirm = {
                viewModel.acceptRejectInvoice(confirmationType = InvoiceConfirmationDialog.ConfirmationType.ACCEPT, invoiceNumber, invoiceDate)
            }
        ).show()
    }

    private fun showRejectConfirmationDialog(invoiceNumber: String, invoiceDate: String) {
        InvoiceConfirmationDialog(
            context = this,
            type = InvoiceConfirmationDialog.ConfirmationType.REJECT,
            onConfirm = {
                viewModel.acceptRejectInvoice(
                    confirmationType = InvoiceConfirmationDialog.ConfirmationType.REJECT,
                    invoiceNumber = invoiceNumber,
                    invoiceDate = invoiceDate
                )
            }
        ).show()
    }
}
