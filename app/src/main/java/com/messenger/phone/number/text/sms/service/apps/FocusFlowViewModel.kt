package com.messenger.phone.number.text.sms.service.apps

import androidx.lifecycle.ViewModel

class FocusFlowViewModel : ViewModel() {
    val todayFocusTime = "2h 35m"
    val taskCount = 7
    val completedHabits = 4

    val premiumFeatures = listOf(
        PremiumFeature("AI Suggestions", "Smart plans for your next focus block", "ai_feature"),
        PremiumFeature("Deep Analytics", "Trends, streaks, and weekly focus patterns", "analytics_feature"),
        PremiumFeature("Cloud Sync", "Keep sessions backed up across devices", "cloud_sync"),
        PremiumFeature("Premium Themes", "Switch focus modes with polished themes", "premium_themes"),
    )
}

data class PremiumFeature(
    val title: String,
    val subtitle: String,
    val source: String,
)
