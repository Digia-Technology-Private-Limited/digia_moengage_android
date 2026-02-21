package com.digia.digia_moengage

import android.app.Application
import com.digia.digia_moengage.config.DigiaMoEConfig
import com.digia.digia_moengage.contract.IDigiaMoEListener
import com.digia.digia_moengage.model.DigiaCampaignModel
import com.moengage.core.MoEngage

/**
 * Application entry-point for the Digia + MoEngage integration.
 *
 * Both SDKs **must** be initialised here (not in an Activity) so they are alive before the very
 * first screen is shown and before any background work can deliver campaigns.
 *
 * ## Initialisation order
 * 1. **Digia SDK** — initialised first so its app-state store is accepting
 * ```
 *    writes before MoEngage delivers the first campaign.
 * ```
 * 2. **[DigiaMoESDK]** — boots MoEngage and registers the campaign observer.
 * ```
 *    The renderer lambda bridges incoming campaigns into Digia's app-state.
 * ```
 */
class DigiaApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val config =
                DigiaMoEConfig(
                        moEngageAppId = "YOUR_MOENGAGE_APP_ID", // ← replace
                        dataCenter = MoEngage.DataCenter.DATA_CENTER_1,
                        digiaAccessKey = "YOUR_DIGIA_ACCESS_KEY", // ← replace
                )

        // Step 1 ─ Initialise Digia SDK (from digia_ui_compose library).
        // Uncomment once the Digia SDK dependency is added to build.gradle.
        //
        // DigiaSDK.initialize(
        //     DigiaUIOptions(
        //         context    = this,
        //         accessKey  = config.digiaAccessKey,
        //         flavor     = Flavor.Release(),
        //     )
        // )

        // Step 2 ─ Boot MoEngage + register campaign observer.
        DigiaMoESDK.Builder(this, config)
                // Wire the Digia readiness guard so any campaign that arrives
                // before Digia finishes its network fetch is held in the
                // coroutine queue and delivered the moment Digia is ready.
                // Without this, a fast MoEngage campaign would try to write into
                // Digia's appState before it exists and silently fail.
                //
                // Uncomment once the Digia SDK dependency is added:
                // .digiaReadyGuard { DigiaSDK.ensureInitialized() }
                .listener(
                        object : IDigiaMoEListener {
                            override fun onMoEngageReady() {
                                // MoEngage is live — attach push tokens, set user attributes, etc.
                            }

                            override fun onCampaignReceived(model: DigiaCampaignModel) {
                                // Push the normalised campaign model into Digia app-state.
                                // DigiaSDK.getInstance().appState.set("campaign_id",      model.id)
                                // DigiaSDK.getInstance().appState.set("campaign_payload",
                                // model.payload)
                            }

                            override fun onError(source: String, error: Throwable) {
                                // Route to your crash / analytics reporter.
                            }
                        }
                )
                .renderer { model ->
                    // Renderer lambda — an alternative / additional hook that
                    // receives every campaign. Delegate to Digia SDK here.
                    // DigiaSDK.getInstance().appState.set("active_campaign", model)
                }
                .build()
                .initialize()
    }
}
