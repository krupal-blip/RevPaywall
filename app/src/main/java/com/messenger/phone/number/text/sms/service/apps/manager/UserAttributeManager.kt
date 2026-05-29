package com.messenger.phone.number.text.sms.service.apps.manager

import android.content.Context
import android.util.Log
import com.messenger.phone.number.text.sms.service.apps.manager.RevenueCatManager.TAG
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.syncAttributesAndOfferingsIfNeededWith
import androidx.core.content.edit

class UserAttributeManager(context: Context) {
    private val prefs = context.getSharedPreferences("focusflow_attributes", Context.MODE_PRIVATE)

    val preferredTheme: String
        get() = prefs.getString(KEY_PREFERRED_THEME, "study") ?: "study"

    val lastPlacement: String
        get() = prefs.getString(KEY_PAYWALL_SOURCE, "none") ?: "none"

    val lockedFeature: String
        get() = prefs.getString(KEY_LOCKED_FEATURE, "none") ?: "none"

    fun setPreferredTheme(theme: String) {
        val normalized = theme.lowercase()
        prefs.edit { putString(KEY_PREFERRED_THEME, normalized) }

        // RevenueCat custom attribute.
        // Used to demonstrate personalized onboarding targeting concepts.
        setRevenueCatAttributes(mapOf(KEY_PREFERRED_THEME to normalized))
    }

    fun setPaywallSource(source: String) {
        prefs.edit { putString(KEY_PAYWALL_SOURCE, source)}

        // Tracks from which app placement paywall was opened.
        // Helps analyze conversion source in RevenueCat experiments and dashboards.
        setRevenueCatAttributes(mapOf(KEY_PAYWALL_SOURCE to source))
    }

    fun setLockedFeature(feature: String) {
        prefs.edit { putString(KEY_LOCKED_FEATURE, feature)}

        // Feature-specific custom attribute.
        // Used when a locked feature creates high-intent purchase context.
        setRevenueCatAttributes(mapOf(KEY_LOCKED_FEATURE to feature))
    }

    fun setUserSegment(segment: String) {
        prefs.edit { putString(KEY_USER_SEGMENT, segment)}

        // Demo segment attribute.
        // Example values: new_user, power_user, returning_user.
        setRevenueCatAttributes(mapOf(KEY_USER_SEGMENT to segment))
    }

    fun currentAttributes(): Map<String, String> = mapOf(
        KEY_PREFERRED_THEME to preferredTheme,
        KEY_PAYWALL_SOURCE to lastPlacement,
        KEY_LOCKED_FEATURE to lockedFeature,
        KEY_USER_SEGMENT to (prefs.getString(KEY_USER_SEGMENT, "new_user") ?: "new_user"),
    )

    private fun setRevenueCatAttributes(attributes: Map<String, String>) {
        val ready=RevenueCatManager.isReady()
        Log.d(TAG, "setRevenueCatAttributes:ready=$ready\n${attributes}")

        if (ready) {
            Purchases.sharedInstance.setAttributes(attributes)
        }
    }

    companion object {
        const val KEY_PREFERRED_THEME = "preferred_theme"
        const val KEY_PAYWALL_SOURCE = "paywall_source"
        const val KEY_LOCKED_FEATURE = "locked_feature"
        const val KEY_USER_SEGMENT = "user_segment"
    }
}
