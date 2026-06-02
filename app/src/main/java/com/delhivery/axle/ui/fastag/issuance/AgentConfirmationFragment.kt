package com.delhivery.axle.ui.fastag.issuance

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.delhivery.axle.databinding.FragmentAgentConfirmationBinding

class AgentConfirmationFragment : Fragment() {

    private var _binding: FragmentAgentConfirmationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAgentConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { args ->
            binding.agentName = args.getString(ARG_AGENT_NAME, "")
            binding.agentCode = args.getString(ARG_AGENT_CODE, "")
        }

        binding.bottomButtons.btnPrimary.text = "Confirm"
        binding.bottomButtons.btnSecondary.text = "Change"

        binding.bottomButtons.btnPrimary.setOnClickListener {
            val intent = Intent(requireContext(), AddVehicleActivity::class.java).apply {
                putExtra(AddVehicleActivity.EXTRA_SALES_CODE, arguments?.getString(ARG_AGENT_CODE, "") ?: "")
            }
            startActivity(intent)
        }

        binding.bottomButtons.btnSecondary.setOnClickListener {
            parentFragmentManager.setFragmentResult("change_sales_code", Bundle.EMPTY)
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
