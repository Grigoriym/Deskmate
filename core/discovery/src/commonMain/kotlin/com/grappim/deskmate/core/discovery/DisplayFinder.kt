package com.grappim.deskmate.core.discovery

/** Finds the display on the LAN by service discovery (API.md "Finding the display"). */
interface DisplayFinder {

    /**
     * The display's address in the form `DeskApi.baseUrl` takes, for example
     * `http://192.168.0.147:80`. `null` when nothing matched before the timeout.
     */
    suspend fun find(): String?
}
