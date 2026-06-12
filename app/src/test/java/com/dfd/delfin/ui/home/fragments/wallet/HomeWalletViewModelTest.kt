package com.dfd.delfin.ui.home.fragments.wallet

import com.dfd.delfin.api.repository.WalletRepository
import com.dfd.delfin.api.response.WalletData
import com.dfd.delfin.api.response.WalletDataResponse
import com.dfd.delfin.testutils.ViewModelTestSetup
import com.dfd.delfin.testutils.relaxedMock
import com.dfd.delfin.testutils.strictMock
import com.dfd.delfin.utils.prefs.UserPrefs
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import io.reactivex.Single

/**
 * Unit tests for HomeWalletViewModel using Kotest and MockK.
 *
 * Covers:
 * - activateWallet() — success posts wallet data and sets walletActivated; error posts null
 * - fetchWalletData() — success posts wallet data and sets optinDate; error posts null
 * - walletActivated property — getter/setter delegates to UserPrefs
 */
class HomeWalletViewModelTest : FunSpec({

    lateinit var walletRepository: WalletRepository
    lateinit var userPrefs: UserPrefs
    lateinit var viewModel: HomeWalletViewModel

    beforeSpec {
        ViewModelTestSetup.setup()
    }

    afterSpec {
        ViewModelTestSetup.teardown()
    }

    beforeEach {
        walletRepository = strictMock()
        userPrefs = relaxedMock()
        viewModel = HomeWalletViewModel(walletRepository, userPrefs)
    }

    fun walletData(
        active: Boolean = true,
        balance: Double = 1000.0,
        walletId: String = "W001",
        optinDate: String? = "2024-01-01T10:00:00"
    ) = WalletData(
        active = active,
        autoWithdraw = false,
        balance = balance,
        walletId = walletId,
        ifsc = "HDFC0001234",
        accName = "Test Supplier",
        accNumber = "1234567890",
        accType = "savings",
        optinDate = optinDate
    )

    // ==================== activateWallet Tests ====================

    context("activateWallet") {

        test("success posts wallet data to walletLiveData") {
            val data = walletData()
            every { walletRepository.activateWallet() } returns Single.just(WalletDataResponse(data))

            viewModel.activateWallet()

            viewModel.walletLiveData.value shouldBe data
        }

        test("success sets walletActivated flag in userPrefs") {
            every { walletRepository.activateWallet() } returns Single.just(WalletDataResponse(walletData()))

            viewModel.activateWallet()

            verify { userPrefs.walletActivated = true }
        }

        test("success sets optinDate from wallet") {
            val data = walletData(optinDate = "2024-02-15T10:00:00")
            every { walletRepository.activateWallet() } returns Single.just(WalletDataResponse(data))

            viewModel.activateWallet()

            viewModel.optinDate shouldBe "2024-02-15T10:00:00"
        }

        test("null optinDate defaults to empty string") {
            val data = walletData(optinDate = null)
            every { walletRepository.activateWallet() } returns Single.just(WalletDataResponse(data))

            viewModel.activateWallet()

            viewModel.optinDate shouldBe ""
        }

        test("error posts null to walletLiveData") {
            every { walletRepository.activateWallet() } returns Single.error(RuntimeException("Activation failed"))

            viewModel.activateWallet()

            viewModel.walletLiveData.value.shouldBeNull()
        }

        test("error does not set walletActivated") {
            every { walletRepository.activateWallet() } returns Single.error(RuntimeException("Server error"))

            viewModel.activateWallet()

            verify(exactly = 0) { userPrefs.walletActivated = true }
        }
    }

    // ==================== fetchWalletData Tests ====================

    context("fetchWalletData") {

        test("success posts wallet data to walletLiveData") {
            val data = walletData()
            every { walletRepository.fetchWalletData() } returns Single.just(WalletDataResponse(data))

            viewModel.fetchWalletData()

            viewModel.walletLiveData.value shouldBe data
        }

        test("success sets optinDate from wallet") {
            val data = walletData(optinDate = "2024-03-10T08:00:00")
            every { walletRepository.fetchWalletData() } returns Single.just(WalletDataResponse(data))

            viewModel.fetchWalletData()

            viewModel.optinDate shouldBe "2024-03-10T08:00:00"
        }

        test("null optinDate on fetch defaults to empty string") {
            val data = walletData(optinDate = null)
            every { walletRepository.fetchWalletData() } returns Single.just(WalletDataResponse(data))

            viewModel.fetchWalletData()

            viewModel.optinDate shouldBe ""
        }

        test("error posts null to walletLiveData") {
            every { walletRepository.fetchWalletData() } returns Single.error(RuntimeException("Network error"))

            viewModel.fetchWalletData()

            viewModel.walletLiveData.value.shouldBeNull()
        }
    }

    // ==================== walletActivated property Tests ====================

    context("walletActivated property") {

        test("getter delegates to userPrefs") {
            every { userPrefs.walletActivated } returns true

            viewModel.walletActivated shouldBe true
        }

        test("getter returns false when userPrefs flag is false") {
            every { userPrefs.walletActivated } returns false

            viewModel.walletActivated shouldBe false
        }

        test("setter delegates to userPrefs") {
            viewModel.walletActivated = true

            verify { userPrefs.walletActivated = true }
        }

        test("setter with false delegates to userPrefs") {
            viewModel.walletActivated = false

            verify { userPrefs.walletActivated = false }
        }
    }
})
