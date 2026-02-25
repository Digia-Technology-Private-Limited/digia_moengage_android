package com.digia.digia_moengage.model

/**
 * Typed representation of what the MoEngage "type" field maps to.
 *
 * Each value determines how [com.digia.digia_moengage.compose.DigiaMoEViewBuilder] or
 * [com.digia.digia_moengage.compose.DigiaMoEInlineContent] presents the Digia page.
 *
 * | Value | Payload string(s) | Rendered by | Placement |
 * |-------------|----------------------|-------------------------------|-------------------------------|
 * | [Dialog] | `"dialog"` | `DigiaUIManager.dialogManager` | Auto via `DigiaMoEHost` | |
 * [BottomSheet] | `"bottomsheet"` | `DigiaUIManager.bottomSheetManager` | Auto via `DigiaMoEHost` |
 * | [Pip] | `"overlayvideo"`, `"pip"` | PIP manager (future) | Auto via `DigiaMoEHost` | | [Inline]
 * | `"inline"` | `DigiaMoEInlineContent` | Host places explicitly |
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
                    "overlayvideo", "pip" -> Pip
                    "inline" -> Inline
                    else -> null
                }
    }
}
