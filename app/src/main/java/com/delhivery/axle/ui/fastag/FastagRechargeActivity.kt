package com.delhivery.axle.ui.fastag

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.delhivery.axle.R
import com.delhivery.axle.databinding.ActivityFastagRechargeBinding
import com.delhivery.axle.ui.base.BaseActivity
import com.delhivery.axle.utils.StringUtils

class FastagRechargeActivity : BaseActivity<ActivityFastagRechargeBinding, FastagRechargeViewModel>() {

    override fun getViewModelClass() = FastagRechargeViewModel::class.java
    override fun layoutId() = R.layout.activity_fastag_recharge
    override fun requireConnection() = true

    private var selectedAmount: Int = 0
    private var slideStartX = 0f
    private var walletBalance: Double = 0.0
    private val insufficientBalanceHandler = android.os.Handler(android.os.Looper.getMainLooper())
    private var insufficientBalanceRunnable: Runnable? = null

    companion object {
        const val TAG_ID = "tag_id"
        const val VEHICLE_NUMBER = "vehicle_number"
        const val FASTAG_BALANCE = "fastag_balance"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (com.delhivery.axle.utils.WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            com.delhivery.axle.utils.WindowInsetsUtils.applyTopSystemWindowInsets(binding.layoutHeader)
        }

        setupUI()
        setupAmountChips()
        setupAmountInput()
        setupSlideToPayButton()
        setupObservers()

        viewModel.fetchWalletDetails()

        val tagId = intent.getStringExtra(TAG_ID)
        if (!tagId.isNullOrEmpty()) {
            viewModel.fetchFastagStatus(tagId)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        insufficientBalanceRunnable?.let { insufficientBalanceHandler.removeCallbacks(it) }
    }

    private fun setupUI() {
        val vehicleNumber = intent.getStringExtra(VEHICLE_NUMBER) ?: ""
        val fastagBalance = intent.getStringExtra(FASTAG_BALANCE) ?: "0"

        binding.tvVehicleNumber.text = vehicleNumber
        binding.tvFastagBalance.text = "FASTag Balance: ₹$fastagBalance"

        binding.ivBack.setOnClickListener { finish() }

        binding.tvAddMoney.setOnClickListener {
            Toast.makeText(this, "Add money feature coming soon", Toast.LENGTH_SHORT).show()
        }

        updateAmountDisplay(selectedAmount)
        updateSliderState()
    }

    private fun setupAmountChips() {
        val chips = listOf(
            binding.chip500 to 500,
            binding.chip1000 to 1000,
            binding.chip1500 to 1500
        )

        chips.forEach { (chip, amount) ->
            chip.setOnClickListener {
                selectedAmount = amount
                binding.etAmount.setText(amount.toString())
                binding.etAmount.setSelection(binding.etAmount.text.length)
                updateChipSelection(amount)
                updateAmountDisplay(amount)
                updateSliderState()
                checkInsufficientBalanceImmediate()
            }
        }
    }

    private fun setupAmountInput() {
        binding.etAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val amount = s?.toString()?.toIntOrNull() ?: 0
                selectedAmount = amount
                updateChipSelection(amount)
                updateAmountDisplay(amount)
                updateSliderState()
                checkInsufficientBalanceDebounced()
            }
        })
    }

    private fun updateChipSelection(amount: Int) {
        val chipMap = mapOf(
            binding.chip500 to 500,
            binding.chip1000 to 1000,
            binding.chip1500 to 1500
        )

        chipMap.forEach { (chip, chipAmount) ->
            if (chipAmount == amount) {
                chip.setBackgroundResource(R.drawable.bg_amount_chip_selected)
                chip.setTextColor(resources.getColor(android.R.color.white, theme))
            } else {
                chip.setBackgroundResource(R.drawable.bg_amount_chip)
                chip.setTextColor(android.graphics.Color.parseColor("#334155"))
            }
        }
    }

    private fun updateAmountDisplay(amount: Int) {
        val formatted = "₹$amount"
        binding.tvRechargeAmount.text = formatted
        binding.tvPayableAmount.text = formatted
    }

    private fun updateSliderState() {
        val hasInsufficientBalance = selectedAmount > 0 && selectedAmount > walletBalance
        val enabled = selectedAmount > 0 && !hasInsufficientBalance
        val container = binding.slideToPayContainer
        val thumb = binding.ivSlideThumb
        val label = binding.tvSlideLabel

        if (enabled) {
            container.setBackgroundResource(R.drawable.bg_slide_to_pay)
            thumb.setColorFilter(resources.getColor(android.R.color.black, theme))
            label.setTextColor(resources.getColor(android.R.color.white, theme))
        } else {
            container.setBackgroundResource(R.drawable.bg_slide_to_pay_disabled)
            val disabledColor = android.graphics.Color.parseColor("#99FFFFFF")
            thumb.setColorFilter(disabledColor)
            label.setTextColor(disabledColor)
        }
    }

    private fun checkInsufficientBalanceDebounced() {
        insufficientBalanceRunnable?.let { insufficientBalanceHandler.removeCallbacks(it) }
        insufficientBalanceRunnable = Runnable { updateInsufficientBalanceUI() }
        insufficientBalanceHandler.postDelayed(insufficientBalanceRunnable!!, 500)
    }

    private fun checkInsufficientBalanceImmediate() {
        insufficientBalanceRunnable?.let { insufficientBalanceHandler.removeCallbacks(it) }
        updateInsufficientBalanceUI()
    }

    private fun updateInsufficientBalanceUI() {
        val shouldShow = selectedAmount > 0 && selectedAmount > walletBalance
        val warning = binding.layoutInsufficientBalance
        val lowBalanceText = binding.tvLowBalance
        val normalBalance = binding.tvWalletBalance

        if (shouldShow) {
            val deficit = Math.ceil(selectedAmount - walletBalance).toInt()
            binding.tvInsufficientMessage.text =
                "Please add at least ₹$deficit to your Delhivery Wallet to recharge your FASTag"

            lowBalanceText.text = "LOW BALANCE: ₹${StringUtils.formatAmount(walletBalance)}"
            if (lowBalanceText.visibility != View.VISIBLE) {
                lowBalanceText.visibility = View.VISIBLE
                normalBalance.visibility = View.GONE
            }

            if (warning.visibility != View.VISIBLE) {
                warning.alpha = 0f
                warning.visibility = View.VISIBLE
                warning.animate().alpha(1f).setDuration(250).start()
            }
        } else {
            if (lowBalanceText.visibility == View.VISIBLE) {
                lowBalanceText.visibility = View.GONE
                normalBalance.visibility = View.VISIBLE
            }

            if (warning.visibility == View.VISIBLE) {
                warning.animate().alpha(0f).setDuration(200).withEndAction {
                    warning.visibility = View.GONE
                }.start()
            }
        }
        updateSliderState()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupSlideToPayButton() {
        val thumb = binding.ivSlideThumb
        val container = binding.slideToPayContainer

        thumb.setOnTouchListener { view, event ->
            if (selectedAmount <= 0 || selectedAmount > walletBalance) return@setOnTouchListener false

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    slideStartX = event.rawX - view.translationX
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val endMargin = resources.getDimension(R.dimen.size_8dp)
                    val maxSlide = container.width - view.width - endMargin
                    val newX = (event.rawX - slideStartX).coerceIn(0f, maxSlide)
                    view.translationX = newX

                    // Fade the label as user slides
                    val progress = newX / maxSlide
                    binding.tvSlideLabel.alpha = 1f - progress
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val endMargin = resources.getDimension(R.dimen.size_8dp)
                    val maxSlide = container.width - view.width - endMargin
                    val progress = view.translationX / maxSlide

                    if (progress > 0.8f) {
                        // Slide completed - trigger payment
                        view.animate().translationX(maxSlide).setDuration(100).start()
                        onSlideCompleted()
                    } else {
                        // Reset slide
                        view.animate().translationX(0f).setDuration(200).start()
                        binding.tvSlideLabel.animate().alpha(1f).setDuration(200).start()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun onSlideCompleted() {
        if (selectedAmount <= 0) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            resetSlider()
            return
        }

        val tagId = intent.getStringExtra(TAG_ID)
        if (tagId.isNullOrEmpty()) {
            Toast.makeText(this, "FASTag ID not found", Toast.LENGTH_SHORT).show()
            resetSlider()
            return
        }

        showSliderCompletedState()
        viewModel.rechargeFastag(tagId, selectedAmount)
    }

    private fun showSliderCompletedState() {
        binding.slideToPayContainer.setBackgroundResource(R.drawable.bg_slide_to_pay_completed)
        binding.ivSlideThumb.visibility = View.GONE
        binding.ivCheckIcon.visibility = View.VISIBLE
        binding.tvSlideLabel.alpha = 1f
        binding.tvSlideLabel.setTextColor(resources.getColor(android.R.color.white, theme))
    }

    private fun resetSlider() {
        binding.ivSlideThumb.translationX = 0f
        binding.ivSlideThumb.visibility = View.VISIBLE
        binding.ivCheckIcon.visibility = View.GONE
        binding.tvSlideLabel.alpha = 1f
        binding.ivSlideThumb.setImageResource(R.drawable.ic_chevron_right_double)
        updateSliderState()
    }

    private var currentBottomSheet: PaymentStatusBottomSheet? = null

    private fun showPaymentStatusBottomSheet(status: PaymentStatusBottomSheet.Status) {
        // Dismiss existing bottom sheet if any
        currentBottomSheet?.dismissAllowingStateLoss()

        currentBottomSheet = PaymentStatusBottomSheet.newInstance(status)
            .setOnDismissCallback {
                resetSlider()
                currentBottomSheet = null
            }
        currentBottomSheet?.show(supportFragmentManager, "payment_status")
    }

    private fun setupObservers() {
        viewModel.walletBalanceData.observe(this, androidx.lifecycle.Observer { balance ->
            walletBalance = balance
            binding.tvWalletBalance.text = "₹${StringUtils.formatAmount(balance)}"
            checkInsufficientBalanceImmediate()
        })

        viewModel.rechargeResponseData.observe(this, androidx.lifecycle.Observer { response ->
            response?.let {
                resetSlider()
                // Update wallet balance on screen
                it.updatedWalletBalance?.let { newBalance ->
                    binding.tvWalletBalance.text = "₹${StringUtils.formatAmount(newBalance)}"
                }
                // Update FASTag balance on screen
                it.fastagBalance?.let { newFastagBalance ->
                    binding.tvFastagBalance.text = "FASTag Balance: ₹$newFastagBalance"
                }

                val status = when (it.status?.lowercase()) {
                    "success" -> PaymentStatusBottomSheet.Status.SUCCESS
                    "pending", "processing" -> PaymentStatusBottomSheet.Status.PROCESSING
                    "failed", "failure" -> PaymentStatusBottomSheet.Status.FAILED
                    else -> PaymentStatusBottomSheet.Status.SUCCESS
                }
                showPaymentStatusBottomSheet(status)
            }
        })

        viewModel.fastagBlacklistedData.observe(this, androidx.lifecycle.Observer { isBlacklisted ->
            binding.layoutBlacklistWarning.visibility = if (isBlacklisted) View.VISIBLE else View.GONE
        })

        viewModel.exceptionLiveData.observe(this, androidx.lifecycle.Observer {
            it?.let { resetSlider() }
        })
    }
}
