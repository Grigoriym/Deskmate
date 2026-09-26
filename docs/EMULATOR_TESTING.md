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
- Backend: the real ESP32 display on the home LAN. The emulator does not reach it; the
  device contract is `../esp32-desk-display/docs/API.md`.

## App-specific gotchas

- The emulator and `adb` need the Bash sandbox disabled.
- The user's physical phone can be attached over USB at the same time. Always pass
  `-s emulator-5554` to `adb`; a bare `adb` fails with "more than one device".
- The screen (M2.4 proof, until M3) shows the discovery state. On the emulator it ends at
  "Display not found", because the emulator can't reach the home LAN. That still means the
  Koin graph started; a crash at launch with a Koin error means it did not.
- To test discovery, use the phone: logcat tag `HostLocator` names the path that won. To
  plant a wrong saved host, write a Preferences protobuf to
  `files/datastore/discovery.preferences_pb` with `run-as com.grappim.deskmate.debug`.
- `local.properties` (`sdk.dir=/home/gregory/Android/Sdk`) is gitignored. A fresh clone
  needs it before any Android Gradle task configures.
- No launcher icon yet: the app shows the default Android icon in the app drawer.
- To confirm `usesCleartextTraffic` in the built APK, read the manifest with
  `~/Android/Sdk/build-tools/<ver>/aapt2 dump xmltree --file AndroidManifest.xml <apk>`.
  `dumpsys package` does not print that flag.
