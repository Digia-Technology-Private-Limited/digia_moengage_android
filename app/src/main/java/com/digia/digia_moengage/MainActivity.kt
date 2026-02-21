package com.digia.digia_moengage

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.digia.digia_moengage.config.DigiaMoEConfig
import com.digia.digia_moengage.contract.IDigiaMoEListener
import com.digia.digia_moengage.model.DigiaCampaignModel
import com.digia.digia_moengage.ui.theme.Digia_MoengageTheme
import com.moengage.core.MoEngage

/**
 * Demo host Activity showing how to wire [DigiaMoESDK] into a screen.
 *
 * **In production, move [DigiaMoESDK] initialisation to [Application.onCreate]** so MoEngage can
 * capture campaigns before any Activity launches.
 *
 * ## Initialisation sequence
 * 1. Build [DigiaMoEConfig] with your app credentials.
 * 2. (In a real app) initialise the Digia SDK **first** so its app-state is
 * ```
 *    ready before the first campaign arrives.
 * ```
 * 3. Call [DigiaMoESDK.Builder.build().initialize()] to boot MoEngage and
 * ```
 *    register the campaign observer.
 * ```
 * 4. Inside the `.renderer { }` lambda, push campaign data into the Digia
 * ```
 *    SDK app-state — the Digia UI will react automatically.
 * ```
 */
class MainActivity : ComponentActivity() {

    private val tag = "DigiaMoEDemo"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Shared configuration — one source of truth for both SDKs.
        val config =
                DigiaMoEConfig(
                        moEngageAppId = "YOUR_MOENGAGE_APP_ID", // ← replace
                        dataCenter = MoEngage.DataCenter.DATA_CENTER_1,
                        digiaAccessKey = "YOUR_DIGIA_ACCESS_KEY", // ← replace
                )

        // 2. TODO: Initialise the Digia SDK here (before MoEngage so app-state
        //    is ready when the first campaign arrives).
        //
        //    DigiaSDK.initialize(
        //        DigiaUIOptions(
        //            context    = this,
        //            accessKey  = config.digiaAccessKey,
        //            flavor     = Flavor.Release(),
        //        )
        //    )

        // 3. Boot MoEngage + register the campaign observer via the Facade.
        DigiaMoESDK.Builder(application, config)
                .listener(
                        object : IDigiaMoEListener {

                            override fun onMoEngageReady() {
                                // MoEngage SDK is live — safe to call any MoEngage API.
                                Log.d(tag, "MoEngage ready")
                            }

                            override fun onCampaignReceived(model: DigiaCampaignModel) {
                                // Forward the normalised campaign model into Digia SDK
                                // app-state so Digia UI components react automatically.
                                //
                                // DigiaSDK.getInstance().appState.set("campaign_id",      model.id)
                                // DigiaSDK.getInstance().appState.set("campaign_payload",
                                // model.payload)
                                Log.d(tag, "Campaign received: id=${model.id}")
                            }

                            override fun onError(source: String, error: Throwable) {
                                // Forward to your crash reporter (Firebase, Sentry, etc.)
                                Log.e(tag, "Integration error from $source", error)
                            }
                        }
                )
                .renderer { model ->
                    // Additional render hook — push campaign data into Digia here.
                    // This lambda is called every time a campaign arrives.
                    Log.d(tag, "Rendering campaign: ${model.id}")
                    // DigiaSDK.getInstance().appState.set("active_campaign", model)
                }
                .build()
                .initialize()

        // 4. Compose UI — replace with DUIFactory.getInstance().CreateNavHost()
        //    once the Digia SDK is fully initialised.
        enableEdgeToEdge()
        setContent {
            Digia_MoengageTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    IntegrationReadyScreen(
                            modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}

@Composable
private fun IntegrationReadyScreen(modifier: Modifier = Modifier) {
    Text(
            text = "Digia + MoEngage Integration Ready",
            modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun IntegrationReadyScreenPreview() {
    Digia_MoengageTheme { IntegrationReadyScreen() }
}
