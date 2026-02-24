package com.digia.digia_moengage

import android.app.Application
import android.content.Context
import com.digia.digia_moengage.compose.DigiaMoEContext
//import com.digia.digia_moengage.contract.DigiaMoeListener
import com.digia.digia_moengage.contract.IDigiaMoEListener
import com.digia.digia_moengage.internal.CampaignStore
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

    private var isConnected = false

    /**
     * Registers MoEngage self-handled listener.
     * Call AFTER MoEngage + Digia are initialized.
     */
    fun connect() {
        if (isConnected) return
        val campaignObserver = MoECampaignObserver(IDigiaMoEListener.NoOp)
        campaignObserver.register()
        listener = campaignObserver
        isConnected = true
    }


    /**
     * get MoEngage self-handled Campaign.
     */
    fun getCampaign(context: Context){
        listener?.let {
            MoEInAppHelper.getInstance()
                .getSelfHandledInApp(context, it)
        }
    }


    /**
     * Updates current screen context.
     * Should be called on navigation change.
     */
    fun setContext(context: DigiaMoEContext) {
        CampaignStore.setContext(context)
    }


    /**
     * Programmatically dismiss current campaign.
     */
    fun dismiss() {
        CampaignStore.dismiss()
    }
}