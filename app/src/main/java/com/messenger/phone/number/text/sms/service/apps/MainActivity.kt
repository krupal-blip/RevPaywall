package com.messenger.phone.number.text.sms.service.apps

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // MainActivity is intentionally only a launcher/router.
        // SplashActivity owns first-launch routing so demo navigation is easy to explain.
        startActivity(Intent(this, SplashActivity::class.java))
        finish()
    }
}
