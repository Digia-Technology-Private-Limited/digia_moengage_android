package com.digia.digia_moengage.model

/**
 * Domain model for a MoEngage self-handled in-app campaign.
 *
 * The MoEngage KV payload must follow this contract:
 * ```json
 * {
 *   "type":    "dialog | bottomsheet | overlayvideo | inline",
 *   "id":      "<digia-page-id>",
 *   "args":    { ... },
 *   "context": { ... }
 * }
 * ```
 *
 * @param type Resolved render type — determines which Compose container is used.
 * @param pageId Digia page ID to render inside the container.
 * @param args Optional key-value arguments forwarded to the Digia page.
 * @param context Optional context metadata (e.g. screen name, user segment).
 */
data class DigiaCampaignModel(
        val type: CampaignType,
        val pageId: String,
        val args: Map<String, Any?> = emptyMap(),
        val context: Map<String, Any?> = emptyMap(),
        val settings: Map<String, Any?> = emptyMap(),
)
