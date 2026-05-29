package com.messenger.phone.number.text.sms.service.apps.manager

import android.util.Log
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.customercenter.CustomerCenterListener
import com.revenuecat.purchases.customercenter.CustomerCenterManagementOption

 fun createCustomerCenterListener(): CustomerCenterListener {
    return object : CustomerCenterListener {

        // ✅ Feedback Survey Callback
        override fun onFeedbackSurveyCompleted(feedbackSurveyOptionId: String) {
            // Called when user completes a feedback survey
            // feedbackSurveyOptionId tells you which option they selected
            Log.d("CustomerCenter", "Feedback: $feedbackSurveyOptionId")
            // e.g., send to analytics, trigger a win-back offer, etc.
        }

        override fun onManagementOptionSelected(action: CustomerCenterManagementOption) {
            when (action) {
                CustomerCenterManagementOption.Cancel -> { /* user selected cancel */ }
                CustomerCenterManagementOption.MissingPurchase -> { /* missing purchase */ }
                is CustomerCenterManagementOption.CustomUrl -> { /* custom URL */ }
            }
        }

        override fun onRestoreStarted() { }
        override fun onRestoreCompleted(customerInfo: CustomerInfo) { }
        override fun onRestoreFailed(error: PurchasesError) { }
        override fun onShowingManageSubscriptions() { }
    }
}