# Revisit

Real problems found outside the current task. One entry each, with `file:line` or a link so a
cold session can act on it. Delete an entry when it's fixed.

- 2026-09-26 (M0.2) — `config/detekt/detekt.yml` is wayprint's file copied whole. It carries
  wayprint's rule choices, incl. Compose rules turned off with a "todo investigate" comment:
  `LambdaParameterInRestartableEffect` (line 893), `MultipleEmitters` (line 935),
  `ModifierMissing` (line 909), `CompositionLocalAllowlist` (line 862). Turn each back on and
  run `./gradlew detekt` once real UI exists (M3); keep one off only for a real finding.
- 2026-09-26 (M0.6) — CI annotation: `actions/upload-artifact@v4` targets Node.js 20, which
  GitHub now forces onto Node.js 24 (deprecated). See `.github/workflows/ci.yml` "Upload Kover
  XML report" step and run https://github.com/Grigoriym/Deskmate/actions/runs/36249336150.
  Bump to the current major; wayprint's `ci.yml` has the same pin.
- 2026-09-26 (M1.1) — Nothing detects drift between the fixture
  `core/api/src/commonTest/kotlin/com/grappim/deskmate/core/api/dto/StatusExampleJson.kt` and
  `../esp32-desk-display/docs/api/status.example.json`. Check by hand:
  `awk '/^    """$/{f=1;next} /""".trimIndent/{f=0} f' <kt file> | sed 's/^    //' | diff - <json file>`.
  A small script (or a step in the esp32 repo's workflow) would make it a tool, not a memory.
  Drift is now real: esp32-desk-display `0a9838e` added `co2` to the example; M3 re-syncs it.
- 2026-09-26 (M1.2) — `DeskApi.status()` does not catch `SerializationException`: a `200` whose
  body doesn't decode throws out of the call
  (`core/api/src/commonMain/kotlin/com/grappim/deskmate/core/api/DeskApi.kt`, `call()`). The
  M3 poll loop must not die on it: catch it there, or add a `DeskResult` variant then.
- 2026-09-26 (M1.2) — `core/api/build.gradle.kts` declares `grappim-kit-coroutines`, but
  nothing in `core:api` uses it (M0 added it for `DeskApi` dispatchers; Ktor's engine does its
  own threading). Remove it, or keep it if M2/M3 put something in `core:api` that needs it.
- 2026-09-26 (M1.3) — Live display at 17:05: `screen` went home → bvg → indoor → home → bvg
  within about a minute, with no screen command sent. In the same window one
  `POST /api/panel?set=toggle` returned `{"ok":true}` but `panel_on` stayed `true` 1.5 s later.
  Cause not confirmed: someone at the knob, or knob noise / a firmware issue. If it happens
  again with nobody at the knob, report it in `../esp32-desk-display`, not here.
