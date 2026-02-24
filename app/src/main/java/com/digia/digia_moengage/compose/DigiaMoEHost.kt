package com.digia.digia_moengage.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.digia.digiaui.app.DigiaUIWrapper

/**
 * Activity-level wrapper that surfaces Dialog, BottomSheet, and Pip campaigns **on top of every
 * screen** inside it — automatically, with zero per-screen setup.
 *
 * ## Architecture
 *
 * ```
 * DigiaMoEHost
 *  └── DigiaUIWrapper          (Digia SDK dialog/sheet infrastructure)
 *       └── Box(fillMaxSize)
 *            ├── content()     (your entire nav graph)
 *            └── DigiaMoEViewBuilder
 *                 ├── LaunchedEffect(visible) → DigiaUIManager.show/dismiss
 *                 └── DraggablePipWindow      → rendered inline for Pip type
 * ```
 *
 * ## Dismiss on context change
 *
 * [DigiaMoESDK.setContext] → [CampaignStore.setContext] auto-dismisses the active campaign when the
 * page ID changes. [DigiaMoEViewBuilder]'s [LaunchedEffect] reacts to `visible = null` and calls
 * `dismiss()` on every UI layer (dialog manager, sheet manager, PIP state).
 *
 * ## Usage
 *
 * ```kotlin
 * // In Activity.setContent — once, wraps everything:
 * DigiaMoEHost {
 *     MyAppNavHost()
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
            // ── App content (entire nav graph) ────────────────────────────
            content()

            // ── Campaign renderer ─────────────────────────────────────────
            // DigiaMoEViewBuilder observes CampaignStore reactively.
            // Dialog / BottomSheet → driven via DigiaUIManager (DialogHost /
            //   BottomSheetHost inside DigiaUIWrapper render them).
            // Pip  → DraggablePipWindow rendered inline here (Box sibling of
            //   content so touch pass-through works naturally).
            // Inline → DUIFactory.CreateComponent rendered inline.
            DigiaMoEViewBuilder()
        }
    }
}
