package com.grappim.deskmate.core.api

/** `go` values of `POST /api/screen` (API.md). [wire] is the exact, lowercase query value. */
enum class ScreenCommand(val wire: String) {
    Next("next"),
    Prev("prev"),
    Home("home"),
    Outdoor("outdoor"),
    Air("air"),
    Indoor("indoor"),
    Bvg("bvg")
}

/** `set` values of `POST /api/panel` (API.md). [wire] is the exact, lowercase query value. */
enum class PanelCommand(val wire: String) {
    On("on"),
    Off("off"),
    Toggle("toggle")
}
