package com.delhivery.axle.ui.fastag.issuance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.delhivery.axle.R
import com.delhivery.axle.api.repository.Resource
import com.delhivery.axle.databinding.FragmentSalesCodeBinding

class SalesCodeFragment : Fragment() {

    private var _binding: FragmentSalesCodeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SalesCodeViewModel by viewModels {
        (requireActivity() as BuyFasTagActivity).viewModelFactory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSalesCodeBinding.inflate(inflater, container, false)
        binding.salesCode = ""
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeViewModel()
    }

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

        // Update primary button state based on sales code input
        binding.etSalesCode.addTextChangedListener { text ->
            val hasText = !text.isNullOrBlank()
            binding.bottomButtons.btnPrimary.isEnabled = hasText
            binding.bottomButtons.btnPrimary.setBackgroundResource(
                if (hasText) R.drawable.bg_all_round_corner_solid_black
                else R.drawable.bg_all_round_corner_light_grey
            )
        }
    }

    private fun observeViewModel() {
        viewModel.validateState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {}

                is Resource.Success -> {
                    val data = resource.data
                    if (data != null && data.isValid) {
                        navigateToAgentConfirmation(
                            agentName = data.agentName ?: "",
                            agentCode = data.agentCode ?: data.salesCode,
                        )
                    } else {
                        Toast.makeText(
                            requireContext(),
                            data?.message ?: "Invalid sales code",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                is Resource.Failure -> {
                    val message = resource.errorMessage
                        ?: if (resource.isNetworkError) "No internet connection" else "Something went wrong"
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = SalesCodeFragment()
    }
}
