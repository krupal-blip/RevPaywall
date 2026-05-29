package com.messenger.phone.number.text.sms.service.apps

import android.content.Context
import androidx.core.content.edit

class FirstLaunchStore(context: Context) {
    private val prefs = context.getSharedPreferences("focusflow_first_launch", Context.MODE_PRIVATE)

    val isOnboardingComplete: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)

    fun markOnboardingComplete() {
        prefs.edit { putBoolean(KEY_ONBOARDING_COMPLETE, true) }
    }

    companion object {
        private const val KEY_ONBOARDING_COMPLETE = "is_onboarding_complete"
    }
}
