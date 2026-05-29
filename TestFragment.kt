package com.messenger.phone.number.text.sms.service.apps

import androidx.appcompat.app.AppCompatActivity
import com.revenuecat.purchases.ui.revenuecatui.fragment.PaywallFragment
import com.revenuecat.purchases.Offering

class TestFragment : AppCompatActivity() {
    fun test(offering: Offering) {
        val fragment = PaywallFragment.newInstance(offering)
    }
}
