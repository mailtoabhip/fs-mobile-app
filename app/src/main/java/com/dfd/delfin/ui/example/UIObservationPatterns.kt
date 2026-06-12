package com.dfd.delfin.ui.example

/**
 * UI OBSERVATION PATTERNS FOR RESOURCE-BASED API CALLS
 *
 * This file documents the recommended patterns for observing Resource LiveData
 * in Activities and Fragments. Use these patterns as a reference when migrating
 * from RxJava to coroutines.
 *
 * ============================================================================
 * PATTERN 1: Basic Resource Observation with When Expression
 * ============================================================================
 *
 * ```kotlin
 * class ExampleActivity : AppCompatActivity() {
 *
 *     @Inject
 *     lateinit var viewModel: ExampleViewModel
 *
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *         setContentView(R.layout.activity_example)
 *
 *         // Observe Resource state with exhaustive when expression
 *         viewModel.userDataState.observe(this) { resource ->
 *             when (resource) {
 *                 is Resource.Loading -> {
 *                     // Show loading indicator
 *                     progressBar.visibility = View.VISIBLE
 *                     contentLayout.visibility = View.GONE
 *                 }
 *                 is Resource.Success -> {
 *                     // Hide loading and handle success
 *                     progressBar.visibility = View.GONE
 *                     contentLayout.visibility = View.VISIBLE
 *                     
 *                     // Data is nullable
 *                     resource.data?.let { userData ->
 *                         textViewName.text = userData.name
 *                         textViewId.text = userData.id
 *                     }
 *                 }
 *                 is Resource.Failure -> {
 *                     // Hide loading and handle failure
 *                     progressBar.visibility = View.GONE
 *                     contentLayout.visibility = View.GONE
 *                     handleError(resource.apiError, resource.isNetworkError)
 *                 }
 *             }
 *         }
 *
 *         // Trigger API call
 *         viewModel.fetchUserData()
 *     }
 *
 *     private fun handleError(apiError: ApiError, isNetworkError: Boolean) {
 *         val message = when (apiError) {
 *             ApiError.Timeout -> getString(R.string.error_timeout)
 *             ApiError.Network -> getString(R.string.error_network)
 *             ApiError.Unauthorized -> {
 *                 // Navigate to login
 *                 navigateToLogin()
 *                 getString(R.string.error_unauthorized)
 *             }
 *             ApiError.AccessDenied -> getString(R.string.error_access_denied)
 *             ApiError.NotFound -> getString(R.string.error_not_found)
 *             ApiError.ServiceUnavailable -> getString(R.string.error_service_unavailable)
 *             ApiError.Unknown -> getString(R.string.error_unknown)
 *         }
 *         showErrorDialog(message)
 *     }
 * }
 * ```
 *
 * ============================================================================
 * PATTERN 2: Using Extension Functions for Cleaner Code
 * ============================================================================
 *
 * ```kotlin
 * class ExampleFragment : Fragment() {
 *
 *     @Inject
 *     lateinit var viewModel: ExampleViewModel
 *
 *     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
 *         super.onViewCreated(view, savedInstanceState)
 *
 *         // Observe Resource state with when expression
 *         viewModel.combinedDataState.observe(viewLifecycleOwner) { resource ->
 *             when (resource) {
 *                 is Resource.Loading -> showLoading()
 *                 is Resource.Success -> {
 *                     hideLoading()
 *                     resource.data?.let {
 *                         updateUserSection(it.user)
 *                         updateWalletSection(it.wallet)
 *                     }
 *                 }
 *                 is Resource.Failure -> {
 *                     hideLoading()
 *                     if (resource.isNetworkError) {
 *                         showNetworkErrorWithRetry()
 *                     } else {
 *                         showError(resource.apiError.toErrorMessage(requireContext()))
 *                     }
 *                 }
 *             }
 *         }
 *
 *         viewModel.fetchCombinedData()
 *     }
 * }
 * ```
 *
 * ============================================================================
 * PATTERN 3: Fragment with Lifecycle-Aware Observation
 * ============================================================================
 *
 * ```kotlin
 * class HomeFragment : Fragment() {
 *
 *     @Inject
 *     lateinit var viewModel: HomeViewModel
 *
 *     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
 *         super.onViewCreated(view, savedInstanceState)
 *
 *         // IMPORTANT: Use viewLifecycleOwner for Fragments, not 'this'
 *         // This prevents memory leaks when Fragment view is destroyed
 *         viewModel.dataState.observe(viewLifecycleOwner) { resource ->
 *             when (resource) {
 *                 is Resource.Loading -> showLoadingState()
 *                 is Resource.Success -> {
 *                     hideLoadingState()
 *                     handleSuccess(resource.data)
 *                 }
 *                 is Resource.Failure -> {
 *                     hideLoadingState()
 *                     handleFailure(resource)
 *                 }
 *             }
 *         }
 *     }
 *
 *     private fun handleFailure(failure: Resource.Failure) {
 *         // Check if it's a network error for special handling
 *         if (failure.isNetworkError) {
 *             showNetworkErrorSnackbar()
 *             return
 *         }
 *
 *         // Handle specific HTTP errors
 *         when (failure.errorCode) {
 *             401 -> navigateToLogin()
 *             403 -> showAccessDeniedDialog()
 *             else -> showGenericError(failure.apiError.toErrorMessage(requireContext()))
 *         }
 *     }
 * }
 * ```
 *
 * ============================================================================
 * PATTERN 4: Retry Logic with Resource
 * ============================================================================
 *
 * ```kotlin
 * class LoadsFragment : Fragment() {
 *
 *     @Inject
 *     lateinit var viewModel: LoadsViewModel
 *
 *     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
 *         super.onViewCreated(view, savedInstanceState)
 *
 *         viewModel.loadsState.observe(viewLifecycleOwner) { resource ->
 *             when (resource) {
 *                 is Resource.Loading -> {
 *                     hideErrorView()
 *                     showLoadingView()
 *                 }
 *                 is Resource.Success -> {
 *                     hideLoadingView()
 *                     hideErrorView()
 *                     showLoadsList(resource.data)
 *                 }
 *                 is Resource.Failure -> {
 *                     hideLoadingView()
 *                     hideLoadsList()
 *                     showErrorViewWithRetry(
 *                         message = resource.apiError.toErrorMessage(requireContext()),
 *                         onRetry = { viewModel.fetchLoads() }
 *                     )
 *                 }
 *             }
 *         }
 *
 *         // Initial load
 *         viewModel.fetchLoads()
 *     }
 *
 *     private fun showErrorViewWithRetry(message: String, onRetry: () -> Unit) {
 *         errorView.visibility = View.VISIBLE
 *         errorTextView.text = message
 *         retryButton.setOnClickListener { onRetry() }
 *     }
 * }
 * ```
 *
 * ============================================================================
 * PATTERN 5: Pull-to-Refresh with Resource
 * ============================================================================
 *
 * ```kotlin
 * class DashboardFragment : Fragment() {
 *
 *     @Inject
 *     lateinit var viewModel: DashboardViewModel
 *
 *     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
 *         super.onViewCreated(view, savedInstanceState)
 *
 *         // Setup SwipeRefreshLayout
 *         swipeRefreshLayout.setOnRefreshListener {
 *             viewModel.refreshDashboard()
 *         }
 *
 *         // Observe data state
 *         viewModel.dashboardState.observe(viewLifecycleOwner) { resource ->
 *             when (resource) {
 *                 is Resource.Loading -> {
 *                     // Show loading in SwipeRefreshLayout
 *                     swipeRefreshLayout.isRefreshing = true
 *                 }
 *                 is Resource.Success -> {
 *                     // Hide loading
 *                     swipeRefreshLayout.isRefreshing = false
 *                     
 *                     // Update UI with new data
 *                     resource.data?.let { dashboard ->
 *                         updateDashboard(dashboard)
 *                     }
 *                 }
 *                 is Resource.Failure -> {
 *                     // Hide loading
 *                     swipeRefreshLayout.isRefreshing = false
 *                     
 *                     // Show error snackbar
 *                     showErrorSnackbar(resource.apiError.toErrorMessage(requireContext()))
 *                 }
 *             }
 *         }
 *
 *         // Initial load
 *         viewModel.loadDashboard()
 *     }
 * }
 * ```
 *
 * ============================================================================
 * KEY TAKEAWAYS
 * ============================================================================
 *
 * 1. Always use exhaustive when expressions for Resource handling (Loading, Success, Failure)
 * 2. Use viewLifecycleOwner in Fragments to prevent memory leaks
 * 3. Resource.Loading replaces separate isLoading LiveData
 * 4. Map ApiError to user-friendly messages using extension functions
 * 5. Consider network errors separately for retry logic
 * 6. Check errorCode for specific HTTP status handling
 * 7. Always handle nullable data in Resource.Success
 * 8. Emit Resource.Loading before every API call in ViewModel
 */
