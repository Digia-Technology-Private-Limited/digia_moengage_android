package com.digia.digia_moengage.contract

import android.util.Log
import com.digia.digia_moengage.model.DigiaCampaignModel

/**
 * Internal no-op [IDigiaMoEListener] used when no host listener is supplied.
 *
 * [MoECampaignObserver] already posts every received campaign to [CampaignStore] before calling
 * this listener, so no UI work belongs here. [DigiaMoEViewBuilder] observes [CampaignStore]
 * reactively and handles all show / dismiss transitions via [LaunchedEffect].
 *
 * Single Responsibility: this object is only responsible for being a valid listener reference —
 * nothing else.
 */
internal object DigiaMoeListener : IDigiaMoEListener {

    override fun onCampaignReceived(model: DigiaCampaignModel) {
        // Campaign is already posted to CampaignStore by MoECampaignObserver.
        // DigiaMoEViewBuilder reacts to the store change automatically.
    }

    override fun onError(source: String, error: Throwable) {
        Log.e("DigiaMoESDK", "[$source] ${error.message}", error)
    }
}
