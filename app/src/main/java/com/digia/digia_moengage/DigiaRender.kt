package com.digia.digia_moengage

import com.digia.digia_moengage.contract.ICampaignRenderer
import com.digia.digia_moengage.model.DigiaCampaignModel

/**
 * Default [ICampaignRenderer] that bridges a MoEngage campaign payload into the Digia UI rendering
 * pipeline.
 *
 * Design decision: the actual rendering call is injected as a lambda so the host can wire this to
 * the specific Digia SDK version it uses **without** this integration layer ever importing the
 * Digia SDK directly.
 *
 * Example host-side wiring (inside [DigiaMoESDK.Builder.renderer]):
 * ```kotlin
 * .renderer { model ->
 *     // Using Digia SDK from digia_ui_compose:
 *     DigiaSDK.getInstance().appState.set("campaign_id",      model.id)
 *     DigiaSDK.getInstance().appState.set("campaign_payload", model.payload)
 * }
 * ```
 *
 * @param onRender Lambda executed on every incoming campaign. Must be thread-safe.
 */
class DigiaRenderer(
        private val onRender: (DigiaCampaignModel) -> Unit,
) : ICampaignRenderer {

    override fun render(model: DigiaCampaignModel) {
        onRender(model)
    }
}
