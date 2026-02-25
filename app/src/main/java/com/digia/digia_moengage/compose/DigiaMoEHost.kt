package com.digia.digia_moengage.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.digia.digiaui.app.DigiaUIWrapper

/**
 * Activity-level wrapper that surfaces **overlay** campaigns (Dialog, BottomSheet, Pip) on top of
 * every screen inside it — automatically, with zero per-screen setup.
 *
 * ## Campaign type ownership
 *
 * | Type | Rendered by | Who places it |
 * |-------------|-------------------------------|-------------------------------| | Dialog |
 * [DigiaUIManager.dialogManager] | This host (automatic) | | BottomSheet |
 * [DigiaUIManager.bottomSheetManager] | This host (automatic) | | Pip | PIP manager (future) | This
 * host (automatic) | | **Inline** | **[DigiaMoEInlineContent]** | **Host, at the desired screen
 * position** |
 *
 * [CampaignType.Inline] is **not** managed here. The host must place [DigiaMoEInlineContent]
 * explicitly at the exact composition-tree location where inline content should appear.
 *
 * ## Architecture
 *
 * ```
 * DigiaMoEHost
 *  └── DigiaUIWrapper          (Digia SDK dialog/sheet infrastructure)
 *       └── Box(fillMaxSize)
 *            ├── content()     (your entire nav graph)
 *            └── DigiaMoEViewBuilder
 *                 └── LaunchedEffect(visible) → DigiaUIManager.show/dismiss
 * ```
 *
 * ## Dismiss on context change
 *
 * [DigiaMoESDK.setContext] → [CampaignStore.setContext] auto-dismisses the active campaign when the
 * page ID changes. [DigiaMoEViewBuilder]'s [LaunchedEffect] reacts to `visible = null` and calls
 * `dismiss()` on every overlay UI layer.
 *
 * ## Usage
 *
 * ```kotlin
 * // Activity.setContent — once, wraps the entire nav graph:
 * DigiaMoEHost {
 *     MyAppNavHost()
 * }
 *
 * // Inside a screen where inline content should appear:
 * Column {
 *     HeroBanner()
 *     DigiaMoEInlineContent()
 *     ProductList()
 * }
 * ```
 *
 * @param content Your entire app content (nav host, screens, etc.).
 */
@Composable
fun DigiaMoEHost(
        content: @Composable () -> Unit,
) {
    DigiaUIWrapper {
        Box(modifier = Modifier.fillMaxSize()) {
            // App content — entire nav graph.
            content()

            // Overlay campaign renderer: Dialog, BottomSheet, Pip.
            // Inline campaigns are NOT handled here; use DigiaMoEInlineContent instead.
            DigiaMoEViewBuilder()
        }
    }
}
