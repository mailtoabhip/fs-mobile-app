package com.delhivery.orion.config

/**
 * Onboarding each screen config
 */
data class OnboardingScreen(
  val title: String,
  val message: String
)

/**
 * On boarding config [List] of [OnboardingScreen]
 */
val OnboardingConfig = listOf(
    OnboardingScreen(
        "Welcome to Orion!",
        "Glad to see that you have taken the right step towards the digitising logistics. We ensure you the best possible loads across the country."
    ),
    OnboardingScreen("Value add 1", "Some description about the value add from the app"),
    OnboardingScreen("Value add 1", "Some description about the value add from the app")
)