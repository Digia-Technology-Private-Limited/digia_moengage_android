// package com.digia.digia_moengage
//
// import android.os.Bundle
// import androidx.activity.ComponentActivity
// import androidx.activity.compose.setContent
// import androidx.activity.enableEdgeToEdge
// import androidx.compose.foundation.layout.fillMaxSize
// import androidx.compose.foundation.layout.padding
// import androidx.compose.material3.Scaffold
// import androidx.compose.material3.Text
// import androidx.compose.ui.Modifier
// import com.digia.digia_moengage.compose.DigiaMoEContext
// import com.digia.digia_moengage.compose.DigiaMoEHost
// import com.digia.digia_moengage.model.CampaignType
// import com.digia.digia_moengage.ui.theme.Digia_MoengageTheme
//
/// **
// * Demo: how a host Activity wires DigiaMoESDK.
// *
// * In production:
// * - Both Digia and MoEngage are initialised in Application.onCreate().
// * - DigiaMoESDK.connect() is called in Application.onCreate().
// * - DigiaMoESDK.setContext() is called per-screen / per-nav-destination.
// * - pageContent lambda calls DigiaSDK.getInstance().host.CreatePage(...).
// *
// * ## All-screens overlay pattern
// *
// * DigiaMoEHost wraps the entire nav graph once at the Activity level.
// * Dialog, BottomSheet, and the draggable PIP overlay (OverlayVideo) will then
// * appear on top of EVERY screen without any per-screen setup.
// */
// class MainActivity : ComponentActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // ── STEP 1: Host initialises Digia SDK (async, network) ──────────────
//        // DigiaSDK.initialize(DigiaUIOptions(context = this, accessKey = "..."))
//
//        // ── STEP 2: Host initialises MoEngage (sync) ─────────────────────────
//        // MoEngage.initialiseDefaultInstance(
//        //     MoEngage.Builder(application, "YOUR_MOE_APP_ID", DataCenter.DATA_CENTER_1).build()
//        // )
//
//        // ── STEP 3: Connect the bridge (after BOTH SDKs are up) ──────────────
//        DigiaMoESDK.connect(app = application)
//
//        enableEdgeToEdge()
//        setContent {
//            Digia_MoengageTheme {
//                 Scaffold(modifier = Modifier.fillMaxSize()) { inner ->
//
//                    // ── STEP 5 + 6 combined: DigiaMoEHost wraps everything ────
//                    //
//                    // Dialog, BottomSheet, and the draggable PIP (OverlayVideo)
//                    // will appear on top of all screens automatically.
//                    // No need to add DigiaMoEViewBuilder inside each individual screen.
//                    DigiaMoEHost {
//                        // ── Your entire navigation graph goes here ────────────
//                        // DUIFactory.getInstance().CreateNavHost(null)
//                        Text(
//                                text = "Digia + MoEngage Ready",
//                                modifier = Modifier.padding(inner),
//                        )
//                    }
//                }
//            }
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        // ── STEP 4: Tell the bridge which screen is now active ────────────────
//        // This auto-dismisses any campaign from the previous screen and
//        // applies per-screen type filtering.
//        DigiaMoESDK.setContext(
//                DigiaMoEContext(
//                        currentPageId = "home",
//                        allowedTypes =
//                                CampaignType.all(), // or restrict: setOf(CampaignType.Inline)
//                )
//        )
//    }
// }
