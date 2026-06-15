package com.dfd.delfin.ui.fastag.issuance

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dfd.delfin.R
import com.dfd.delfin.databinding.ActivityBuyFasTagBinding
import com.dfd.delfin.ui.base.BaseActivity

class BuyFasTagActivity : BaseActivity<ActivityBuyFasTagBinding, SalesCodeViewModel>() {

    override fun getViewModelClass() = SalesCodeViewModel::class.java
    override fun layoutId() = R.layout.activity_buy_fas_tag
    override fun requireConnection() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding.lifecycleOwner = this

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupToolbar()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, SalesCodeFragment.newInstance())
                .commit()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Buy FASTag"
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    fun loadFastagIntent(
        context: Context
    ) = Intent(context, BuyFasTagActivity::class.java)
}
