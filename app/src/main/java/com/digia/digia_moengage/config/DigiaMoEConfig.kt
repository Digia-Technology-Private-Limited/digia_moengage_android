package com.digia.digia_moengage.config

import com.moengage.core.MoEngage

/**
 * Immutable configuration for the Digia + MoEngage integration.
 *
 * Centralises every knob the host needs to supply so there is no scattered configuration scattered
 * across multiple call-sites.
 *
 * @param moEngageAppId MoEngage workspace / app ID (from MoEngage dashboard).
 * @param dataCenter MoEngage data-centre region. Defaults to DATA_CENTER_1.
 * @param digiaAccessKey Digia project access key (from Digia dashboard).
 */
data class DigiaMoEConfig(
        val moEngageAppId: String,
        val dataCenter: MoEngage.DataCenter = MoEngage.DataCenter.DATA_CENTER_1,
        val digiaAccessKey: String,
)
