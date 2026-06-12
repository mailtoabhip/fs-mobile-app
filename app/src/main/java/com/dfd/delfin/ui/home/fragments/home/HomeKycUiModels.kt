package com.dfd.delfin.ui.home.fragments.home

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.dfd.delfin.R

enum class KycUiState {
    START,
    CONTINUE,
    COMPLETED,
    REJECTED,
    VERIFICATION_PENDING
}

data class KycUiColors(
    @field:ColorRes val cardColorLight: Int,
    @field:ColorRes val cardColorDark: Int,
    @field:ColorRes val progressColor: Int,
    @field:ColorRes val progressBgColor: Int
)

data class KycUiStrings(
    @field:StringRes val headingResId: Int,
    @field:StringRes val messageResId: Int,
    @field:StringRes val buttonTextResId: Int
)

data class KycUiModel(
    val uiState: KycUiState,
    val progress: Int,
    val colors: KycUiColors = KycUiColorSchemes.forState(uiState),
    val strings: KycUiStrings = KycUiStringSchemes.forState(uiState)
)

object KycUiColorSchemes {
    val DEFAULT = KycUiColors(
        cardColorLight = R.color.cardboard_surface_light,
        cardColorDark = R.color.cardboard_surface_dark,
        progressColor = R.color.progress_copper,
        progressBgColor = R.color.bg_light_grey_v2
    )

    val COMPLETED = KycUiColors(
        cardColorLight = R.color.surface_success_dark,
        cardColorDark = R.color.surface_success_light,
        progressColor = R.color.bid_placed_green,
        progressBgColor = R.color.light_green
    )

    val REJECTED = KycUiColors(
        cardColorLight = R.color.surface_failed_dark,
        cardColorDark = R.color.surface_failed_light,
        progressColor = R.color.progress_red,
        progressBgColor = R.color.light_red
    )

    fun forState(state: KycUiState): KycUiColors = when (state) {
        KycUiState.START, KycUiState.CONTINUE, KycUiState.VERIFICATION_PENDING -> DEFAULT
        KycUiState.COMPLETED -> COMPLETED
        KycUiState.REJECTED -> REJECTED
    }
}

object KycUiStringSchemes {
    fun forState(state: KycUiState): KycUiStrings = when (state) {
        KycUiState.START -> KycUiStrings(
            headingResId = R.string.kyc_heading_start,
            messageResId = R.string.kyc_message_start,
            buttonTextResId = R.string.kyc_button_start
        )
        KycUiState.CONTINUE -> KycUiStrings(
            headingResId = R.string.kyc_heading_continue,
            messageResId = R.string.kyc_message_continue,
            buttonTextResId = R.string.kyc_button_continue
        )
        KycUiState.COMPLETED -> KycUiStrings(
            headingResId = R.string.kyc_heading_completed,
            messageResId = R.string.kyc_message_completed,
            buttonTextResId = R.string.kyc_button_completed
        )
        KycUiState.VERIFICATION_PENDING -> KycUiStrings(
            headingResId = R.string.kyc_heading_pending,
            messageResId = R.string.kyc_message_pending,
            buttonTextResId = R.string.kyc_button_pending
        )
        KycUiState.REJECTED -> KycUiStrings(
            headingResId = R.string.kyc_heading_rejected,
            messageResId = R.string.kyc_message_rejected,
            buttonTextResId = R.string.kyc_button_rejected
        )
    }
}
