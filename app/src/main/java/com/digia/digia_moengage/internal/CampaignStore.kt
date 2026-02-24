package com.digia.digia_moengage.internal

import com.digia.digia_moengage.compose.DigiaMoEContext
import com.digia.digia_moengage.model.DigiaCampaignModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Internal reactive store — single source of truth for both the active campaign and the current
 * screen context.
 *
 * Thread-safe: [MutableStateFlow] handles concurrent writes from the MoEngage callback thread and
 * reads from the Compose main thread.
 */
internal object CampaignStore {

    // ── Active campaign ───────────────────────────────────────────────────────

    private val _activeCampaign = MutableStateFlow<DigiaCampaignModel?>(null)

    /** The campaign currently waiting to be shown, or null when nothing is queued. */
    val activeCampaign: StateFlow<DigiaCampaignModel?> = _activeCampaign.asStateFlow()

    /** Post a new campaign — replaces any previously queued one. */
    fun post(model: DigiaCampaignModel) {
        _activeCampaign.value = model
    }

    /** Clear the active campaign (user dismissed or page changed). */
    fun dismiss() {
        _activeCampaign.value = null
    }

    // ── Screen context ────────────────────────────────────────────────────────

    private val _activeContext = MutableStateFlow(DigiaMoEContext())

    /**
     * The context set by the host via [DigiaMoESDK.setContext]. Defaults to a permissive context
     * (all types, no page tracking).
     */
    val activeContext: StateFlow<DigiaMoEContext> = _activeContext.asStateFlow()

    /**
     * Update the active screen context. If the page ID changes, the active campaign is
     * auto-dismissed.
     */
    fun setContext(ctx: DigiaMoEContext) {
        val previous = _activeContext.value
        _activeContext.value = ctx
        if (ctx.currentPageId != null && ctx.currentPageId != previous.currentPageId) {
            dismiss()
        }
    }
}
