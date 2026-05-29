package com.messenger.phone.number.text.sms.service.apps

import com.revenuecat.purchases.ui.revenuecatui.PaywallDialog
import com.revenuecat.purchases.ui.revenuecatui.PaywallDialogOptions

class TestPaywall {
    fun test() {
        val options = PaywallDialogOptions.Builder().build()
        PaywallDialog(options)
    }
}
