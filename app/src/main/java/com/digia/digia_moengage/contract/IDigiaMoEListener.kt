package com.digia.digia_moengage.contract

import com.digia.digia_moengage.model.DigiaCampaignModel

/**
 * Optional callbacks exposed to the host.
 *
 * All methods default to no-ops — host only overrides what it needs.
 */
interface IDigiaMoEListener {

    /** Fired when a campaign was received, validated, and queued for display. */
    fun onCampaignReceived(model: DigiaCampaignModel) {}

    /**
     * Fired when the observer receives a payload that cannot be parsed
     * (e.g. unknown type, missing page ID) or when any internal step throws.
     *
     * @param source  Component name where the error originated.
     * @param error   The underlying exception.
     */
    fun onError(source: String, error: Throwable) {}

    companion object {
        /** Silent no-op — used when host omits a listener. */
        val NoOp: IDigiaMoEListener = object : IDigiaMoEListener {}
    }
}
