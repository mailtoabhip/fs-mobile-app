package com.dfd.delfin.ui.fastag.issuance

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.dfd.delfin.R
import com.dfd.delfin.databinding.FragmentAgentConfirmationBinding
import com.dfd.delfin.ui.home.fragments.HomeBaseFragment

class AgentConfirmationFragment : HomeBaseFragment<FragmentAgentConfirmationBinding, SalesCodeViewModel>() {

    override fun getViewModelClass() = SalesCodeViewModel::class.java

    override fun layoutId() = R.layout.fragment_agent_confirmation

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        arguments?.let { args ->
            binding.agentName = args.getString(ARG_AGENT_NAME, "")
            binding.agentCode = args.getString(ARG_AGENT_CODE, "")
        }

        initUI()

        binding.bottomButtons.btnPrimary.text = "Confirm"
        binding.bottomButtons.btnSecondary.text = "Change"

        binding.bottomButtons.btnPrimary.setOnClickListener {
            val intent = Intent(requireContext(), AddVehicleActivity::class.java).apply {
                putExtra(AddVehicleActivity.EXTRA_SALES_CODE, arguments?.getString(ARG_AGENT_CODE, "") ?: "")
                putExtra(AddVehicleActivity.EXTRA_CUSTOMER_NAME, arguments?.getString(ARG_AGENT_NAME, "") ?: "")
            }
            startActivity(intent)
        }

        binding.bottomButtons.btnSecondary.setOnClickListener {
            parentFragmentManager.setFragmentResult("change_sales_code", Bundle.EMPTY)
            parentFragmentManager.popBackStackImmediate()
        }
    }

    private fun initUI() {
        
    }

    companion object {
        private const val ARG_AGENT_NAME = "arg_agent_name"
        private const val ARG_AGENT_CODE = "arg_agent_code"

        fun newInstance(
            agentName: String,
            agentCode: String,
        ): AgentConfirmationFragment {
            return AgentConfirmationFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_AGENT_NAME, agentName)
                    putString(ARG_AGENT_CODE, agentCode)
                }
            }
        }
    }
}
