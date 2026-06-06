package com.delhivery.axle.ui.fastag.issuance

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import com.delhivery.axle.R
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.databinding.FragmentSalesCodeBinding
import com.delhivery.axle.ui.home.fragments.HomeBaseFragment

class SalesCodeFragment : HomeBaseFragment<FragmentSalesCodeBinding, SalesCodeViewModel>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        setupClickListeners()
        observeViewModel()

        // Listen for "Change" from AgentConfirmationFragment
        parentFragmentManager.setFragmentResultListener("change_sales_code", viewLifecycleOwner) { _, _ ->
            hasNavigatedToAgent = false
            viewModel.resetValidateState()
            viewModel.salesCode = ""
            binding.salesCode = ""
            binding.etSalesCode.requestFocus()
            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.showSoftInput(binding.etSalesCode, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun initUI() {
        // Restore sales code from ViewModel (survives view recreation on back navigation)
        binding.salesCode = viewModel.salesCode
        val hasText = !binding.salesCode.isNullOrBlank()
        binding.bottomButtons.btnPrimary.isEnabled = hasText
        binding.bottomButtons.btnPrimary.setBackgroundResource(
            if (hasText) R.drawable.bg_all_round_corner_solid_black
            else R.drawable.bg_all_round_corner_light_grey
        )
        // Reset navigation guard and stale state so user can validate and navigate again
        hasNavigatedToAgent = false
        viewModel.resetValidateState()
    }

    override fun getViewModelClass() = SalesCodeViewModel::class.java

    override fun layoutId() = R.layout.fragment_sales_code

    private fun setupClickListeners() {
        binding.bottomButtons.btnPrimary.text = "Validate code"
        binding.bottomButtons.btnSecondary.text = "I don\u0027t have sales code"

        binding.bottomButtons.btnPrimary.setOnClickListener {
            val code = binding.salesCode?.trim() ?: return@setOnClickListener
            if (code.isNotEmpty()) {
                viewModel.validateSalesCode(code)
            }
        }

        binding.bottomButtons.btnSecondary.setOnClickListener {
            val bottomSheet = RequestReceivedBottomSheet.newInstance()
            bottomSheet.show(parentFragmentManager, RequestReceivedBottomSheet.TAG)
        }

        // Update primary button state based on sales code input and persist to ViewModel
        binding.etSalesCode.addTextChangedListener { text ->
            val hasText = !text.isNullOrBlank()
            binding.bottomButtons.btnPrimary.isEnabled = hasText
            binding.bottomButtons.btnPrimary.setBackgroundResource(
                if (hasText) R.drawable.bg_all_round_corner_solid_black
                else R.drawable.bg_all_round_corner_light_grey
            )
            viewModel.salesCode = text?.toString() ?: ""
        }
    }

    private var hasNavigatedToAgent = false

    private fun observeViewModel() {
        viewModel.validateState.observe(viewLifecycleOwner) { resource ->
            if (resource == null) return@observe
            when (resource) {
                is Resource.Loading -> {}

                is Resource.Success -> {
                    if (hasNavigatedToAgent) return@observe
                    val data = resource.data
                    if (data != null && data.isValid) {
                        hasNavigatedToAgent = true
                        navigateToAgentConfirmation(
                            agentName = data.agentName ?: "",
                            agentCode = data.agentCode ?: data.salesCode,
                        )
                    } else {
                        (requireActivity() as BuyFasTagActivity).dialogUtils.showErrorDialog(
                            data?.message ?: "Invalid sales code"
                        )
                    }
                }

                is Resource.Failure -> {
                    val message = resource.errorMessage
                        ?: if (resource.isNetworkError) "No internet connection" else "Something went wrong"
                    (requireActivity() as BuyFasTagActivity).dialogUtils.showErrorDialog(message)
                }
            }
        }
    }

    private fun navigateToAgentConfirmation(
        agentName: String,
        agentCode: String,
    ) {
        val fragment = AgentConfirmationFragment.newInstance(
            agentName = agentName,
            agentCode = agentCode,
        )
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    companion object {
        fun newInstance() = SalesCodeFragment()
    }
}
