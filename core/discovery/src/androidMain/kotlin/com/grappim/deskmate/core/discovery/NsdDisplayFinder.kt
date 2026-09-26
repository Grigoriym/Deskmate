package com.grappim.deskmate.core.discovery

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.annotation.Single
import java.net.Inet4Address
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds

private const val SERVICE_TYPE = "_http._tcp"
private const val SERVICE_NAME = "Desk display"
private val FIND_TIMEOUT = 5.seconds

@Single(binds = [DisplayFinder::class])
internal class NsdDisplayFinder(context: Context) : DisplayFinder {

    private val nsdManager = context.getSystemService(NsdManager::class.java)

    override suspend fun find(): String? = withTimeoutOrNull(FIND_TIMEOUT) {
        discover()?.let { resolve(it) }
    }

    /** Discovery stops when the flow ends: on the first match, on timeout and on cancellation. */
    private suspend fun discover(): NsdServiceInfo? = callbackFlow {
        val startFailed = AtomicBoolean(false)
        val listener = object : NsdManager.DiscoveryListener {
            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                trySend(serviceInfo)
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                // NsdManager has already dropped the listener; stopping it again would throw.
                startFailed.set(true)
                close()
            }

            override fun onDiscoveryStarted(serviceType: String) = Unit
            override fun onDiscoveryStopped(serviceType: String) = Unit
            override fun onServiceLost(serviceInfo: NsdServiceInfo) = Unit
            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) = Unit
        }
        nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, listener)
        awaitClose {
            if (!startFailed.get()) nsdManager.stopServiceDiscovery(listener)
        }
    }
        // Other devices (printers, routers) announce `_http._tcp` too.
        .filter { it.serviceName == SERVICE_NAME }
        .firstOrNull()

    /**
     * One path for every API level: the deprecated `resolveService`. It still works up to
     * compileSdk 37, and we resolve one service once. The API 34 replacement
     * (`registerServiceInfoCallback`) would need a second path for API 24-33 anyway.
     */
    @Suppress("DEPRECATION")
    private suspend fun resolve(service: NsdServiceInfo): String? = suspendCancellableCoroutine { cont ->
        val listener = object : NsdManager.ResolveListener {
            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                // IPv4 only: a link-local IPv6 address needs a scope id that a URL can't carry.
                val host = serviceInfo.host as? Inet4Address
                cont.resume(host?.let { "http://${it.hostAddress}:${serviceInfo.port}" })
            }

            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                cont.resume(null)
            }
        }
        nsdManager.resolveService(service, listener)
        cont.invokeOnCancellation {
            // Below API 34 a running resolve can't be stopped; it ends by itself.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // Throws when the resolve has already ended, which is the result we want.
                runCatching { nsdManager.stopServiceResolution(listener) }
            }
        }
    }
}
