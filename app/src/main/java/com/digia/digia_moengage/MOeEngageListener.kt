package com.digia.digia_moengage.contract

import com.digia.digia_moengage.model.DigiaCampaignModel

/**
 * Event listener surface exposed to the host application.
 *
 * Interface Segregation Principle: all methods carry default no-op bodies so hosts only override
 * the events they actually care about — no empty stubs forced on consumers.
 */
interface IDigiaMoEListener {

    /** Invoked once the MoEngage SDK has been successfully initialised. */
    fun onMoEngageReady() {}

    /**
     * Invoked when a MoEngage self-handled in-app campaign has been received, mapped, and is ready
     * for Digia rendering.
     *
     * @param model Normalised, MoEngage-decoupled campaign data.
     */
    fun onCampaignReceived(model: DigiaCampaignModel) {}

    /**
     * Invoked when any integration step throws an exception.
     *
     * @param source Human-readable tag identifying the failing component.
     * @param error The underlying exception.
     */
    fun onError(source: String, error: Throwable) {}
}
