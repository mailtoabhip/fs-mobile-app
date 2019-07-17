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
        "Welcome to Orion",
        "We're changing the way India ships freight across the country. You're now part of our revolutionary journey !"
    ),
    OnboardingScreen("Best Loads", "View, search and bid for loads in your favorite lanes"),
    OnboardingScreen(
        "Payment Transparency", "Track pending advance and balance payment all in one place"
    )
)