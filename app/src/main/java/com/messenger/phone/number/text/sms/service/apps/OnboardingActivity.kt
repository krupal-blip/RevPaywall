package com.messenger.phone.number.text.sms.service.apps

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.messenger.phone.number.text.sms.service.apps.manager.PaywallManager
import com.messenger.phone.number.text.sms.service.apps.manager.RevenueCatManager.TAG
import com.messenger.phone.number.text.sms.service.apps.manager.RevenueCatManager.liveRevenueCatConfigCheck
import com.messenger.phone.number.text.sms.service.apps.manager.UserAttributeManager
import com.revenuecat.purchases.ui.revenuecatui.activity.PaywallResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.content.edit
import com.messenger.phone.number.text.sms.service.apps.manager.RevenueCatManager.applyLanguage
import java.util.Locale

class OnboardingActivity : AppCompatActivity() {
    private lateinit var attributes: UserAttributeManager
    private lateinit var paywallManager: PaywallManager
    private lateinit var styleButtons: List<MaterialButton>
    private lateinit var status: TextView
    private var selectedStyle = "general"
    private var isSpinnerInitialized = false

    val languages = listOf(
        "English",
        "French",
        "German",
        "Dutch",
        "Swedish",
        "Danish",
        "Finnish",
        "Japanese",
        "Korean",
        "Hindi",
        "Indonesian",
        "Vietnamese",
        "Thai",
        "Malay",
        "Portuguese",
        "Spanish",
        "Turkish",
        "Arabic",
        "Polish",
        "Romanian",
        "Ukrainian"
    )

    val localeCodes = listOf(
        "en", "fr", "de", "nl", "sv", "da", "fi",
        "ja", "ko", "hi", "id", "vi", "th", "ms",
        "pt", "es", "tr", "ar", "pl", "ro", "uk"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_onboarding)
        applyInsets()

        attributes = UserAttributeManager(this)
        paywallManager = PaywallManager(this, attributes, onResult = { result ->
            when (result) {
                is PaywallResult.Cancelled -> {
                    // user closed/dismissed paywall
                    Toast.makeText(
                        application,
                        "Cancelled/Close Paywall",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }

                is PaywallResult.Purchased -> {
                    // purchase completed
//                    onPurchased(result.customerInfo)
                    Toast.makeText(
                        application,
                        "Purchased: ${result.customerInfo.entitlements.active.firstNotNullOf { it.value.store.name }}",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }

                is PaywallResult.Restored -> {
                    // restore completed
//                    onRestored(result.customerInfo)
                    Toast.makeText(
                        application,
                        "Restored: ${result.customerInfo.entitlements.active.firstNotNullOf { it.value.store.name }}",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }

                is PaywallResult.Error -> {
                    // error occurred
                    Toast.makeText(application, "${result.error}", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
            }
        })
        selectedStyle = attributes.preferredTheme
        if (selectedStyle != "study" && selectedStyle != "work") selectedStyle = "study"

        status = findViewById(R.id.onboardingStatus)
        styleButtons = listOf(
            findViewById(R.id.styleStudy),
            findViewById(R.id.styleWork),
        )

        styleButtons.forEach { button ->
            button.setOnClickListener {
                selectedStyle = button.tag.toString().lowercase()
                updateSelectedStyle()
            }
            button.isEnabled = false
        }
        findViewById<MaterialButton>(R.id.finishOnboarding).isEnabled = false
        updateSelectedStyle()
        liveRevenueCatConfigCheck {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(
                    this@OnboardingActivity,
                    "RevenueCat Is Configs Successfully!",
                    Toast.LENGTH_SHORT
                ).show()
                status.text = "RevenueCat Is Configs Successfully!"
                this@OnboardingActivity.apply {
                    styleButtons.forEach { button ->
                        button.isEnabled = true
                    }
                    findViewById<MaterialButton>(R.id.finishOnboarding).isEnabled = true
                }
            }

        }
        findViewById<MaterialButton>(R.id.finishOnboarding).setOnClickListener {
            // RevenueCat custom attribute.
            // Used to demonstrate personalized targeting concepts.

            attributes.setUserSegment("new_user")
            FirstLaunchStore(this).markOnboardingComplete()
            status.text = "Saved preferred_theme=$selectedStyle. Opening RevenueCatUI paywall."
            paywallManager.showOnboardingPaywall(this, {
                Toast.makeText(
                    application,
                    "Failed to fetch Offering",
                    Toast.LENGTH_SHORT
                ).show()
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            })

        }

        val spinner = findViewById<Spinner>(R.id.languageSpinner)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            languages
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (!isSpinnerInitialized) {
                    isSpinnerInitialized = true
                    return
                }

                val selectedLocaleCode = localeCodes[position]

                val prefs = getSharedPreferences("settings", MODE_PRIVATE)
                val currentLocaleCode = prefs.getString("language_code", "en") ?: "en"

                if (selectedLocaleCode == currentLocaleCode) return

                prefs.edit()
                    .putString("language_code", selectedLocaleCode)
                    .apply()

                setLocale(selectedLocaleCode)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setLocale(languageCode: String) {
        val currentCode = resources.configuration.locales[0].language

        if (currentCode == languageCode) return

        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(resources.configuration)
        config.setLocale(locale)

        baseContext.createConfigurationContext(config)
        applyLanguage(languageCode)
        recreate()
    }

    private fun updateSelectedStyle() {
        styleButtons.forEach { button ->
            val selected = button.tag == selectedStyle
            button.alpha = if (selected) 1f else 0.62f
            button.strokeWidth = if (selected) dp(2) else dp(1)
            Log.d(TAG, "setPreferredTheme: ${selectedStyle}")

            attributes.setPreferredTheme(selectedStyle)
        }
    }

    //    override fun attachBaseContext(newBase: Context) {
//
//        val prefs = newBase.getSharedPreferences("settings", MODE_PRIVATE)
//        val languageCode = prefs.getString("language_code", "en") ?: "en"
//
//        val locale = Locale(languageCode)
//        Locale.setDefault(locale)
//
//        val config = Configuration(newBase.resources.configuration)
//        config.setLocale(locale)
//
//        val context = newBase.createConfigurationContext(config)
//
//        super.attachBaseContext(context)
//    }
    private fun applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.onboardingRoot)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
