package com.digia.digia_moengage.compose

import com.digia.digia_moengage.model.CampaignType

/**
 * Scoping context that controls campaign visibility on the current screen.
 *
 * Set via [DigiaMoESDK.setContext] whenever the user navigates to a new screen.
 * [DigiaMoEViewBuilder] subscribes to it and auto-dismisses stale campaigns.
 *
 * @param currentPageId Identifier for the screen currently shown. Changing
 * ```
 *                       this automatically dismisses any active campaign so
 *                       a stale overlay never bleeds into the next screen.
 *                       Null means "global" — campaigns are never dismissed
 *                       by navigation.
 * @param allowedTypes
 * ```
 * Campaign types permitted on this screen. Use this to
 * ```
 *                       block dialogs on critical screens (e.g. checkout).
 *                       Defaults to all types.
 * ```
 */
data class DigiaMoEContext(
        val currentPageId: String? = null,
        val allowedTypes: Set<CampaignType> = CampaignType.all(),
)
