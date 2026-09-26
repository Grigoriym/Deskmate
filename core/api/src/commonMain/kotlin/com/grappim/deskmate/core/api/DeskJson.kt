package com.grappim.deskmate.core.api

import kotlinx.serialization.json.Json

/**
 * The one `Json` config for the display's responses. API.md: "new fields may be added; ignore
 * ones you don't know", so unknown keys must not fail a decode.
 */
internal val DeskJson: Json = Json { ignoreUnknownKeys = true }
