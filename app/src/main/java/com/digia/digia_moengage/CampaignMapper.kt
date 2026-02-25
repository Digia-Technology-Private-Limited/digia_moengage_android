package com.digia.digia_moengage

import com.digia.digia_moengage.model.CampaignType
import com.digia.digia_moengage.model.DigiaCampaignModel
import com.moengage.inapp.model.SelfHandledCampaignData
import org.json.JSONObject

/**
 * Parses a MoEngage [SelfHandledCampaignData] into a typed [DigiaCampaignModel].
 *
 * In MoEngage inapp 9.x, the self-handled campaign payload is delivered as a JSON string in
 * [SelfHandledCampaignData.campaign.payload]. Configure the campaign in the MoEngage dashboard with
 * a JSON body like:
 *
 * ```json
 * {
 *   "type":    "dialog",
 *   "id":      "<digia-page-id>",
 *   "args":    { "param1": "value1" },
 *   "context": { "screen": "home" }
 * }
 * ```
 *
 * Returns null on unknown type, missing required fields, or unparseable JSON so the observer can
 * surface the error via [IDigiaMoEListener.onError].
 *
 * Single Responsibility: pure parsing, zero side-effects.
 */
internal object CampaignMapper {

    fun map(campaignData: SelfHandledCampaignData): DigiaCampaignModel? {
        val rawPayload = campaignData.campaign.payload.takeIf { it.isNotBlank() } ?: return null

        val json =
                try {
                    JSONObject(rawPayload)
                } catch (e: Exception) {
                    return null
                }

        val type = CampaignType.from(json.optString("type")) ?: return null
        val pageId = json.optString("pageId").takeIf { it.isNotBlank() } ?: return null

        val args = json.optJSONObject("args")?.toMap() ?: emptyMap()
        val context = json.optJSONObject("context")?.toMap() ?: emptyMap()

        return DigiaCampaignModel(
                type = type,
                pageId = pageId,
                args = args,
                context = context,
        )
    }

    private fun JSONObject.toMap(): Map<String, Any?> =
            keys().asSequence().associateWith { opt(it) }
}
