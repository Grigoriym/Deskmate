# Revisit

Real problems found outside the current task. One entry each, with `file:line` or a link so a
cold session can act on it. Delete an entry when it's fixed.

- 2026-09-26 (M0.6) — CI annotation: `actions/upload-artifact@v4` targets Node.js 20, which
  GitHub now forces onto Node.js 24 (deprecated). See `.github/workflows/ci.yml` "Upload Kover
  XML report" step and run https://github.com/Grigoriym/Deskmate/actions/runs/36249336150.
  Bump to the current major; wayprint's `ci.yml` has the same pin.
- 2026-09-26 (M1.1) — Nothing detects drift between the fixture
  `core/api/src/commonTestFixture/kotlin/com/grappim/deskmate/core/api/dto/StatusExampleJson.kt` and
  `../esp32-desk-display/docs/api/status.example.json`. Check by hand:
  `awk '/^    """$/{f=1;next} /""".trimIndent/{f=0} f' <kt file> | sed 's/^    //' | diff - <json file>`.
  A small script (or a step in the esp32 repo's workflow) would make it a tool, not a memory.
  M3.1 re-synced the fixture to `0a9838e` (the `co2` drift); the detection gap stays open.
- 2026-09-26 (M1.3) — Live display at 17:05: `screen` went home → bvg → indoor → home → bvg
  within about a minute, with no screen command sent. In the same window one
  `POST /api/panel?set=toggle` returned `{"ok":true}` but `panel_on` stayed `true` 1.5 s later.
  Cause not confirmed: someone at the knob, or knob noise / a firmware issue. If it happens
  again with nobody at the knob, report it in `../esp32-desk-display`, not here.
- 2026-09-26 (M2.4) — The `ACCESS_LOCAL_NETWORK` runtime request
  (`androidApp/src/main/kotlin/com/grappim/deskmate/MainActivity.kt`, API 37+ only) has not run
  on a real Android 17 device; the M2.4 phone is API 35. Test it on an Android 17 device or
  emulator: allow → found; deny → "Display not found", no crash. Source:
  https://developer.android.com/privacy-and-security/local-network-permission
- 2026-09-26 (M3.2) — API.md and the firmware disagree on the storm group. API.md: `weather_code`
  95-99 is storm. `../esp32-desk-display/main/weather_parse.c` `weather_icon_for_code()`: only
  95, 96, 99; so 97 and 98 fall to cloud on the panel. The app follows API.md
  (`feature/display/domain/.../StatusMapper.kt`, `weatherGroup()`). Open-Meteo does not seem to
  send 97/98, so this is cosmetic. Fix it in the esp32-desk-display repo (doc or code), not here.
- 2026-09-26 (M3.6) — Two M3.6 real-device checks were skipped (user's decision), so nothing has
  run them on a real display: unplug the display → stale mark within ~10 s, one `rediscover()`
  (logcat tag `HostLocator`), fresh data after replug without an app restart; and app in the
  background → no polls in logcat. Only `DisplayViewModel`'s `commonTest` covers them. The Verify
  line is in `docs/CHECKLIST_ARCHIVE.md` M3.6. A natural time: M4's real-device check.
