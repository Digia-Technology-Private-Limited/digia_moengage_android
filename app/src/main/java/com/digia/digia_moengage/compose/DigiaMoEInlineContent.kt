package com.digia.digia_moengage.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.digia.digia_moengage.internal.CampaignStore
import com.digia.digia_moengage.model.CampaignType

/**
 * Renders an **Inline** campaign at the exact position the host places this composable.
 *
 * ## Why this is a separate composable
 *
 * Unlike [CampaignType.Dialog] and [CampaignType.BottomSheet] — which are overlay types rendered
 * imperatively via [com.digia.digiaui.init.DigiaUIManager] regardless of layout position —
 * [CampaignType.Inline] content must appear at a **specific location** inside the host's
 * composition tree (e.g. between a hero banner and a product list). Only the host knows where that
 * location is, so this composable is provided separately for the host to place explicitly.
 *
 * [DigiaMoEViewBuilder] (inside [DigiaMoEHost]) intentionally skips the Inline type. This
 * composable is the sole owner of Inline rendering.
 *
 * ## Usage
 *
 * ```kotlin
 * // Inside any screen where you want inline campaign content:
 * Column {
 *     HeroBanner()
 *     DigiaMoEInlineContent()   // ← campaign content appears here when active
 *     ProductList()
 * }
 * ```
 *
 * When no Inline campaign is active (or the type is blocked by [DigiaMoEContext.allowedTypes]),
 * this composable renders nothing and occupies no space.
 *
 * @param modifier Optional [Modifier] forwarded to the rendered Digia component.
 */
@Composable
fun DigiaMoEInlineContent(modifier: Modifier = Modifier) {
    val ctx by CampaignStore.activeContext.collectAsState()
    val campaign by CampaignStore.activeCampaign.collectAsState()

    // Only react to Inline campaigns that are permitted on the current screen.
    val visible = campaign?.takeIf { it.type == CampaignType.Inline && it.type in ctx.allowedTypes }

    if (visible != null) {
        // Render the Digia component at this composable's position in the tree.
        // Uncomment and wire DUIFactory once the Digia SDK dependency is integrated:
        //
        // DUIFactory.getInstance().CreateComponent(
        //     modifier    = modifier,
        //     componentId = visible.pageId,
        //     args        = visible.args,
        // )
    }
}
