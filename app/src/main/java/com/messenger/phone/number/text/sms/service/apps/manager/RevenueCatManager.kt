package com.messenger.phone.number.text.sms.service.apps.manager

import android.content.Context
import android.util.Log
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Offering
import com.revenuecat.purchases.Offerings
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.interfaces.ReceiveOfferingsCallback
import com.revenuecat.purchases.interfaces.SyncAttributesAndOfferingsCallback
import com.revenuecat.purchases.syncAttributesAndOfferingsIfNeededWith
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object RevenueCatManager {
    private const val REVENUECAT_API_KEY = "goog_vwffVeAqEsqRtnAPZwaXudfQnHV"
    val TAG = "RevenueCatManager"
    private var latestCustomerInfo: CustomerInfo? = null

    // Central RevenueCat handler.
    // Keeps billing, offerings, restore, and entitlement logic separated from UI screens.
    fun init(context: Context) {
        if (Purchases.isConfigured) return

        Purchases.logLevel = LogLevel.DEBUG
        Purchases.configure(
            PurchasesConfiguration.Builder(context.applicationContext, REVENUECAT_API_KEY)
                .build()
        )
        Purchases.sharedInstance.customerCenterListener = createCustomerCenterListener()

        // Fetch customer info on start to cache user entitlement status
        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                latestCustomerInfo = customerInfo
                Log.d(TAG, "CustomerInfo initialized successfully: ${subscriptionStatus()}")
            }

            override fun onError(error: PurchasesError) {
                Log.e(TAG, "Failed to initialize CustomerInfo: ${error.message}")
            }
        })
    }

    fun liveRevenueCatConfigCheck(action: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                // check condition
                if (isReady()) {
                    action()
//                    prefetchOfferings()
                    break // stop timer
                }
                delay(1000) // 1 second
            }
        }
    }

    private var cachedOfferings: Offerings? = null

    // Call this as early as possible (e.g., in App or SplashActivity)
    fun prefetchOfferings() {
        if (!isReady()) return

        Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
            override fun onReceived(offerings: Offerings) {
                cachedOfferings = offerings
                Log.d(TAG, "Offerings prefetched and cached successfully.")
            }

            override fun onError(error: PurchasesError) {
                Log.e(TAG, "Prefetch failed: ${error.message}")
            }
        })
    }
    fun applyLanguage(code: String) {
        if (!isReady()) return
        Purchases.sharedInstance.overridePreferredUILocale(code)
    }
    // New optimized function that checks cache first
    fun getCachedOfferingForPlacement(placementId: String): Offering? {
        return cachedOfferings?.getCurrentOfferingForPlacement(placementId)
    }

    // Fallback: If cache is empty, fetch fresh
    fun getOffering(placementId: String, onResult: (Offering?) -> Unit) {
        val cached = getCachedOfferingForPlacement(placementId)
        Log.e(TAG, "getOffering $placementId: cache available: ${cached != null}")
        if (cached != null) {
            onResult(cached)
        } else {
            // Fallback to network if cache missed
            Purchases.sharedInstance.syncAttributesAndOfferingsIfNeededWith(onError = {
                onResult(null)
            }, { offerings ->
                cachedOfferings = offerings
                onResult(offerings.getCurrentOfferingForPlacement(placementId))
            })


        }
    }


    fun getCurrentOffering(onResult: (Offering?) -> Unit, onError: (PurchasesError) -> Unit = {}) {
        if (!isReady()) {
            onResult(null)
            return
        }

        Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
            override fun onReceived(offerings: com.revenuecat.purchases.Offerings) {
                onResult(offerings.current)
            }

            override fun onError(error: PurchasesError) {
                onError(error)
                onResult(null)
            }
        })
    }

    fun getCurrentOfferingForPlacement(
        placementId: String,
        onResult: (Offering?) -> Unit,
        onError: (PurchasesError) -> Unit = {},
    ) {
        if (!isReady()) {
            onResult(null)
            return
        }
//        getOffering(placementId, { offering ->
//            if (offering == null) {
        Purchases.sharedInstance.syncAttributesAndOfferingsIfNeeded(callback = object :
            SyncAttributesAndOfferingsCallback {
            override fun onError(error: PurchasesError) {
                onError(error)
                onResult(null)
            }

            override fun onSuccess(offerings: Offerings) {
                Log.d(TAG, "onReceived: ${offerings.all.map { it.key }}")

                val offer = offerings.getCurrentOfferingForPlacement(placementId)
                Log.d(
                    TAG,
                    "onReceived getCurrentOfferingForPlacement:$placementId ${offer?.identifier}"
                )
                onResult(offer)
            }

        })
//                return@getOffering
//            }
//            Log.d(TAG, "cached Offerings: ${offering.identifier}")
//
//            onResult(offering)
//        })

        /*Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
            override fun onReceived(offerings: com.revenuecat.purchases.Offerings) {
                Log.d(TAG, "onReceived: ${offerings.all.map { it.key }}")

                val offer = offerings.getCurrentOfferingForPlacement(placementId)
                Log.d(TAG, "onReceived getCurrentOfferingForPlacement:$placementId ${offer?.identifier}")
                onResult(offer)
            }

            override fun onError(error: PurchasesError) {
                onError(error)
                onResult(null)
            }
        })*/
    }

    fun getOfferingById(
        offeringId: String,
        onResult: (Offering?) -> Unit,
        onError: (PurchasesError) -> Unit = {},
    ) {
        if (!isReady()) {
            onResult(null)
            return
        }

        Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
            override fun onReceived(offerings: com.revenuecat.purchases.Offerings) {
                onResult(offerings[offeringId] ?: offerings.current)
            }

            override fun onError(error: PurchasesError) {
                onError(error)
                onResult(null)
            }
        })
    }

    fun getOfferingsDebugText(onResult: (String) -> Unit) {
        if (!isReady()) {
            onResult("RevenueCat not configured")
            return
        }

        Purchases.sharedInstance.getOfferings(object : ReceiveOfferingsCallback {
            override fun onReceived(offerings: com.revenuecat.purchases.Offerings) {
                val current = offerings.current?.identifier ?: "none"
                val all = offerings.all.keys.joinToString().ifBlank { "none" }
                onResult("Current: $current\nAvailable: $all")
            }

            override fun onError(error: PurchasesError) {
                onResult("Offerings unavailable: ${error.message}")
            }
        })
    }

    fun restorePurchases(onResult: (String) -> Unit) {
        if (!isReady()) {
            onResult("RevenueCat not configured")
            return
        }

        Purchases.sharedInstance.restorePurchases(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                latestCustomerInfo = customerInfo
                onResult("Restore complete. ${subscriptionStatus()}")
            }

            override fun onError(error: PurchasesError) {
                onResult("Restore failed: ${error.message}")
            }
        })
    }

    fun isProUser(onResult: (Boolean) -> Unit) {
        if (!isReady()) {
            onResult(false)
            return
        }

        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                latestCustomerInfo = customerInfo
                onResult(customerInfo.entitlements.active.isNotEmpty())
            }

            override fun onError(error: PurchasesError) {
                onResult(false)
            }
        })
    }

    fun subscriptionStatus(): String {
        val entitlementIds = latestCustomerInfo?.entitlements?.active?.keys.orEmpty()
        return if (entitlementIds.isEmpty()) "Free" else "Pro: ${entitlementIds.joinToString()}"
    }

    fun isLargeDevice(context: Context): Boolean {
        val configuration = context.resources.configuration
        val screenLayout = configuration.screenLayout and android.content.res.Configuration.SCREENLAYOUT_SIZE_MASK
        val isLargeScreen = screenLayout == android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE ||
                screenLayout == android.content.res.Configuration.SCREENLAYOUT_SIZE_XLARGE
        val isSw600dp = configuration.smallestScreenWidthDp >= 600
        return isLargeScreen || isSw600dp
    }

    fun handlePaywallLaunch(
        context: Context,
        onMobile: () -> Unit,
        onLargeDevice: () -> Unit
    ) {
        if (isLargeDevice(context)) {
            onLargeDevice() // Large Device (Foldable mobile or Tablet)
        } else {
            onMobile() // Mobile
        }
    }

    fun isReady(): Boolean = Purchases.isConfigured
}
