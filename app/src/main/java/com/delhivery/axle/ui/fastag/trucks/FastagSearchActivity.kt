package com.delhivery.axle.ui.fastag.trucks

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.delhivery.axle.databinding.ActivityFastagSearchBinding
import com.delhivery.axle.utils.WindowInsetsUtils

/**
 * Full-screen search activity for finding FASTag trucks by vehicle number.
 * Receives a list of vehicle numbers and filters locally as the user types.
 */
class FastagSearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFastagSearchBinding
    private lateinit var searchAdapter: FastagSearchAdapter
    private var vehicleNumbers: List<String> = emptyList()

    companion object {
        const val EXTRA_VEHICLE_NUMBERS = "vehicle_numbers"
        const val RESULT_VEHICLE_NUMBER = "selected_vehicle_number"

        fun newIntent(context: Context, vehicleNumbers: ArrayList<String>): Intent {
            return Intent(context, FastagSearchActivity::class.java).apply {
                putStringArrayListExtra(EXTRA_VEHICLE_NUMBERS, vehicleNumbers)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFastagSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            WindowInsetsUtils.applyTopSystemWindowInsets(binding.parentLl)
        }

        vehicleNumbers = intent.getStringArrayListExtra(EXTRA_VEHICLE_NUMBERS) ?: emptyList()

        setupSearchAdapter()
        setupListeners()
        showKeyboard()
    }

    private fun setupSearchAdapter() {
        searchAdapter = FastagSearchAdapter { selectedVehicle ->
            val resultIntent = Intent().apply {
                putExtra(RESULT_VEHICLE_NUMBER, selectedVehicle)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        binding.rvSearchResults.apply {
            layoutManager = LinearLayoutManager(this@FastagSearchActivity)
            adapter = searchAdapter
        }
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener { finish() }

        binding.ivClear.setOnClickListener {
            binding.etSearch.text?.clear()
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                binding.ivClear.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
                filterResults(query)
            }
        })
    }

    private fun filterResults(query: String) {
        if (query.isEmpty()) {
            searchAdapter.submitResults(emptyList(), "")
            return
        }

        val filtered = vehicleNumbers.filter {
            it.uppercase().contains(query.uppercase())
        }
        searchAdapter.submitResults(filtered, query)
    }

    private fun showKeyboard() {
        binding.etSearch.requestFocus()
        binding.etSearch.postDelayed({
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT)
        }, 200)
    }
}
