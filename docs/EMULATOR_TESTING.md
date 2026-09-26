# Deskmate — Emulator testing

Project-specific facts for the `emulator-testing` skill. Generic adb/uiautomator
technique lives in the skill itself, not here — this file is only what's true about
*this* app.

## Device facts

- AVD: `Medium_Phone_API_36.1` (also available: `Medium_Tablet`). Screen 1080x2400.
- Package id: `com.grappim.deskmate.debug` (debug build type adds `.debug`; no flavors).
- Activity: `com.grappim.deskmate.MainActivity`
- Build and install: `./gradlew :androidApp:assembleDebug`, then
  `adb -s emulator-5554 install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk`.
- Backend: the real ESP32 display on the home LAN; the device contract is
  `../esp32-desk-display/docs/API.md`. NSD and `desk.local` fail on the emulator, but a
  manual IP works: the emulator reaches the display through the host's network (M3.5,
  `192.168.0.147`). Find the IP with `getent hosts desk.local` on the host.

## App-specific gotchas

- The emulator and `adb` need the Bash sandbox disabled.
- The user's phone: serial `R5CR214CSQL` (Samsung SM-G998B), `adb -s R5CR214CSQL`.
  A second phone: serial `2c78f4512f1d7ece` (Samsung SM-A920F), seen 2026-09-26. The user
  wants on-device checks on the connected phone, not a headless emulator: run `adb devices -l`
  first. It can drop
  off adb during a session: run `adb devices` before a `logcat` read, or the read blocks.
- The user's physical phone can be attached over USB at the same time. Always pass
  `-s emulator-5554` to `adb`; a bare `adb` fails with "more than one device".
- On launch the emulator ends at "Display not found" (no NSD, no `desk.local`). That still
  means the Koin graph started; a crash at launch with a Koin error means it did not. Enter the
  display's IP in the manual field and press Enter (`ImeAction.Go` submits) to get real data.
- Offline test: `adb -s emulator-5554 shell cmd connectivity airplane-mode enable`. The
  offline banner shows and logcat tag `HostLocator` logs one search. Turn it off, then "Search
  again" finds the saved manual IP.
- Do not send panel on/off/toggle and do not restart the real display (user, 2026-09-26).
  Reading `/api/status` is fine.
- To test discovery, use the phone: logcat tag `HostLocator` names the path that won. To
  plant a wrong saved host, write a Preferences protobuf to
  `files/datastore/discovery.preferences_pb` with `run-as com.grappim.deskmate.debug`.
- `local.properties` (`sdk.dir=/home/gregory/Android/Sdk`) is gitignored. A fresh clone
  needs it before any Android Gradle task configures.
- No launcher icon yet: the app shows the default Android icon in the app drawer.
- To confirm `usesCleartextTraffic` in the built APK, read the manifest with
  `~/Android/Sdk/build-tools/<ver>/aapt2 dump xmltree --file AndroidManifest.xml <apk>`.
  `dumpsys package` does not print that flag.
- The widget (M4.1): `dumpsys appwidget | grep -i deskmate` shows the provider is registered.
  To place it: long-press the home screen, "Widgets", the "Browse" tab, expand "Deskmate",
  tap the 2×1 preview, then the "Add Deskmate widget" button (`content-desc`). No drag needed.
- The widget worker (M4.3): `dumpsys jobscheduler`, job tag `#WidgetRefreshWorker#`. A forced
  `cmd jobscheduler run -f -n androidx.work.systemjobscheduler <pkg> <id>` does not run a periodic
  worker early: WorkManager logs "executed before schedule" and re-enqueues it. Logcat lines start
  with "Widget refresh".
- The widget layout (M4.4): at the default 2×1 on this AVD the refresh icon is at about (463, 574)
  with data, (463, 547) with "No data yet". `am broadcast -a android.appwidget.action.APPWIDGET_UPDATE`
  from the shell fails with "Permission Denial" (protected broadcast). To re-render the widget
  (for example after deleting `files/datastore/widget.preferences_pb`), `adb install -r` the APK
  again: the package update makes the launcher render it.
- The widget on the SM-A920F (M4.5, API 29, Samsung launcher): long-press an empty home cell
  (not the weather widget: that opens its resize frame), "Widgets", then drag the Deskmate cell:
  `adb shell input draganddrop <cell center> <empty home cell> 2500` (the skill has the general
  recipe). The widget's corners are square there: Glance rounds them only on API 31+.
