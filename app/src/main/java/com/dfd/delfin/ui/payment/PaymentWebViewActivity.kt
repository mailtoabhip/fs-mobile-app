package com.dfd.delfin.ui.payment

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import com.dfd.delfin.BuildConfig
import com.dfd.delfin.R
import com.dfd.delfin.databinding.ActivityPaymentWebviewBinding
import com.dfd.delfin.ui.base.BaseActivity
import com.dfd.delfin.utils.WindowInsetsUtils
import androidx.core.graphics.toColorInt
/**
 * Activity to display payment URL in a WebView
 */
class PaymentWebViewActivity : BaseActivity<ActivityPaymentWebviewBinding, PaymentWebViewViewModel>() {

    companion object {
        const val EXTRA_PAYMENT_URL = "payment_url"
        const val EXTRA_TITLE = "title"
        const val EXTRA_REDIRECT_URL = "redirect_url"
        
        // Result constants
        const val RESULT_SUCCESS = RESULT_OK
        const val RESULT_CANCELLED = RESULT_CANCELED
        const val EXTRA_RESULT_MESSAGE = "result_message"

        /**
         * Create intent to open payment URL in WebView
         */
        fun createIntent(
            context: android.content.Context,
            paymentUrl: String,
            redirectUrl: String? = null,
            title: String = "Payment"
        ): Intent {
            return Intent(context, PaymentWebViewActivity::class.java).apply {
                putExtra(EXTRA_PAYMENT_URL, paymentUrl)
                putExtra(EXTRA_TITLE, title)
                redirectUrl?.let { putExtra(EXTRA_REDIRECT_URL, it) }
            }
        }
    }

    private var redirectUrl: String? = null
    private val handler = Handler(Looper.getMainLooper())
    private var pendingJsInjection: Runnable? = null

    init {
        StatusBarColor = "#ffffff".toColorInt()
    }

    override fun getViewModelClass() = PaymentWebViewViewModel::class.java

    override fun layoutId() = R.layout.activity_payment_webview

    override fun requireConnection() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get payment URL and redirect URL from intent
        val paymentUrl = intent.getStringExtra(EXTRA_PAYMENT_URL)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Payment"
        redirectUrl = intent.getStringExtra(EXTRA_REDIRECT_URL)

        if (paymentUrl.isNullOrEmpty()) {
            uiUtils.showSnackbar("Invalid payment URL")
            finish()
            return
        }

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
        }
        this.title = title
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        // Setup close button
        binding.closeButton.setOnClickListener {
            handleExitConfirmation()
        }

        // Handle back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.webView.canGoBack()) {
                    binding.webView.goBack()
                } else {
                    handleExitConfirmation()
                }
            }
        })

        // Setup WebView
        setupWebView(paymentUrl)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        if (WindowInsetsUtils.isEdgeToEdgeEnforced()) {
            WindowInsetsUtils.applyTopSystemWindowInsets(binding.toolbar)
        }
        title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    /**
     * Setup WebView with payment URL
     */
    private fun setupWebView(url: String) {
        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = false
            displayZoomControls = false
            setSupportZoom(false)
            allowFileAccess = true
            allowContentAccess = true
            javaScriptCanOpenWindowsAutomatically = true
            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            // Enable caching for better performance
            cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
            // Set Chrome user agent so payment gateways (e.g. Razorpay) show UPI options
            userAgentString = "Mozilla/5.0 (Linux; Android ${android.os.Build.VERSION.RELEASE}; ${android.os.Build.MODEL}) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
        }
        
        // Enable vertical scrolling to see all options including "Enter UPI ID"
        binding.webView.isVerticalScrollBarEnabled = true
        binding.webView.isHorizontalScrollBarEnabled = false
        binding.webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        
        // Add JavaScript interface to handle exit button clicks
        if (BuildConfig.DEBUG) {
            Log.d("PaymentWebView", "Adding JavaScript interface: AndroidInterface")
        }
        binding.webView.addJavascriptInterface(ExitButtonInterface(), "AndroidInterface")
        
        // Enable WebView debugging
        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {

                val currentUrl = request?.url?.toString() ?: return false

                Log.d("PaymentWebView", "shouldOverrideUrlLoading: $currentUrl")

                // Exit button callback
                if (currentUrl == "android-app://exit-payment") {
                    runOnUiThread {
                        handleExitConfirmation()
                    }
                    return true
                }

                // Payment success redirect
                if (checkRedirectUrl(currentUrl)) {
                    return true
                }

                Log.d("FS_DEBUG::PAYMENT_1", "Received URL: $currentUrl")

                // UPI / payment apps
                if (isCustomUrlSchemeNew(currentUrl)) {

                    Log.d(
                        "PaymentWebView",
                        "Custom payment scheme detected: $currentUrl"
                    )

                    //manage custom uri
                    openCustomUrlSchemeNew(currentUrl)

                    // Always consume custom schemes
                    return true

//                    val launched = openCustomUrlSchemeNew(currentUrl)
//
//                    return if (launched) {
//                        // App opened successfully
//                        true
//                    } else {
//                        // Allow Razorpay/WebView to continue with fallback logic
//                        false
//                    }
                }

                // Let WebView handle HTTP/HTTPS URLs
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.progressBar.visibility = View.GONE

                if (BuildConfig.DEBUG) {
                    android.util.Log.d("PaymentWebView", "onPageFinished: $url")
                }
                
                // Inject JavaScript to listen for exit button clicks
                // Use postDelayed to ensure DOM is fully loaded
                // Cancel any pending injection first
                pendingJsInjection?.let { handler.removeCallbacks(it) }
                pendingJsInjection = Runnable {
                    if (!isFinishing && !isDestroyed && view != null) {
                        if (BuildConfig.DEBUG) {
                            android.util.Log.d("PaymentWebView", "Injecting exit button listener JavaScript")
                        }
                        injectExitButtonListener(view)
                    }
                }
                handler.postDelayed(pendingJsInjection!!, 500)
                
                // Also check redirect URL when page finishes loading
                url?.let { currentUrl ->
                    if (checkRedirectUrl(currentUrl)) {
                        return
                    }
                }
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                binding.progressBar.visibility = View.GONE
                Log.i("Failed to load page", error?.description.toString())
                //uiUtils.showSnackbar("Failed to load page: ${error?.description}")
            }
            
            /**
             * Check if the current URL matches the redirect URL exactly and close WebView if it does
             * Only matches the exact base URL (ignoring query params and fragments)
             */
            private fun checkRedirectUrl(currentUrl: String): Boolean {
                redirectUrl?.let { redirect ->
                    try {
                        val currentUri = Uri.parse(currentUrl)
                        val redirectUri = Uri.parse(redirect)
                        
                        // Extract base URL (scheme + host + path only, no query/fragment)
                        val currentBase = "${currentUri.scheme}://${currentUri.host}${currentUri.path ?: "/"}"
                        val redirectBase = "${redirectUri.scheme}://${redirectUri.host}${redirectUri.path ?: "/"}"
                        
                        // Normalize (remove trailing slashes and convert to lowercase)
                        val normalizedCurrent = currentBase.trimEnd('/').lowercase()
                        val normalizedRedirect = redirectBase.trimEnd('/').lowercase()
                        
                        // Exact match only - this prevents false positives from asset URLs
                        if (normalizedCurrent.contains(normalizedRedirect)) {
                            // Redirect URL detected - set result and close the WebView
                            val resultIntent = Intent().apply {
                                putExtra(EXTRA_RESULT_MESSAGE, "Payment completed successfully")
                            }
                            setResult(RESULT_SUCCESS, resultIntent)
                            uiUtils.showSnackbar("Payment completed")
                            finish()
                            return true
                        }
                    } catch (e: Exception) {
                        // Invalid URL format
                        return false
                    }
                }
                return false
            }
        }

        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                binding.progressBar.progress = newProgress
                if (newProgress == 100) {
                    binding.progressBar.visibility = View.GONE
                }
            }
            
            /**
             * Handle JavaScript confirm dialogs (like "Are you sure you want to exit?")
             */
            override fun onJsConfirm(
                view: WebView?,
                url: String?,
                message: String?,
                result: android.webkit.JsResult?
            ): Boolean {

                Log.d("onJsConfirm",""+message?.lowercase())

                // Check if this is the exit confirmation dialog
                message?.let { msg ->
                    val lowerMessage = msg.lowercase()
                    
                    if (lowerMessage.contains("exit") ||
                        lowerMessage.contains("cancel") ||
                        lowerMessage.contains("sure") || 
                        lowerMessage.contains("taken back") ||
                        lowerMessage.contains("want to exit")) {
                        // Show a native dialog to handle the exit confirmation
                        android.app.AlertDialog.Builder(this@PaymentWebViewActivity)
                            .setTitle("Exit Payment?")
                            .setMessage(message)
                            .setPositiveButton("Yes, exit") { _, _ ->
                                result?.confirm()
                                handleExitConfirmation()
                            }
                            .setNegativeButton("Continue to payment") { _, _ ->
                                result?.cancel()
                            }
                            .setOnCancelListener {
                                result?.cancel()
                            }
                            .show()
                        return true // We handled the dialog
                    }
                }
                // For other confirm dialogs, use default behavior
                return super.onJsConfirm(view, url, message, result)
            }
            
            /**
             * Handle JavaScript alert dialogs
             */
            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: android.webkit.JsResult?
            ): Boolean {
                // Show native alert for better UX
                message?.let { msg ->
                    android.app.AlertDialog.Builder(this@PaymentWebViewActivity)
                        .setMessage(msg)
                        .setPositiveButton("OK") { _, _ ->
                            result?.confirm()
                        }
                        .setOnCancelListener {
                            result?.cancel()
                        }
                        .show()
                    return true
                }
                return super.onJsAlert(view, url, message, result)
            }
        }

        // Load the payment URL
        if (BuildConfig.DEBUG) {
            android.util.Log.d("PaymentWebView", "Loading URL: $url")
        }
        binding.webView.loadUrl(url)
    }
    
    /**
     * Inject JavaScript to listen for "Yes, exit" button clicks in custom HTML dialogs
     */
    private fun injectExitButtonListener(view: WebView?) {
        val jsCode = """
            (function() {
                function triggerExit() {
                    try {
                        if (typeof AndroidInterface !== 'undefined' && typeof AndroidInterface.onExitClicked === 'function') {
                            AndroidInterface.onExitClicked();
                        } else {
                            window.location.href = 'android-app://exit-payment';
                        }
                    } catch(e) {
                        try {
                            window.location.href = 'android-app://exit-payment';
                        } catch(e2) {
                            // Fallback failed
                        }
                    }
                }
                
                // Global click interceptor - catch ALL clicks at capture phase
                document.addEventListener('click', function(e) {
                    const target = e.target || e.srcElement;
                    if (!target) return;
                    
                    // Get text from target and all parents
                    let text = (target.textContent || target.innerText || target.value || '').trim().toLowerCase();
                    let parent = target.parentElement;
                    for (let i = 0; i < 3 && parent; i++) {
                        const parentText = (parent.textContent || parent.innerText || '').trim().toLowerCase();
                        if (parentText.length > text.length) {
                            text = parentText;
                        }
                        parent = parent.parentElement;
                    }
                    
                    // Check for exit patterns
                    if (text.includes('yes, exit') || 
                        (text.includes('exit') && text.includes('yes')) ||
                        text === 'yes, exit' ||
                        text.includes('exit payment') ||
                        (text.includes('exit') && text.includes('sure')) ||
                        (text.includes('exit') && text.includes('taken back'))) {
                        e.preventDefault();
                        e.stopPropagation();
                        e.stopImmediatePropagation();
                        triggerExit();
                        return false;
                    }
                }, true);
                
                // Check for iframes and try to communicate via postMessage
                function setupIframeListeners() {
                    const iframes = document.querySelectorAll('iframe');
                    iframes.forEach(function(iframe) {
                        try {
                            iframe.contentWindow.postMessage({
                                type: 'EXIT_LISTENER_REQUEST',
                                source: 'PaymentWebView'
                            }, '*');
                        } catch(e) {
                            // Cannot access iframe (cross-origin)
                        }
                    });
                }
                
                // Listen for messages from iframes - Razorpay sends checkout.close events
                window.addEventListener('message', function(event) {
                    try {
                        // Parse the data - it might be a string or an object
                        let data = event.data;
                        if (typeof data === 'string') {
                            try {
                                data = JSON.parse(data);
                            } catch(e) {
                                return;
                            }
                        }
                        
                        if (!data) return;
                        
                        // Check for Razorpay checkout.close event
                        // ONLY trigger exit on checkout.close, NOT on other events like payment.initiate
                        if (data.event === 'merchantevent' && 
                            data.data && 
                            data.data.event === 'checkout.close') {
                            triggerExit();
                            return;
                        }
                        
                        // Check for other exit-related events
                        if (data.type === 'EXIT_CONFIRMED' || 
                            data.exit === true ||
                            (data.event && typeof data.event === 'string' && data.event.includes('close'))) {
                            triggerExit();
                            return;
                        }
                    } catch(e) {
                        // Error processing message
                    }
                });
                
                // Setup iframe listeners after a delay
                setTimeout(setupIframeListeners, 2000);
                setInterval(setupIframeListeners, 5000);
                
                // Also intercept touch events
                document.addEventListener('touchend', function(e) {
                    const target = e.target || e.srcElement;
                    if (!target) return;
                    
                    const text = (target.textContent || target.innerText || target.value || '').trim().toLowerCase();
                    if (text.includes('yes, exit') || 
                        (text.includes('exit') && text.includes('yes')) ||
                        text.includes('exit payment')) {
                        e.preventDefault();
                        e.stopPropagation();
                        e.stopImmediatePropagation();
                        triggerExit();
                        return false;
                    }
                }, true);
                
                // MutationObserver for dynamically added elements
                const observer = new MutationObserver(function(mutations) {
                    mutations.forEach(function(mutation) {
                        mutation.addedNodes.forEach(function(node) {
                            if (node.nodeType === 1) {
                                const clickables = node.querySelectorAll ? 
                                    node.querySelectorAll('button, a, div, span, [role="button"], [onclick], [class*="button"], [class*="btn"]') : [];
                                clickables.forEach(function(el) {
                                    const text = (el.textContent || el.innerText || '').trim().toLowerCase();
                                    if (text.includes('yes, exit') || 
                                        (text.includes('exit') && text.includes('yes')) ||
                                        text.includes('exit payment')) {
                                        el.onclick = function(e) {
                                            e.preventDefault();
                                            e.stopPropagation();
                                            e.stopImmediatePropagation();
                                            triggerExit();
                                            return false;
                                        };
                                        el.addEventListener('click', function(e) {
                                            e.preventDefault();
                                            e.stopPropagation();
                                            e.stopImmediatePropagation();
                                            triggerExit();
                                            return false;
                                        }, true);
                                    }
                                });
                                
                                // Check node itself
                                const nodeText = (node.textContent || node.innerText || '').trim().toLowerCase();
                                if (nodeText.includes('yes, exit') || 
                                    (nodeText.includes('exit') && nodeText.includes('yes')) ||
                                    nodeText.includes('exit payment')) {
                                    node.onclick = function(e) {
                                        e.preventDefault();
                                        e.stopPropagation();
                                        e.stopImmediatePropagation();
                                        triggerExit();
                                        return false;
                                    };
                                }
                            }
                        });
                    });
                });
                
                if (document.body) {
                    observer.observe(document.body, { childList: true, subtree: true });
                }
                
                // Periodic check for exit dialog
                setInterval(function() {
                    try {
                        const dialogSelectors = [
                            '[role="dialog"]',
                            '.modal',
                            '[class*="modal"]',
                            '[class*="dialog"]',
                            '[class*="overlay"]',
                            '[class*="popup"]',
                            '[class*="confirm"]'
                        ];
                        
                        let dialogs = [];
                        dialogSelectors.forEach(function(selector) {
                            try {
                                const found = document.querySelectorAll(selector);
                                found.forEach(function(d) {
                                    if (dialogs.indexOf(d) === -1) {
                                        dialogs.push(d);
                                    }
                                });
                            } catch(e) {}
                        });
                        
                        dialogs.forEach(function(dialog) {
                            const dialogText = (dialog.textContent || dialog.innerText || '').trim().toLowerCase();
                            if (dialogText.includes('exit') || 
                                dialogText.includes('sure') || 
                                dialogText.includes('taken back') ||
                                dialogText.includes('want to exit')) {
                                
                                const clickables = dialog.querySelectorAll('button, [role="button"], [class*="button"], [class*="btn"], a, div[onclick], span[onclick], [tabindex="0"]');
                                
                                clickables.forEach(function(btn) {
                                    const btnText = (btn.textContent || btn.innerText || '').trim().toLowerCase();
                                    
                                    if (btnText.includes('yes, exit') || 
                                        (btnText.includes('exit') && btnText.includes('yes')) ||
                                        btnText === 'yes, exit' ||
                                        btnText.includes('exit payment') ||
                                        (btnText.includes('exit') && (btnText.includes('sure') || btnText.includes('confirm')))) {
                                        
                                        if (!btn.hasAttribute('data-exit-handled')) {
                                            btn.setAttribute('data-exit-handled', 'true');
                                            
                                            btn.onclick = function(e) {
                                                e.preventDefault();
                                                e.stopPropagation();
                                                e.stopImmediatePropagation();
                                                triggerExit();
                                                return false;
                                            };
                                            
                                            ['click', 'touchend', 'touchstart', 'mousedown', 'pointerdown'].forEach(function(eventType) {
                                                btn.addEventListener(eventType, function(e) {
                                                    e.preventDefault();
                                                    e.stopPropagation();
                                                    e.stopImmediatePropagation();
                                                    triggerExit();
                                                    return false;
                                                }, true);
                                            });
                                        }
                                    }
                                });
                            }
                        });
                        
                        // Check all elements for exit buttons
                        document.querySelectorAll('*').forEach(function(el) {
                            const text = (el.textContent || el.innerText || '').trim().toLowerCase();
                            if ((text.includes('yes, exit') || 
                                 (text.includes('exit') && text.includes('yes')) ||
                                 text.includes('exit payment')) && 
                                !el.hasAttribute('data-exit-handled')) {
                                el.setAttribute('data-exit-handled', 'true');
                                
                                el.onclick = function(e) {
                                    e.preventDefault();
                                    e.stopPropagation();
                                    e.stopImmediatePropagation();
                                    triggerExit();
                                    return false;
                                };
                                
                                ['click', 'touchend', 'touchstart'].forEach(function(eventType) {
                                    el.addEventListener(eventType, function(e) {
                                        e.preventDefault();
                                        e.stopPropagation();
                                        e.stopImmediatePropagation();
                                        triggerExit();
                                        return false;
                                    }, true);
                                });
                            }
                        });
                    } catch(e) {
                        // Error in periodic check
                    }
                }, 100);
                
                // Try to intercept Razorpay's exit handler if it exists
                if (window.razorpay && window.razorpay.on) {
                    try {
                        window.razorpay.on('exit', function() {
                            triggerExit();
                        });
                    } catch(e) {
                        // Could not hook Razorpay exit event
                    }
                }
            })();
        """.trimIndent()
        
        // Check if WebView and activity are still valid before injecting JavaScript
        if (view != null && !isFinishing && !isDestroyed) {
            try {
                view.evaluateJavascript(jsCode, null)
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    android.util.Log.e("PaymentWebView", "Error injecting JavaScript: ${e.message}", e)
                }
            }
        }
    }

    /**
     * Check if URL is a custom scheme that should be opened externally
     * WebView cannot handle custom schemes (like gpay://, phonepe://), so we intercept them
     * and open with Intent instead of letting WebView fail with ERR_UNKNOWN_URL_SCHEME
     */
    private fun isCustomUrlSchemeNew(url: String): Boolean {
        return url.startsWith("upi://", true) ||
                url.startsWith("gpay://", true) ||
                url.startsWith("phonepe://", true) ||
                url.startsWith("paytmmp://", true) ||
                url.startsWith("amazonpay://", true) ||
                url.startsWith("bhim://", true) ||
                url.startsWith("credpay://", true) ||
                url.startsWith("super://", true) ||
                url.startsWith("navipay://", true) ||
                url.startsWith("intent://", true)
    }
    
    /**
     * Inner class to handle JavaScript interface for exit button clicks
     */
    private inner class ExitButtonInterface {
        @android.webkit.JavascriptInterface
        fun onExitClicked() {
            runOnUiThread {
                handleExitConfirmation()
            }
        }
    }
    
    /**
     * Handle exit confirmation - close activity immediately
     */
    private fun handleExitConfirmation() {
        try {
            uiUtils.showSnackbar("Payment cancelled")
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.e("PaymentWebView", "Error showing snackbar: ${e.message}", e)
            }
        }
        
        try {
            val resultIntent = Intent().apply {
                putExtra(EXTRA_RESULT_MESSAGE, "Payment cancelled by user")
            }
            setResult(RESULT_CANCELLED, resultIntent)
            finish()
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.e("PaymentWebView", "Error calling finish(): ${e.message}", e)
            }
        }
    }

    /**
     * Open custom URL scheme with external app
     * Note: In Razorpay test mode, UPI apps will redirect to Razorpay mock page instead of real apps
     */
    private fun openCustomUrlSchemeNew(url: String): Boolean {

        return try {
            Log.d("PaymentWebView", "Launching payment URL: $url")
            //
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            //
            startActivity(intent)
            //
            true
        } catch (e: ActivityNotFoundException) {
            Log.d("PaymentWebView", "No app found for scheme: ${Uri.parse(url).scheme}")
            //
            uiUtils.showSnackbar("App is not available. Please select another app.")
            // Dismiss the "Processing Payment" overlay by navigating back in the WebView
            // This returns the user to the payment method selection screen
            dismissProcessingOverlay()
            //
            true
        } catch (e: Exception) {
            Log.d("PaymentWebView", "Failed to launch payment app", e)
            //
            uiUtils.showSnackbar("There is an issue with selected upi app. Please select another app.")
            // Dismiss the "Processing Payment" overlay for any launch failure
            dismissProcessingOverlay()
            //
            true
        }
    }

    /**
     * Dismiss the "Processing Payment" overlay shown by Razorpay when a UPI app
     * cannot be launched. This injects JavaScript to close the overlay/modal,
     * falling back to WebView goBack() if the JS approach doesn't work.
     */
    private fun dismissProcessingOverlay() {
        handler.postDelayed({
            if (isFinishing || isDestroyed) return@postDelayed

            // Inject JS to dismiss the Razorpay processing overlay and return to payment options
            val jsCode = """
                (function() {
                    // Try to close Razorpay processing overlay via known selectors
                    var overlays = document.querySelectorAll(
                        '[class*="processing"], [class*="overlay"], [class*="loader"], [class*="spinner"], [class*="pending"]'
                    );
                    var dismissed = false;
                    overlays.forEach(function(el) {
                        var text = (el.textContent || '').toLowerCase();
                        if (text.includes('processing') || text.includes('waiting') || text.includes('pending')) {
                            el.style.display = 'none';
                            el.remove();
                            dismissed = true;
                        }
                    });
                    
                    // Try clicking any visible back/cancel button within the payment UI
                    var backButtons = document.querySelectorAll(
                        '[class*="back"], [class*="cancel"], [class*="close"], [aria-label*="back"], [aria-label*="close"]'
                    );
                    backButtons.forEach(function(btn) {
                        if (btn.offsetParent !== null) {
                            btn.click();
                            dismissed = true;
                        }
                    });
                    
                    // If nothing was dismissed via DOM, trigger browser back
                    if (!dismissed) {
                        window.history.back();
                    }
                })();
            """.trimIndent()

            try {
                binding.webView.evaluateJavascript(jsCode) { result ->
                    Log.d("PaymentWebView", "dismissProcessingOverlay JS result: $result")
                }
            } catch (e: Exception) {
                Log.e("PaymentWebView", "Error injecting dismiss JS: ${e.message}", e)
                // Fallback: use WebView goBack
                if (binding.webView.canGoBack()) {
                    binding.webView.goBack()
                }
            }
        }, 1000) // 1 second delay to allow the overlay to appear before dismissing
    }

    override fun onDestroy() {
        // Cancel any pending JavaScript injection
        pendingJsInjection?.let { handler.removeCallbacks(it) }
        pendingJsInjection = null
        
        // Destroy WebView
        binding.webView.destroy()
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        binding.webView.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.webView.onResume()
    }
}
