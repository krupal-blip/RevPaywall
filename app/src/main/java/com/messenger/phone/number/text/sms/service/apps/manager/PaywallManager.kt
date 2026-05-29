package com.messenger.phone.number.text.sms.service.apps.manager

import android.app.Activity
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Shape
import com.revenuecat.purchases.Offering
import com.revenuecat.purchases.ui.revenuecatui.CustomVariableValue
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.models.StoreTransaction
import com.revenuecat.purchases.ui.revenuecatui.activity.PaywallActivityLauncher
import com.revenuecat.purchases.ui.revenuecatui.activity.PaywallResult
import com.revenuecat.purchases.ui.revenuecatui.activity.PaywallResultHandler
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.children
import com.messenger.phone.number.text.sms.service.apps.manager.RevenueCatManager.TAG
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.material3.CardDefaults
import com.revenuecat.purchases.ui.revenuecatui.Paywall
import com.revenuecat.purchases.ui.revenuecatui.PaywallOptions
import com.revenuecat.purchases.ui.revenuecatui.PaywallListener

class PaywallManager(
    private val activity: ComponentActivity,
    private val attributes: UserAttributeManager,
    private val onResult: (PaywallResult) -> Unit = {},
) {
    private val launcher = PaywallActivityLauncher(activity, object : PaywallResultHandler {
        override fun onActivityResult(result: PaywallResult) {
            onResult(result)
        }
    })

    fun showOnboardingPaywall(activity: Activity, onFail: () -> Unit) {
        attributes.setPaywallSource(PLACEMENT_ONBOARDING_OFFER)

        RevenueCatManager.handlePaywallLaunch(
            activity,
            onMobile = {
                RevenueCatManager.getCurrentOfferingForPlacement(
                    placementId = PLACEMENT_ONBOARDING_OFFER,
                    onResult = { offering ->
                        launchRevenueCatUi(
                            offering = offering,
                            source = PLACEMENT_ONBOARDING_OFFER,
                            extraVariables = mapOf(),
                            onFail = onFail
                        )
                    },
                    onError = { toast(activity, "Opening default RevenueCatUI paywall") }
                )
            },
            onLargeDevice = {
                RevenueCatManager.getCurrentOfferingForPlacement(
                    placementId = PLACEMENT_ONBOARDING_OFFER,
                    onResult = { offering ->
                        launchRevenueCatDialogUi(
                            activity,
                            offering,
                            PLACEMENT_ONBOARDING_OFFER,
                            mapOf(),
                            onFail
                        )
                    },
                    onError = { toast(activity, "Opening default RevenueCatUI paywall") }
                )
            }
        )
    }

    fun showHomePaywall(activity: Activity, onFail: () -> Unit) {
        attributes.setPaywallSource(PLACEMENT_HOME_PRO)

        RevenueCatManager.handlePaywallLaunch(
            activity,
            onMobile = {
                RevenueCatManager.getCurrentOfferingForPlacement(
                    placementId = PLACEMENT_HOME_PRO,
                    onResult = { offering ->
                        launchRevenueCatUi(offering, PLACEMENT_HOME_PRO, onFail = onFail)
                    },
                    onError = { toast(activity, "Home placement offering unavailable") }
                )
            },
            onLargeDevice = {
                RevenueCatManager.getCurrentOfferingForPlacement(
                    placementId = PLACEMENT_HOME_PRO,
                    onResult = { offering ->
                        launchRevenueCatDialogUi(
                            activity,
                            offering,
                            PLACEMENT_HOME_PRO,
                            emptyMap(),
                            onFail
                        )
                    },
                    onError = { toast(activity, "Home placement offering unavailable") }
                )
            }
        )
    }

    fun showSettingsPaywall(activity: Activity, onFail: () -> Unit) {
        attributes.setPaywallSource(PLACEMENT_SETTINGS)

        // Secondary upgrade placement.
        // Demonstrates alternate paywall entry point driven by RevenueCatUI.
        RevenueCatManager.getCurrentOfferingForPlacement(
            placementId = PLACEMENT_SETTINGS,
            onResult = { offering ->
                launchRevenueCatUi(
                    offering,
                    PLACEMENT_SETTINGS,
                    onFail = onFail
                )
            },
            onError = { toast(activity, "Settings placement offering unavailable") }
        )
    }

    fun showFeaturePaywall(activity: Activity, featureName: String, onFail: () -> Unit) {
        attributes.setPaywallSource(PLACEMENT_HOME_PRO)

        RevenueCatManager.handlePaywallLaunch(
            activity,
            onMobile = {
                RevenueCatManager.getCurrentOfferingForPlacement(
                    placementId = PLACEMENT_HOME_PRO,
                    onResult = { offering ->
                        val extra = mapOf("feature_name" to CustomVariableValue.String(featureName))
                        launchRevenueCatUi(
                            offering,
                            PLACEMENT_HOME_PRO,
                            extraVariables = extra,
                            onFail = onFail
                        )
                    },
                    onError = { toast(activity, "Feature placement offering unavailable") }
                )
            },
            onLargeDevice = {
                RevenueCatManager.getCurrentOfferingForPlacement(
                    placementId = PLACEMENT_HOME_PRO,
                    onResult = { offering ->
                        val extra = mapOf("feature_name" to CustomVariableValue.String(featureName))
                        launchRevenueCatDialogUi(
                            activity,
                            offering,
                            PLACEMENT_HOME_PRO,
                            extraVariables = extra,
                            onFail = onFail
                        )
                    },
                    onError = { toast(activity, "Feature placement offering unavailable") }
                )
            }
        )
    }

    private fun launchRevenueCatUi(
        offering: Offering?,
        source: String,
        extraVariables: Map<String, CustomVariableValue> = emptyMap(), onFail: () -> Unit
    ) {
        // We set placement attributes before opening paywall so conversion sources can later be analyzed.
        // The paywall UI itself is not implemented in-app; RevenueCatUI renders the full purchase screen.
        val variables = extraVariables + mapOf(
            /* "paywall_source" to CustomVariableValue.String(source),*/"preferred_theme" to CustomVariableValue.String(
                attributes.preferredTheme
            )
        )
        if (offering != null) {
            launcher.launch(offering = offering, customVariables = variables)
        } else {
            onFail()
        }
    }

    private fun launchRevenueCatDialogUi(
        activity: Activity,
        offering: Offering?,
        source: String,
        extraVariables: Map<String, CustomVariableValue> = emptyMap(),
        onFail: () -> Unit
    ) {
        if (offering != null) {
            val rootLayout = activity.findViewById<android.view.ViewGroup>(android.R.id.content)
            var composeView: ComposeView? = null

            // Register onBackPressedCallback on the ComponentActivity to intercept back press
            val onBackPressedCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    onResult(PaywallResult.Cancelled)
                    rootLayout.removeView(composeView)
                    remove()
                }
            }
            this.activity.onBackPressedDispatcher.addCallback(this.activity, onBackPressedCallback)

            composeView = ComposeView(activity).apply {
                id = android.view.View.generateViewId()

                // Set layout params to MATCH_PARENT to display full-screen translucent dimmed overlay
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                )

                setContent {
                    val displayMetrics = activity.resources.displayMetrics
                    val screenWidth = displayMetrics.widthPixels
                    val screenHeight = displayMetrics.heightPixels

                    // Calculate target height: 85% of screen height, capped at a maximum of 850dp
                    val maxTargetHeight = (displayMetrics.density * 850).toInt()
                    val targetHeight = minOf((screenHeight * 0.85f).toInt(), maxTargetHeight)

                    // Calculate target width using standard 9:16 mobile aspect ratio
                    val targetWidth = (targetHeight * 9f / 16f).toInt()

                    // Ensure width doesn't exceed 85% of screen width
                    val finalWidth = minOf(targetWidth, (screenWidth * 0.85f).toInt())

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onResult(PaywallResult.Cancelled)
                                onBackPressedCallback.remove()
                                rootLayout.removeView(composeView)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            modifier = Modifier
                                .size(
                                    width = with(LocalDensity.current) { finalWidth.toDp() },
                                    height = with(LocalDensity.current) { targetHeight.toDp() }
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    // Prevent click propagation to background dim overlay
                                }
                        ) {
                            Paywall(
                                options = PaywallOptions.Builder(dismissRequest = {
                                    onResult(PaywallResult.Cancelled)
                                    onBackPressedCallback.remove()
                                    rootLayout.removeView(composeView)
                                })
                                    .setOffering(offering)
                                    .setListener(
                                        object : PaywallListener {
                                            override fun onPurchaseCompleted(
                                                customerInfo: CustomerInfo,
                                                storeTransaction: StoreTransaction
                                            ) {
                                                onResult(PaywallResult.Purchased(customerInfo))
                                                onBackPressedCallback.remove()
                                                rootLayout.removeView(composeView)
                                            }

                                            override fun onRestoreCompleted(customerInfo: CustomerInfo) {
                                                onResult(PaywallResult.Restored(customerInfo))
                                                onBackPressedCallback.remove()
                                                rootLayout.removeView(composeView)
                                            }

                                            override fun onPurchaseError(error: com.revenuecat.purchases.PurchasesError) {
                                                onResult(PaywallResult.Error(error))
                                            }

                                            override fun onRestoreError(error: com.revenuecat.purchases.PurchasesError) {
                                                onResult(PaywallResult.Error(error))
                                            }

                                            override fun onPurchaseCancelled() {
                                                onResult(PaywallResult.Cancelled)
                                                onBackPressedCallback.remove()
                                                rootLayout.removeView(composeView)
                                            }
                                        }
                                    )
                                    .build()
                            )
                        }
                    }
                }
            }
            rootLayout.addView(composeView)
            Log.d(TAG, "rootLayout: ${rootLayout.children.map { it.id }}")

        } else {
            onFail()
        }
    }

    private fun onboardingHeadline(): String = when (attributes.preferredTheme) {
        "study" -> "Unlock Deep Study Analytics"
        "work" -> "Boost Daily Work Efficiency"
        "fitness" -> "Track Consistency Like a Pro"
        else -> "Build Better Focus Habits"
    }

    private fun toast(activity: Activity, message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val PLACEMENT_ONBOARDING_OFFER = "onboarding_offer"
        const val PLACEMENT_HOME_PRO = "home_pro"
        const val PLACEMENT_SETTINGS = "settings_upgrade"
    }
}
