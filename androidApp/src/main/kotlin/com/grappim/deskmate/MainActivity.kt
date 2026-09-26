package com.grappim.deskmate

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.grappim.deskmate.composeapp.DeskmateAppContent
import com.grappim.deskmate.core.discovery.HostLocator
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/** Android 17: NSD and LAN HTTP need `ACCESS_LOCAL_NETWORK` as a runtime permission. */
private const val LOCAL_NETWORK_PERMISSION_SDK = 37

class MainActivity : ComponentActivity() {
    private val locator: HostLocator by inject()

    // Search also when the user denies: the search then ends as not found.
    private val requestLocalNetwork =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { locate() }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            DeskmateAppContent()
        }
        // Below Android 17, `INTERNET` grants local network access.
        if (Build.VERSION.SDK_INT >= LOCAL_NETWORK_PERMISSION_SDK) {
            requestLocalNetwork.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
        } else {
            locate()
        }
    }

    private fun locate() {
        lifecycleScope.launch { locator.locate() }
    }
}
