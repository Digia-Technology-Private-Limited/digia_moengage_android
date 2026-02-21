package com.digia.digia_moengage

import android.app.Application
import com.digia.digia_moengage.config.DigiaMoEConfig
import com.digia.digia_moengage.contract.ICampaignRenderer
import com.digia.digia_moengage.contract.IDigiaMoEListener
import com.digia.digia_moengage.model.DigiaCampaignModel

/**
 * **Single entry-point for the Digia + MoEngage integration.**
 *
 * ## Design patterns used | Pattern | Where |
 * |--------------------|------------------------------------------------| | **Facade** | One
 * `initialize()` call boots both SDKs | | **Builder** | Fluent [Builder] keeps the public API clean
 * | | **Observer** | [MoECampaignObserver] listens for campaigns | | **Strategy / DIP** |
 * [ICampaignRenderer] & [IDigiaMoEListener] are | | | injected abstractions, not concrete classes |
 * | **Null-Object** | [NoOpListener] / [NoOpRenderer] prevent NPE | | | when the host omits
 * optional callbacks |
 *
 * ## Lifecycle Initialise **once** in [android.app.Application.onCreate] so MoEngage can capture
 * campaigns launched before the first Activity opens.
 *
 * ## Minimal usage
 * ```kotlin
 * // Application.onCreate()
 * DigiaMoESDK.Builder(this, DigiaMoEConfig(
 *     moEngageAppId  = "YOUR_MOE_APP_ID",
 *     digiaAccessKey = "YOUR_DIGIA_KEY",
 * )).build().initialize()
 * ```
 *
 * ## Full usage
 * ```kotlin
 * DigiaMoESDK.Builder(this, config)
 *     .listener(object : IDigiaMoEListener {
 *         override fun onMoEngageReady()                          { /* SDK live */ }
 *         override fun onCampaignReceived(model: DigiaCampaignModel) {
 *             // Push into Digia SDK:
 *             DigiaSDK.getInstance().appState.set("campaign_id",      model.id)
 *             DigiaSDK.getInstance().appState.set("campaign_payload", model.payload)
 *         }
 *         override fun onError(source: String, error: Throwable) { /* log it */ }
 *     })
 *     .renderer { model ->
 *         // Alternative / additional rendering hook
 *         DigiaSDK.getInstance().appState.set("campaign", model)
 *     }
 *     .build()
 *     .initialize()
 * ```
 */
class DigiaMoESDK
private constructor(
        private val app: Application,
        private val config: DigiaMoEConfig,
        private val listener: IDigiaMoEListener,
        private val renderer: ICampaignRenderer,
        /**
         * Suspend guard that blocks campaign processing until Digia is ready.
         *
         * MoEngage is synchronously ready after [MoEInitializer.initialize], but Digia performs an
         * async network fetch. A campaign can arrive from MoEngage before that fetch completes,
         * causing a race. This lambda is called inside [CampaignProcessor.process] and should
         * suspend until the Digia SDK signals full readiness — e.g. [DigiaSDK.ensureInitialized()].
         *
         * **Default is a no-op** so the SDK compiles without the Digia library, but the host should
         * always wire the real guard via [Builder.digiaReadyGuard].
         */
        private val digiaReadyGuard: suspend () -> Unit,
) {

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Initialises MoEngage and registers the self-handled in-app campaign observer.
     *
     * Call this **once** from [Application.onCreate]. Calling it multiple times is safe —
     * subsequent calls are no-ops for MoEngage (it guards internally), though the observer will
     * re-register.
     */
    fun initialize() {
        bootMoEngage()
        registerCampaignObserver()
    }

    // ── Initialisation steps — each has exactly one job (SRP) ────────────────

    private fun bootMoEngage() {
        try {
            MoEInitializer.initialize(app, config)
            listener.onMoEngageReady()
        } catch (e: Exception) {
            listener.onError(SOURCE_MOENGAGE, e)
        }
    }

    private fun registerCampaignObserver() {
        try {
            val processor = CampaignProcessor(renderer, listener, digiaReadyGuard)
            MoECampaignObserver(processor).register(app)
        } catch (e: Exception) {
            listener.onError(SOURCE_CAMPAIGN_OBSERVER, e)
        }
    }

    // ── Builder ───────────────────────────────────────────────────────────────

    /**
     * Fluent builder for [DigiaMoESDK].
     *
     * Both [listener] and [renderer] are optional; omitting them installs silent no-op defaults so
     * the integration still works without callbacks.
     */
    class Builder(
            private val app: Application,
            private val config: DigiaMoEConfig,
    ) {
        private var listener: IDigiaMoEListener = NoOpListener
        private var renderer: ICampaignRenderer = NoOpRenderer
        private var digiaReadyGuard: suspend () -> Unit = {}

        /**
         * Provide the suspend function that blocks campaign delivery until the Digia SDK has
         * finished its async network initialisation.
         *
         * **Always wire this** when using the Digia SDK to prevent the race where a MoEngage
         * campaign arrives before Digia's config is fetched:
         * ```kotlin
         * .digiaReadyGuard { DigiaSDK.ensureInitialized() }
         * ```
         */
        fun digiaReadyGuard(guard: suspend () -> Unit): Builder = apply {
            this.digiaReadyGuard = guard
        }

        /**
         * Attach a host-side event listener.
         * @see IDigiaMoEListener
         */
        fun listener(listener: IDigiaMoEListener): Builder = apply { this.listener = listener }

        /**
         * Supply a lambda that receives every Digia campaign model. Use this to push campaign data
         * into [DigiaSDK.getInstance().appState].
         */
        fun renderer(onRender: (DigiaCampaignModel) -> Unit): Builder = apply {
            this.renderer = DigiaRenderer(onRender)
        }

        /** Supply a full [ICampaignRenderer] implementation, e.g. for unit testing. */
        fun renderer(renderer: ICampaignRenderer): Builder = apply { this.renderer = renderer }

        /** Builds the configured [DigiaMoESDK] instance. */
        fun build(): DigiaMoESDK = DigiaMoESDK(app, config, listener, renderer, digiaReadyGuard)
    }

    // ── Null-Object defaults ──────────────────────────────────────────────────

    /** Silent no-op listener: prevents NPE when the host omits a listener. */
    private object NoOpListener : IDigiaMoEListener

    /** Silent no-op renderer: prevents NPE when the host omits a renderer. */
    private object NoOpRenderer : ICampaignRenderer {
        override fun render(model: DigiaCampaignModel) = Unit
    }

    // ── Constants ─────────────────────────────────────────────────────────────

    private companion object {
        const val SOURCE_MOENGAGE = "MoEngage"
        const val SOURCE_CAMPAIGN_OBSERVER = "CampaignObserver"
    }
}
