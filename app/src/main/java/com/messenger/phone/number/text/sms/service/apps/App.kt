package com.messenger.phone.number.text.sms.service.apps

import android.app.Application
import com.messenger.phone.number.text.sms.service.apps.manager.RevenueCatManager

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        RevenueCatManager.init(this)
    }
}
