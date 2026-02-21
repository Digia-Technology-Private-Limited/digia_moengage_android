package com.digia.digia_moengage

import android.app.Application
import com.moengage.inapp.MoEInAppHelper
import com.moengage.inapp.listeners.SelfHandledAvailableListener
import com.moengage.inapp.model.SelfHandledCampaignData

/**
 * Observes MoEngage self-handled in-app campaigns and delegates processing to [CampaignProcessor].
 *
 * Observer pattern: MoEngage pushes data; this class reacts and immediately hands off — it knows
 * nothing about mapping or rendering.
 *
 * @param processor Handles the map → render → notify pipeline.
 */
internal class MoECampaignObserver(
        private val processor: CampaignProcessor,
) : SelfHandledAvailableListener {

    /** Register this observer so MoEngage delivers campaigns to it. */
    fun register(application: Application) {
        MoEInAppHelper.getInstance().getSelfHandledInApp(application, this)
    }

    override fun onSelfHandledAvailable(data: SelfHandledCampaignData?) {
        // Guard: MoEngage may deliver null when no campaign is available.
        data ?: return
        processor.process(data)
    }
}
