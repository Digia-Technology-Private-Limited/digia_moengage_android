package com.digia.digia_moengage.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.digia.digia_moengage.DigiaMoESDK
import com.digia.digia_moengage.internal.CampaignStore
import com.digia.digia_moengage.model.CampaignType
import com.digia.digiaui.init.DigiaUIManager

/**
 * Imperative campaign renderer for **non-spatial** campaign types.
 *
 * Handles [CampaignType.Dialog], [CampaignType.BottomSheet], and [CampaignType.Pip] by calling the
 * appropriate [DigiaUIManager] APIs. These types are "overlay" campaigns — they are not tied to a
 * specific position in the composition tree, so they are driven purely via manager calls rather
 * than rendered inline.
 *
 * ## Campaign type split
 *
 * | Type | Rendered by | Placement |
 * |---------------|-------------------------------|-------------------------------| | Dialog |
 * [DigiaUIManager.dialogManager] | Auto via [DigiaMoEHost] | | BottomSheet |
 * [DigiaUIManager.bottomSheetManager] | Auto via [DigiaMoEHost] | | Pip | PIP manager (future) |
 * Auto via [DigiaMoEHost] | | **Inline** | **[DigiaMoEInlineContent]** | **Host places it
 * explicitly** |
 *
 * [CampaignType.Inline] is intentionally **not** handled here. The host must place
 * [DigiaMoEInlineContent] at precisely the location in the composition tree where inline content
 * should appear — this composable cannot know that position.
 *
 * ## Show / dismiss lifecycle
 *
 * A single [LaunchedEffect] keyed on `visible` drives every transition:
 *
 * | Trigger | `visible` | Effect |
 * |----------------------------------|-----------|--------------------------------| | New overlay
 * campaign arrives | non-null | show the appropriate manager | | [DigiaMoESDK.dismiss] called |
 * null | dismiss all overlay managers | | [DigiaMoESDK.setContext] (nav) | null | dismiss all
 * overlay managers | | Type not in `allowedTypes` | null | dismiss all overlay managers |
 *
 * ## Placement
 *
 * Added automatically by [DigiaMoEHost]. Do **not** add this composable manually unless you are
 * building a custom host wrapper.
 */
@Composable
internal fun DigiaMoEViewBuilder() {
    val ctx by CampaignStore.activeContext.collectAsState()
    val campaign by CampaignStore.activeCampaign.collectAsState()
    val androidContext = LocalContext.current

    // Only campaigns whose type is allowed on the current screen — and whose type is an
    // overlay type — are eligible here. Inline campaigns are excluded; they are handled by
    // DigiaMoEInlineContent placed by the host.
    val visible = campaign?.takeIf { it.type in ctx.allowedTypes && it.type != CampaignType.Inline }

    // Single LaunchedEffect drives ALL overlay show / dismiss transitions.
    // Keyed on `visible` so it re-runs when the campaign appears, disappears,
    // or is replaced. CampaignStore already clears `visible` on page-id change,
    // which triggers the null branch here and cleans up every UI layer.
    LaunchedEffect(visible) {
        when (val c = visible) {
            null -> {
                // Campaign gone: dismissed, blocked by allowedTypes, or screen changed.
                // Dismiss every overlay layer so nothing is left dangling.
                DigiaUIManager.getInstance().dialogManager?.dismiss()
                DigiaUIManager.getInstance().bottomSheetManager?.dismiss()
            }
            else ->
                    when (c.type) {
                        CampaignType.Dialog -> {
                            DigiaUIManager.getInstance()
                                    .dialogManager
                                    ?.show(
                                            componentId = c.pageId,
                                            args = c.args,
                                            onDismiss = { DigiaMoESDK.dismiss(androidContext) },
                                    )
                            DigiaMoESDK.trackShown(androidContext)
                        }
                        CampaignType.BottomSheet -> {
                            DigiaUIManager.getInstance()
                                    .bottomSheetManager
                                    ?.show(
                                            componentId = c.pageId,
                                            args = c.args,
                                            onDismiss = { DigiaMoESDK.dismiss(androidContext) },
                                    )
                            DigiaMoESDK.trackShown(androidContext)
                        }
                        CampaignType.Pip -> {
                            // TODO: wire PIP manager when implemented.
                            // DigiaUIManager.getInstance().pipManager?.show(c.pageId, c.args) {
                            // DigiaMoESDK.dismiss(androidContext) }
                            // DigiaMoESDK.trackShown(androidContext)
                        }
                        // Inline is spatial — handled exclusively by DigiaMoEInlineContent.
                        CampaignType.Inline -> Unit
                    }
        }
    }
}
