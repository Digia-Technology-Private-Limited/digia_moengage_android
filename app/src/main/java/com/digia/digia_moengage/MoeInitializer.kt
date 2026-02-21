package com.digia.digia_moengage

import android.app.Application
import com.digia.digia_moengage.config.DigiaMoEConfig
import com.moengage.core.MoEngage

/**
 * Responsible solely for bootstrapping the MoEngage SDK.
 *
 * Single Responsibility Principle: this object has exactly one reason to change — when the MoEngage
 * initialisation API changes.
 */
internal object MoEInitializer {

    fun initialize(app: Application, config: DigiaMoEConfig) {
        val moEngage =
                MoEngage.Builder(
                                app,
                                config.moEngageAppId,
                                config.dataCenter,
                        )
                        .build()

        MoEngage.initialiseDefaultInstance(moEngage)
    }
}
