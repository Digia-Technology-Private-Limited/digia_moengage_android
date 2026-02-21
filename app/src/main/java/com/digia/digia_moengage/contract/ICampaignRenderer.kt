package com.digia.digia_moengage.contract

import com.digia.digia_moengage.model.DigiaCampaignModel

/**
 * Contract for rendering a Digia campaign inside the host application.
 *
 * Dependency Inversion Principle: the integration layer depends on this abstraction rather than any
 * concrete Digia SDK class. The host injects an implementation that forwards the model to whatever
 * Digia rendering API it uses (e.g. DigiaSDK.getInstance().appState.set("campaign", model)).
 */
interface ICampaignRenderer {

    /**
     * Called when a MoEngage self-handled in-app campaign should be rendered through the Digia UI
     * layer.
     *
     * @param model Normalised campaign data (never null at this point).
     */
    fun render(model: DigiaCampaignModel)
}
