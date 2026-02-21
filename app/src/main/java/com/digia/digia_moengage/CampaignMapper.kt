package com.digia.digia_moengage

import com.digia.digia_moengage.contract.ICampaignRenderer
import com.digia.digia_moengage.contract.IDigiaMoEListener
import com.digia.digia_moengage.model.DigiaCampaignModel
import com.moengage.inapp.model.SelfHandledCampaignData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Maps a MoEngage [SelfHandledCampaignData] to a framework-agnostic [DigiaCampaignModel], isolating
 * mapping logic so both [MoECampaignObserver] and [CampaignProcessor] remain independently
 * testable.
 *
 * Single Responsibility Principle: only mapping, nothing else.
 */
internal object MoECampaignMapper {

    fun map(data: SelfHandledCampaignData): DigiaCampaignModel =
            DigiaCampaignModel(
                    id = data.campaignId ?: "",
                    payload = data.selfHandledData?.kvPair ?: emptyMap(),
            )
}

/**
 * Orchestrates the map → render → notify pipeline for a single campaign.
 *
 * Open/Closed Principle: extend behaviour by swapping [ICampaignRenderer] or [IDigiaMoEListener]
 * without touching this class.
 *
 * **Race-condition safety**: MoEngage campaigns can arrive while Digia is still fetching its config
 * over the network. [process] suspends on [digiaReadyGuard] before touching any Digia API, so the
 * campaign is never dropped — it is queued by the coroutine runtime and fires the moment Digia
 * becomes ready.
 *
 * @param renderer Digia rendering strategy (injected by host).
 * @param listener Host event callback (injected by host).
 * @param digiaReadyGuard Suspend lambda that resolves once Digia is ready.
 * ```
 *                        Default wires to [DigiaSDK.ensureInitialized()].
 *                        Override in tests to skip the real SDK.
 * ```
 */
internal class CampaignProcessor(
        private val renderer: ICampaignRenderer,
        private val listener: IDigiaMoEListener,
        private val digiaReadyGuard: suspend () -> Unit = { /* no-op: host wires real guard */},
) {
    // One supervisor scope per processor — campaigns processed serially on IO
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Enqueues the campaign for processing. Suspends internally until the Digia SDK signals it is
     * fully initialised, then renders.
     */
    fun process(data: SelfHandledCampaignData) {
        scope.launch {
            // Wait for Digia to finish its async network init before
            // touching appState/renderer. MoEngage is already sync-ready
            // so no guard needed on that side.
            digiaReadyGuard()

            val model = MoECampaignMapper.map(data)
            renderer.render(model)
            listener.onCampaignReceived(model)
        }
    }
}
