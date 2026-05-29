package com.messenger.phone.number.text.sms.service.apps

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.messenger.phone.number.text.sms.service.apps.manager.PaywallManager
import com.messenger.phone.number.text.sms.service.apps.manager.UserAttributeManager
import android.widget.Toast

class PremiumFeaturesActivity : AppCompatActivity() {
    private lateinit var paywallManager: PaywallManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_premium_features)
        applyInsets()

        paywallManager = PaywallManager(this, UserAttributeManager(this))

        bindFeature(R.id.featureAiSuggestions, "ai_suggestions")
        bindFeature(R.id.featureDeepAnalytics, "deep_analytics")
        bindFeature(R.id.featureCloudSync, "cloud_sync")
        bindFeature(R.id.featurePremiumThemes, "premium_themes")

        findViewById<MaterialButton>(R.id.premiumBack).setOnClickListener { finish() }
    }

    private fun bindFeature(cardId: Int, featureName: String) {
        findViewById<MaterialCardView>(cardId).setOnClickListener {
            // Feature-specific contextual paywall.
            // Helps demonstrate intent-based monetization.
            paywallManager.showFeaturePaywall(this, featureName, onFail = {
                Toast.makeText(this, "Feature offering unavailable", Toast.LENGTH_SHORT).show()
            })
        }
    }

    private fun applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.premiumRoot)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
