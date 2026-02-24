package com.digia.digia_moengage.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.digia.digia_moengage.DigiaMoESDK
import com.digia.digia_moengage.internal.CampaignStore
import com.digia.digia_moengage.model.CampaignType
import com.digia.digiaui.framework.DUIFactory
import com.digia.digiaui.init.DigiaUIManager

/**
 * Reactive campaign renderer driven by [CampaignStore].
 *
 * ## How show / dismiss works
 *
 * All side-effects (calling [DigiaUIManager] show/dismiss, updating [PipGlobalState]) live in a
 * single [LaunchedEffect] keyed on `visible` — the currently-allowed campaign.
 *
 * | Trigger | `visible` value | Effect |
 * |---------------------------------------------|-----------------|---------------------------------|
 * | New campaign arrives | non-null | show appropriate manager/pip | | [DigiaMoESDK.dismiss] called
 * | null | dismiss all managers + pip | | [DigiaMoESDK.setContext] called (page change)| null |
 * dismiss all managers + pip | | Campaign type not in `allowedTypes` | null | dismiss all managers
 * + pip |
 *
 * ## Placement
 *
 * Normally placed automatically by [DigiaMoEHost] — you do not need to add this manually if you use
 * that wrapper.
 *
 * For **Inline** campaigns only, place this inside the exact `Box` where you want the content to
 * appear, since it renders the Digia component directly in the composition.
 *
 * @param pipConfig Size and corner configuration for [CampaignType.Pip] overlay.
 */
@Composable
fun DigiaMoEViewBuilder() {
    val ctx by CampaignStore.activeContext.collectAsState()
    val campaign by CampaignStore.activeCampaign.collectAsState()

    // Campaigns whose type is not allowed on the current screen are treated as absent.
    val visible = campaign?.takeIf { it.type in ctx.allowedTypes }

    // ── Single LaunchedEffect drives ALL show / dismiss transitions ───────
    //
    // Keyed on `visible` (a data class) so it re-runs whenever the campaign
    // appears, disappears, or is replaced by a different one. This is the
    // answer to "how do we dismiss when context changes" — CampaignStore
    // already sets visible=null on page-id change, which triggers this block.
    LaunchedEffect(visible) {
        when (val c = visible) {
            null -> {
                // Campaign gone (dismissed, blocked by allowedTypes, or screen changed).
                // Dismiss every possible UI layer so nothing is left dangling.
                DigiaUIManager.getInstance().dialogManager?.dismiss()
                DigiaUIManager.getInstance().bottomSheetManager?.dismiss()
            }
            else ->
                    when (c.type) {
                        CampaignType.Dialog ->
                                DigiaUIManager.getInstance()
                                        .dialogManager
                                        ?.show(
                                                componentId = c.pageId,
                                                args = c.args,
                                                // Sync CampaignStore when the dialog is closed
                                                // natively (barrier tap,
                                                // back press, or a DUI action) so the store never
                                                // stays stale.
                                                onDismiss = { DigiaMoESDK.dismiss() },
                                        )
                        CampaignType.BottomSheet ->
                                DigiaUIManager.getInstance()
                                        .bottomSheetManager
                                        ?.show(
                                                componentId = c.pageId,
                                                args = c.args,
                                                onDismiss = { DigiaMoESDK.dismiss() },
                                        )
                        CampaignType.Pip ->
                            // PipGlobalState drives DraggablePipWindow rendered below.
                        {}
                        CampaignType.Inline -> {
                            // Inline content is rendered in the composition tree below.
                            // No imperative manager call needed.
                        }
                    }
        }
    }

    // ── Inline content — rendered at this composable's position ──────────
    if (visible?.type == CampaignType.Inline) {
        DUIFactory.getInstance()
                .CreateComponent(
                        componentId = visible.pageId,
                        args = visible.args,
                )
    }
}
