package com.delhivery.axle.config

import com.delhivery.axle.R

/**
 * Onboarding each screen config
 */
data class OnboardingScreen(
  val title: String,
  val image:Int
)

/**
 * On boarding config [List] of [OnboardingScreen]
 */
val OnboardingConfig = listOf(
    OnboardingScreen(
        "Find the right truck\nto carry your load",
        R.drawable.ic_illustration
    ),
    OnboardingScreen("Find the right truck\nto carry your load",  R.drawable.ic_illustration),
    OnboardingScreen(
        "Find the right truck\nto carry your load",  R.drawable.ic_illustration
    )
)