package com.digia.digia_moengage

import com.digia.digia_moengage.contract.IDigiaMoEListener
import com.digia.digia_moengage.internal.CampaignStore
import com.moengage.inapp.MoEInAppHelper
import com.moengage.inapp.listeners.SelfHandledAvailableListener
import com.moengage.inapp.model.SelfHandledCampaignData

/**
 * Observes MoEngage self-handled in-app events, maps the raw KV payload to a typed
 * [DigiaCampaignModel], and posts it to [CampaignStore].
 *
 * Observer pattern: MoEngage pushes; this class reacts and immediately hands off to the store. No
 * rendering logic lives here.
 *
 * @param listener Optional host callbacks for received/error events.
 */
internal class MoECampaignObserver(
        private val listener: IDigiaMoEListener,
) : SelfHandledAvailableListener {

    fun register() {
        MoEInAppHelper.getInstance().setSelfHandledListener ( this )
    }

    override fun onSelfHandledAvailable(data: SelfHandledCampaignData?) {
        data ?: return

        val model =
                CampaignMapper.map(data)
                        ?: run {
                            listener.onError(
                                    "CampaignMapper",
                                    IllegalArgumentException(
                                            "Invalid campaign payload: unknown type or missing 'id'. " +
                                                    "Payload received: ${data.campaign.payload}"
                                    )
                            )
                            return
                        }

        CampaignStore.post(model, data)
        listener.onCampaignReceived(model)
    }
}
