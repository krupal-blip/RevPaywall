package com.messenger.phone.number.text.sms.service.apps

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        applyInsets()

        findViewById<View>(R.id.splashRoot).postDelayed({
            val nextActivity = if (FirstLaunchStore(this).isOnboardingComplete) {
                HomeActivity::class.java
            } else {
                OnboardingActivity::class.java
            }
            startActivity(Intent(this, nextActivity))
            finish()
        }, SPLASH_MS)
    }

    private fun applyInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splashRoot)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    companion object {
        private const val SPLASH_MS = 700L
    }
}
