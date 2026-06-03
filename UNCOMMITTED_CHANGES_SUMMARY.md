# Uncommitted Changes Summary

## 1. Splash Flow — Migrated to StateFlow

**Files:**
- `SplashViewModel.kt`
- `StartRoutingActivity.kt`

**Purpose:** Fix broken `getProfileDetails()` that used `return` inside `viewModelScope.launch` (which returns from the lambda, not the function — so the state was never emitted).

**Changes:**
- Replaced synchronous `determineState()` return with a `MutableStateFlow<SplashPostState?>` that emits the resolved state.
- `StartRoutingActivity` now collects the flow using `lifecycleScope.launch { splashState.filterNotNull().first() }`.
- Added `isUserLoggedIn()` helper to decide whether to show "Get Started" button or auto-navigate.
- Flow: No token → show Get Started button; Token present → wait for profile API → navigate to Home or AccountDetails.

---

## 2. X-Client-Ip Race Condition Fix

**Files:**
- `DeviceInfoProvider.kt`
- `DelhiveryNetworkInterceptor.kt`

**Purpose:** Fix missing `X-Client-Ip` header on early API calls (profile on splash) because the public IP fetch hadn't completed yet.

**Changes:**
- Added `CompletableDeferred<String>` to `DeviceInfoProvider` that completes when IP fetch finishes.
- Added `awaitPublicIp(timeoutMs = 5000L)` — blocks up to 5s for the IP to be available.
- Interceptor now calls `awaitPublicIp()` instead of reading `publicIp` directly.

---

## 3. Token Refresh (TokenAuthenticator) — New File

**File:** `TokenAuthenticator.kt` (new)

**Purpose:** Handle 401 responses by automatically refreshing the access token and retrying the failed request.

**Changes:**
- Uses `UrlConfig.FsAuthService.url()` for dynamic base URL per flavor (fixes hardcoded wrong URLs).
- Normalizes trailing slash with `trimEnd('/')` to prevent URL concatenation bugs (`api.devfinserv.comapi`).
- Sends all required device headers (`X-Device-Id`, `X-Device-Model`, etc.) on the refresh request.
- Request body matches API contract: only `refresh_token` field (removed incorrect `access_token` field).
- Skips token refresh for `/auth/logout` endpoint (no point refreshing if user is logging out).
- On refresh failure: clears local session (`userPrefs.clearPrefs()`) and triggers `SessionManager.onSessionExpired()`.
- Detailed logging for debugging refresh flow.

---

## 4. Session Expired — Global Force-Logout

**Files:**
- `SessionManager.kt` (new)
- `BaseActivity.kt`
- `TokenAuthenticator.kt`

**Purpose:** When token refresh fails, immediately redirect the user to the login screen from any screen.

**Changes:**
- Created `SessionManager` singleton with `sessionExpired: LiveData<Boolean>`.
- `TokenAuthenticator` calls `sessionManager.onSessionExpired()` on refresh failure.
- `BaseActivity` observes `sessionExpired` and navigates to `AuthenticationActivity` with `CLEAR_TASK | NEW_TASK`.
- Excludes `AuthenticationActivity` and `StartRoutingActivity` from the redirect (they handle their own routing).

---

## 5. Logout Flow Fix

**File:** `MyProfileActivity.kt`

**Purpose:** Fix missing Authorization header on logout API call (token was cleared before the API fired).

**Changes:**
- Removed immediate `navigationUtils.logout()` call after `viewModel.logout()`.
- Added `logoutResultLiveData` observer: navigates to login only on API success, shows snackbar on failure.
- Both logout trigger points (confirm dialog + delayed handler) now only call `viewModel.logout()` and let the observer handle navigation.

---

## 6. FsRefreshTokenRequest — API Contract Fix

**Files:**
- `FsAuthRequest.kt`
- `FsAuthRepository.kt`

**Purpose:** Align the refresh token request model with the actual API contract.

**Changes:**
- Removed `accessToken` field from `FsRefreshTokenRequest` (API only requires `refresh_token`).
- Updated the commented-out `refreshToken()` method in `FsAuthRepository` to match.

---

## 7. MyWorker — Disabled Offers Sync

**File:** `HomeTrucksViewModel.kt`

**Purpose:** Stop the periodic `MyWorker` from being enqueued (offers sync no longer needed).

**Changes:**
- `fetchData()` is now a no-op (body removed, method signature kept for callers).

---

## 8. Phone Number Formatting (XXXXX XXXXX)

**Files:**
- `activity_authentication.xml`
- `AuthenticationActivity.kt`
- `AuthenticationViewModel.kt`

**Purpose:** Auto-format phone number with a space after 5 digits for readability, while sending raw digits to the API.

**Changes:**
- XML: `maxLength="11"`, `digits="1234567890 "` (allows programmatic space).
- Added `TextWatcher` that auto-inserts space at position 5.
- ViewModel: all API calls (`initiate`, `resend`, `verify`) use `phoneNo.replace(" ", "")` to strip the space.
- `onLengthReached` threshold updated to 11 (10 digits + 1 space).

---

## 9. Resend OTP — Clear Input & Error State

**File:** `AuthenticationActivity.kt`

**Purpose:** When user clicks "Resend OTP", clear the entered OTP digits and remove the red error outline.

**Changes:**
- Added `binding.otpView.clear()` and `binding.otpView.showError(false)` to the resend button click handler.

---

## 10. Auth VM — Verify OTP Navigation (No Extra Profile Call)

**File:** `AuthenticationViewModel.kt`

**Purpose:** Remove redundant profile API call after OTP verify. The verify response already populates firstName/lastName in prefs.

**Changes:**
- Removed `fsAuthRepository.getProfile()` call from `verifyOTP()`.
- Navigation decision now reads directly from `userPrefs.firstName` and `userPrefs.lastName` (already saved by `FsAuthRepository.verify()`).
- firstName & lastName present → `HomePage`; missing → `AccountDetails`.
- Eliminates one network call on every login.

---

## 11. AccountDetails Checkbox Styling

**File:** `activity_account_details.xml`

**Purpose:** Make the communication consent checkbox have a black box with white checkmark.

**Changes:**
- Added `android:buttonTint="@color/black"` to the CheckBox.

---

## Files Changed Summary

| File | Type |
|------|------|
| `SessionManager.kt` | New |
| `TokenAuthenticator.kt` | New (previously didn't exist) |
| `SplashViewModel.kt` | Modified |
| `StartRoutingActivity.kt` | Modified |
| `DeviceInfoProvider.kt` | Modified |
| `DelhiveryNetworkInterceptor.kt` | Modified |
| `BaseActivity.kt` | Modified |
| `MyProfileActivity.kt` | Modified |
| `AuthenticationActivity.kt` | Modified |
| `AuthenticationViewModel.kt` | Modified |
| `HomeTrucksViewModel.kt` | Modified |
| `FsAuthRequest.kt` | Modified |
| `FsAuthRepository.kt` | Modified |
| `FsAuthResponse.kt` | Modified |
| `NetworkModule.kt` | Modified |
| `AccountDetailsActivity.kt` | Modified |
| `UserPrefs.kt` | Modified |
| `activity_authentication.xml` | Modified |
| `activity_account_details.xml` | Modified |
