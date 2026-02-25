package com.digia.digia_moengage.model

/**
 * Typed representation of what the MoEngage "type" field maps to.
 *
 * Each value determines which Compose container [DigiaMoEViewBuilder] uses to present the Digia
 * page:
 *
 * | Value | Container | |-----------------|--------------------------------------------| | [Dialog]
 * | `AlertDialog` — modal, blocks interaction | | [BottomSheet] | `ModalBottomSheet` — slides from
 * bottom | | [OverlayVideo] | Full-screen `Box` overlay | | [Inline] | Rendered in-place wherever
 * host puts the composable |
 */
sealed class CampaignType {
    data object Dialog : CampaignType()
    data object BottomSheet : CampaignType()
    data object Pip : CampaignType()
    data object Inline : CampaignType()

    companion object {
        /** All known types — used as the default allow-list in [DigiaMoEContext]. */
        fun all(): Set<CampaignType> = setOf(Dialog, BottomSheet, Pip, Inline)

        /**
         * Parses the raw string from the MoEngage KV payload. Returns `null` for any unrecognised
         * value so the observer can report it via [IDigiaMoEListener.onError] rather than crash.
         */
        fun from(value: String?): CampaignType? =
                when (value?.trim()?.lowercase()) {
                    "dialog" -> BottomSheet
                    "bottomsheet" -> BottomSheet
                    "overlayvideo" -> Pip
                    "inline" -> Inline
                    else -> null
                }
    }
}
