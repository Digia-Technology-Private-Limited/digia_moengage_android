package com.digia.digia_moengage

import android.app.Application
import android.content.Context
import com.digia.digia_moengage.compose.DigiaMoEContext
import com.digia.digia_moengage.contract.DigiaMoeListener
import com.digia.digia_moengage.contract.IDigiaMoEListener
import com.digia.digia_moengage.internal.CampaignStore
import com.digia.digiaui.framework.analytics.AnalyticEvent
import com.digia.digiaui.framework.analytics.AnalyticsHandler
import com.digia.digiaui.framework.analytics.DUIAnalytics
import com.moengage.inapp.MoEInAppHelper
import com.moengage.inapp.listeners.SelfHandledAvailableListener

/**
 * Single entry-point for the Digia + MoEngage bridge.
 *
 * Host responsibilities (before calling connect):
 * 1. DigiaSDK.initialize(DigiaUIOptions(...)) - Digia config fetch (async)
 * 2. MoEngage.initialiseDefaultInstance(...) - MoEngage boot (sync)
 *
 * This object owns nothing about those SDKs. It only wires the MoEngage self-handled campaign
 * observer to the internal CampaignStore so DigiaMoEViewBuilder can reactively render the correct
 * Digia page.
 */
object DigiaMoESDK {

    // Keep it nullable and internal
    private var listener: SelfHandledAvailableListener? = null
    private var appContext: Context? = null
    private var isConnected = false

    /**
     * Registers MoEngage self-handled listener and wires Digia analytics → MoEngage click tracking.
     *
     * Call AFTER both MoEngage and Digia are initialized.
     *
     * @param app Application context — stored internally so the analytics callback can forward
     * click events to MoEngage without requiring callers to pass a Context each time.
     * @param hostListener Optional [IDigiaMoEListener] for receiving campaign and error callbacks.
     * When omitted, an internal default listener that logs errors to Logcat is used.
     */
    fun connect(
            app: Application,
            hostListener: IDigiaMoEListener? = null,
    ) {
        if (isConnected) return
        appContext = app.applicationContext
        val campaignObserver = MoECampaignObserver(hostListener ?: DigiaMoeListener)
        campaignObserver.register()
        listener = campaignObserver
        registerAnalyticsHook()
        isConnected = true
    }

    /**
     * Registers a [DUIAnalytics] provider with [AnalyticsHandler] that automatically calls
     * [MoEInAppHelper.selfHandledClicked] whenever any Digia analytics event fires while a campaign
     * is active.
     *
     * Any button tap, link press, or custom event inside the campaign's Digia component will
     * satisfy this — no dashboard configuration is required.
     *
     * If the host has already set [AnalyticsHandler.analyticsProvider], the existing provider is
     * wrapped so both handlers receive every event.
     */
    private fun registerAnalyticsHook() {
        val existing = AnalyticsHandler.analyticsProvider
        AnalyticsHandler.analyticsProvider =
                object : DUIAnalytics {
                    override fun onEvent(events: List<AnalyticEvent>) {
                        // Forward to any pre-existing analytics provider first.
                        existing?.onEvent(events)
                        // If a campaign is currently visible, report the interaction to MoEngage.
                        val ctx = appContext ?: return
                        val raw = CampaignStore.activeRawCampaign.value ?: return
                        MoEInAppHelper.getInstance().selfHandledClicked(ctx, raw)
                    }
                }
    }

    /** get MoEngage self-handled Campaign. */
    fun getCampaign(context: Context) {
        listener?.let { MoEInAppHelper.getInstance().getSelfHandledInApp(context, it) }
    }

    /** Updates current screen context. Should be called on navigation change. */
    fun setContext(context: DigiaMoEContext) {
        CampaignStore.setContext(context)
    }

    /**
     * Report to MoEngage that the campaign was shown to the user. Call this immediately after
     * rendering the campaign UI.
     */
    fun trackShown(context: Context) {
        val raw = CampaignStore.activeRawCampaign.value ?: return
        MoEInAppHelper.getInstance().selfHandledShown(context, raw)
    }

    /**
     * Report to MoEngage that the user clicked/interacted with the campaign. Call this from your
     * Digia page's click handler.
     */
    fun trackClicked(context: Context) {
        val raw = CampaignStore.activeRawCampaign.value ?: return
        MoEInAppHelper.getInstance().selfHandledClicked(context, raw)
    }

    /**
     * Dismiss the active campaign and report the dismissal to MoEngage. Use this overload from UI
     * callbacks where [android.content.Context] is available (e.g. the [onDismiss] lambda inside
     * [DigiaMoEViewBuilder]).
     */
    fun dismiss(context: Context) {
        val raw = CampaignStore.activeRawCampaign.value
        CampaignStore.dismiss()
        if (raw != null) MoEInAppHelper.getInstance().selfHandledDismissed(context, raw)
    }

    /**
     * Programmatically dismiss the current campaign without MoEngage tracking. Used internally when
     * the page context changes (auto-dismiss).
     */
    fun dismiss() {
        CampaignStore.dismiss()
    }
}
