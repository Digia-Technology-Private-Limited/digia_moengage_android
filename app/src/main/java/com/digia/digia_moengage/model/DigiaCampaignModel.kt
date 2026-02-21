package com.digia.digia_moengage.model

/**
 * Framework-agnostic domain model for an in-app campaign received from MoEngage.
 *
 * Deliberately decoupled from [com.moengage.inapp.model.SelfHandledCampaignData] so that the rest
 * of the codebase (including Digia-side rendering) never acquires a hard dependency on the MoEngage
 * SDK.
 *
 * @param id Unique campaign identifier.
 * @param payload Arbitrary key-value pairs attached to the campaign.
 */
data class DigiaCampaignModel(
        val id: String,
        val payload: Map<String, Any?>,
)
