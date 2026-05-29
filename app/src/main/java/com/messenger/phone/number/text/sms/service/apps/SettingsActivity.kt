package com.messenger.phone.number.text.sms.service.apps

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.messenger.phone.number.text.sms.service.apps.manager.PaywallManager
import com.messenger.phone.number.text.sms.service.apps.manager.RevenueCatManager
import com.messenger.phone.number.text.sms.service.apps.manager.UserAttributeManager

class SettingsActivity : AppCompatActivity() {
    private lateinit var attributes: UserAttributeManager
    private lateinit var paywallManager: PaywallManager
    private lateinit var debugStatus: TextView
    private lateinit var offeringsStatus: TextView
    private lateinit var personaStatus: TextView
    private lateinit var subscriptionStatus: TextView
    private lateinit var placementStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        applyInsets()

        attributes = UserAttributeManager(this)
        paywallManager = PaywallManager(this, attributes)
        debugStatus = findViewById(R.id.settingsStatus)
        offeringsStatus = findViewById(R.id.offeringsStatus)
        personaStatus = findViewById(R.id.personaStatus)
        subscriptionStatus = findViewById(R.id.subscriptionStatus)
        placementStatus = findViewById(R.id.placementStatus)

        findViewById<MaterialButton>(R.id.settingsUpgrade).setOnClickListener {
            paywallManager.showSettingsPaywall(this,{
                Toast.makeText(
                    application,
                    "Failed to fetch Offering",
                    Toast.LENGTH_SHORT
                ).show()
            })
            refreshDebugSection()
        }
        findViewById<MaterialButton>(R.id.restorePurchase).setOnClickListener {
            RevenueCatManager.restorePurchases {
                debugStatus.text = it
                refreshDebugSection()
            }
        }
        findViewById<MaterialButton>(R.id.checkEntitlement).setOnClickListener {
            RevenueCatManager.isProUser { isPro ->
                debugStatus.text = if (isPro) "Entitlement active" else "No active entitlement"
                refreshDebugSection()
            }
        }
        findViewById<MaterialButton>(R.id.settingsBack).setOnClickListener { finish() }

        refreshDebugSection()
    }

    private fun refreshDebugSection() {
        personaStatus.text = attributes.preferredTheme.replaceFirstChar { it.uppercase() }
        placementStatus.text = attributes.lastPlacement
        subscriptionStatus.text = RevenueCatManager.subscriptionStatus()
        RevenueCatManager.getOfferingsDebugText { offeringsStatus.text = it }
    }

    private fun applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settingsRoot)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
