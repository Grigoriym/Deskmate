package com.grappim.deskmate.core.discovery

/**
 * The last host that answered `GET /api/status`, in the form `DeskApi.baseUrl` takes
 * (for example `http://192.168.0.147`). Discovery tries it first on the next launch.
 */
interface SavedHostStore {

    /** `null` when no host was saved, or after [clear]. */
    suspend fun read(): String?

    suspend fun save(host: String)

    suspend fun clear()
}
