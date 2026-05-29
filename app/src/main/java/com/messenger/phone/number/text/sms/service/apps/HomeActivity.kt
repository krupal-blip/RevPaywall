package com.messenger.phone.number.text.sms.service.apps

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.launch
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.messenger.phone.number.text.sms.service.apps.manager.PaywallManager
import com.messenger.phone.number.text.sms.service.apps.manager.RevenueCatManager.liveRevenueCatConfigCheck
import com.messenger.phone.number.text.sms.service.apps.manager.UserAttributeManager
import com.revenuecat.purchases.ui.revenuecatui.activity.PaywallResult
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.revenuecat.purchases.ui.revenuecatui.customercenter.CustomerCenter
import androidx.activity.OnBackPressedCallback
import com.google.android.material.composethemeadapter3.Mdc3Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class HomeActivity : AppCompatActivity() {
    private lateinit var paywallManager: PaywallManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        applyInsets()

        paywallManager = PaywallManager(this, UserAttributeManager(this), onResult = { result ->
            when (result) {
                is PaywallResult.Cancelled -> {
                    // user closed/dismissed paywall
                    Toast.makeText(
                        this,
                        "Cancelled/Close Paywall",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is PaywallResult.Purchased -> {
                    // purchase completed
//                    onPurchased(result.customerInfo)
                    Toast.makeText(
                        this,
                        "Purchased: ${result.customerInfo.entitlements.active.firstNotNullOf { it.value.store.name }}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is PaywallResult.Restored -> {
                    // restore completed
//                    onRestored(result.customerInfo)
                    Toast.makeText(
                        this,
                        "Restored: ${result.customerInfo.entitlements.active.firstNotNullOf { it.value.store.name }}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                is PaywallResult.Error -> {
                    // error occurred
                    Toast.makeText(this, "${result.error}", Toast.LENGTH_SHORT).show()
                }
            }
        })
        findViewById<MaterialButton>(R.id.homeGoPro).isEnabled = false
        liveRevenueCatConfigCheck {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(
                    this@HomeActivity,
                    "RevenueCat Is Configs Successfully!",
                    Toast.LENGTH_SHORT
                ).show()
                this@HomeActivity.findViewById<MaterialButton>(R.id.homeGoPro).isEnabled = true
            }

        }
        findViewById<MaterialButton>(R.id.btnCustomer).setOnClickListener {
            val rootLayout = findViewById<android.view.ViewGroup>(android.R.id.content)
            var composeView: ComposeView? = null

            val onBackPressedCallback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    rootLayout.removeView(composeView)
                    remove()
                }
            }
            onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

            composeView = ComposeView(this).apply {
                id = android.view.View.generateViewId()
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT
                )
                setContent {
                    Mdc3Theme {
                        CustomerCenter(
                            modifier = Modifier.fillMaxSize(),
                            onDismiss = {
                                onBackPressedCallback.remove()
                                rootLayout.removeView(this@apply)
                            }
                        )
                    }
                }
            }
            rootLayout.addView(composeView)
        }
        findViewById<MaterialButton>(R.id.homeGoPro).setOnClickListener {
            // Main monetization CTA.
            // Uses Experiment offering for demo purposes.
            paywallManager.showHomePaywall(this,{
                Toast.makeText(
                    application,
                    "Failed to fetch Offering",
                    Toast.LENGTH_SHORT
                ).show()
            })
        }
        findViewById<MaterialButton>(R.id.homePremium).setOnClickListener {
            startActivity(Intent(this, PremiumFeaturesActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.homeSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.homeRoot)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

}
