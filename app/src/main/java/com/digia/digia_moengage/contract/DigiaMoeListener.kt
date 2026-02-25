package com.digia.digia_moengage.contract

import android.util.Log
import com.digia.digia_moengage.model.DigiaCampaignModel

/**
 * Internal default [IDigiaMoEListener] used by [com.digia.digia_moengage.DigiaMoESDK] when the host
 * does not supply their own listener via [com.digia.digia_moengage.DigiaMoESDK.connect].
 *
 * ## Distinction from [IDigiaMoEListener.NoOp]
 *
 * | | [DigiaMoeListener] | [IDigiaMoEListener.NoOp] |
 * |--------------|--------------------|--------------------------| | Audience | SDK internals |
 * Public API callers | | onError | Logs via `Log.e` | Silent | | onCampaignReceived | No-op (store
 * already updated) | No-op |
 *
 * Use [IDigiaMoEListener.NoOp] when the host explicitly wants to opt out of ALL callbacks. This
 * object is the SDK-side default that surfaces errors during development.
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
