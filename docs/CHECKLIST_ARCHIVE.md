# Deskmate checklist archive

Finished milestones, moved here verbatim from `docs/CHECKLIST.md`.

## M0 — repo and module scaffolding

- [x] **M0.1** — `git init` (default branch `master`). Build-logic source is decided: copy wallosmobile's (IMPLEMENTATION_PLAN.md §5), not
  `grappim-kit/build-logic`. No store flavors or release signing (no stores, decided 2026-09-26):
  drop `AppFlavors.kt` and the F-Droid/Play dimension when copying.
  Then port build-logic the way wayprint's archived M0.1 did (`wallosmobile.*` →
  `deskmate.*`, package `com.grappim.deskmate.buildlogic`), **keeping**
  `KmpNetworkConventionPlugin`. Watch for the hardcoded `project(":core:logger")` /
  `":testing"` / `":detekt-rules"` injections wayprint hit (see
  `grappim-kit/CONSUMING.md` build-logic section) — this app gets logger/testing from
  `grappim-kit` instead. Root `settings.gradle.kts`, `gradle/libs.versions.toml` (drop unused
  entries), `gradle.properties`, `.editorconfig`, `.gitignore`, Gradle wrapper, LICENSE.
  **Note:** the hardcoded injections were fixed here, not deferred: `Quality.kt` now takes
  `grappim-kit-testing` from the catalog instead of `project(":testing")`, and the
  `detektPlugins(project(":detekt-rules"))` line is gone (`detekt-rules` is deferred, plan §4).
  wallosmobile's `configureKmp()` already used `grappim-kit-logger`. So the catalog already holds
  `grappimKit = "0.1.7"` (published on Maven Central, checked 2026-09-26) with `logger` and
  `testing` entries; M0.3 adds the other kit modules under the same key.
  **Note:** carried-over suppressions were deleted, not copied: wallosmobile's per-file
  `ktlint_compose_*` overrides, its `compose_allowed_composition_locals` list and
  `ktlint_standard_kdoc = disabled` in `.editorconfig`. Re-add one only when a real finding needs it.
  **Note:** for M0.2, wayprint's M0.2 hit three gaps that this port also has: no root
  `build.gradle.kts` (plugins `apply false` + detekt/ktlint/kover), no `config/detekt/detekt.yml`,
  no `config/compose/stability_config.conf`. The convention plugins read all three.
  **Verify:** `./gradlew :build-logic:convention:build` and `./gradlew help` succeed. Both
  confirmed green; the built jar declares all seven `deskmate.*` plugin ids.

- [x] **M0.2** — `androidApp` + `composeApp` skeletons with a placeholder screen.
  `applicationId com.grappim.deskmate`. Manifest: `INTERNET`, `ACCESS_NETWORK_STATE`,
  `usesCleartextTraffic="true"`.
  **Note:** the three gaps named in M0.1's note were filled here: root `build.gradle.kts`
  (from wayprint), `config/detekt/detekt.yml` (wayprint's current copy, which already
  excludes `androidHostTest`) and `config/compose/stability_config.conf`. Catalog gained
  `app-pkg`, `version-code`, `version-name` and `androidx-activity-compose`.
  **Note:** no launcher icon yet (wayprint's icons are its own branding); the manifest has no
  `android:icon`, so Android shows its default icon.
  **Verify:** `./gradlew :androidApp:assembleDebug` succeeds; the APK installs and shows the
  placeholder on an emulator (`emulator-testing` skill; create `docs/EMULATOR_TESTING.md`).
  Confirmed on `Medium_Phone_API_36.1`: "Deskmate" text rendered (screenshot + `uiautomator
  dump`), both permissions granted, `usesCleartextTraffic=true` in the APK manifest.
  `ktlintCheck` and `detekt` green.

- [x] **M0.3** — Empty module skeletons from IMPLEMENTATION_PLAN.md §4 (`core:api`,
  `core:discovery`, `feature:display:domain`, `feature:display:ui`, `widget`, `strings`), each
  applying its convention plugins. Add `grappim-kit` `logger`, `coroutines`, `uikit`,
  `testing` under one `grappimKit` version key (current `VERSION_NAME` in
  `grappim-kit/gradle.properties`, only if published — check the latest `publish.yml` run).
  **Note:** `VERSION_NAME` is still `0.1.7`; all four artifacts return 200 on Maven Central.
  Each module has one `internal object Placeholder` (a `package`-only file trips detekt's
  `EmptyKotlinFile`, as wayprint found). Plugins: `core:api` library + serialization + network
  + di; `core:discovery` library + di; `feature:display:domain` library + stability;
  `feature:display:ui` library + compose + di; `strings` library + compose; `widget` library
  only (Glance comes in M4).
  **Note:** the two new kit modules are wired, not just listed, so the build proves they
  resolve: `coroutines` in `core:api` (dispatchers for `DeskApi`, M1.2), `uikit` in
  `feature:display:ui`.
  **Verify:** `./gradlew build` green. Confirmed (all six modules ran detekt, ktlint, kover);
  `grappim-kit-coroutines:0.1.7` and `grappim-kit-uikit:0.1.7` are on the compile classpaths.

- [x] **M0.4** — Koin skeleton: `AppModule`, one injected dependency visible on the
  placeholder screen (proves the graph).
  **Note:** wayprint's shape (`composeApp/.../di/Koin.kt`: `AppModule` with `@ComponentScan`,
  `@KoinApplication object KoinApp`; `DeskmateApp : Application` calls `startKoin<KoinApp>`),
  minus wayprint's `expect class PlatformComponentModule`: Android is the only target and
  nothing needs a platform binding yet. The injected value is `GreetingProvider.greeting()` =
  "Deskmate (via Koin)". It differs from M0.2's hardcoded "Deskmate" on purpose, so the
  screenshot shows the injection happened. Catalog gained `koin-test`.
  **Note:** `KoinGraphTest` is in `composeApp/src/commonTest` (runs as `testAndroidHostTest`).
  No `extraTypes` needed yet: no definition takes a `Context`. `verify()` is
  `@KoinExperimentalAPI`, so the test compile prints one warning.
  **Verify:** `./gradlew build` green; `KoinGraphTest` passes. Negative check: a temporary
  `@Single class Broken(val missing: Missing)` made it fail with "Missing definition", then was
  removed. On `Medium_Phone_API_36.1` the app shows "Deskmate (via Koin)" (screenshot +
  `uiautomator dump`); no crash in logcat.

- [x] **M0.5** — `CLAUDE.md`: merge `../agentic-grappim/templates/CLAUDE.md.template`
  (working agreements, close-out, settled decisions, reference projects) into the existing
  file without losing its project sections. Create `docs/revisit.md`, `docs/frictions.md`.
  **Note:** done early, 2026-09-26, before any code — so M0.1-M0.4 sessions already get the
  working agreements. Not committed yet (no git repo); M0.1's first commit includes it.
  **Verify:** no template placeholder left (`grep -n '<[A-Z][A-Z]' CLAUDE.md` empty; `<N>` in the
  workflow line is intentional). Confirmed.

- [x] **M0.6** — GitHub repo (ask the user: name, public/private) + CI: port
  `wayprint/.github/workflows/ci.yml` (build, ktlint, detekt, tests). Push `master`.
  **Note:** user chose `Grigoriym/Deskmate`, public (capitalised like Wayprint/Wallosmobile).
  Default branch on GitHub is `master`. The workflow drops wayprint's signing secrets, keystore
  restore and fdroid/gplay assemble steps; `:androidApp:assembleDebug` replaces them.
  **Note:** two commits, not one: the Verify needs the pushed workflow to run first. The tick
  commit is docs-only, so `paths-ignore` skips CI for it.
  **Verify:** first CI run on `master` is green. Confirmed: run
  [36249336150](https://github.com/Grigoriym/Deskmate/actions/runs/36249336150), 5m26s;
  its log shows `:composeApp:testAndroidHostTest` ran (tests were not skipped).

- [x] **M0.7** — Tell the other projects the app exists: update the `deskmate` paragraph in
  `../grappim-watcher/CLAUDE.md` (added 2026-09-26, planning-only) with the repo URL and
  status, and add a pointer from `esp32-desk-display` ROADMAP step 12 to this repo (that
  repo's own PR/commit rules apply).
  **Note:** grappim-watcher has no git repo, so its edit is on disk only. esp32-desk-display
  has no PR rule and commits straight to `master`: one commit, pushed. The watcher paragraph
  points to this checklist's banner for status instead of copying a step number.
  **Verify:** both pointers name the right path/repo URL. Confirmed: the URL returns 200, and
  `../deskmate` resolves from both repos.

## M1 — API client (`core:api`)

- [x] **M1.1** — DTOs for `/api/status`, mirroring API.md field-for-field; every section
  nullable; `ignoreUnknownKeys`. Copy `status.example.json` into test resources, with a comment
  naming its source path and the `esp32-desk-display` commit it came from.
  **Note:** the fixture is a Kotlin raw string (`core/api/src/commonTest/.../StatusExampleJson.kt`),
  not a `.json` resource file. Reading a file from `commonTest` needs a JVM-only API, and plan §4
  keeps `core:api` common. JSON also can't hold the source comment. The string is byte-identical
  to the upstream file at esp32-desk-display `cf9740c` (checked with `diff`).
  **Note:** sections are nullable but have no default: API.md says the key is always sent, as
  `null` when there is no data. Only `warning`'s fields after `count` default to `null`, because
  they are absent when `count` is 0 (a fourth test covers that). Values stay raw strings
  (`screen`, `severity`, times); typing them is domain work. `DeskJson` holds the one `Json`
  config. `core:api`'s `Placeholder` is deleted.
  **Verify:** a test decodes the fixture and asserts every field; a second test decodes it
  with every section set to `null`; a third decodes it with an extra unknown field. Confirmed:
  `StatusDtoTest` 4/4 green, `./gradlew build` green. Negative check: with
  `ignoreUnknownKeys = false` only the unknown-field test failed, then the setting was restored.

- [x] **M1.2** — `DeskApi`: `status()`, `screen(go)`, `panel(set)` with typed values (no raw
  strings at call sites). 3-5 s timeouts, requests serialised (one at a time), results as a
  sealed type: success / HTTP error (status + body) / offline (timeout, refused, unknown host).
  **Note:** `DeskApi(engine, baseUrl: () -> String)` builds its own `HttpClient`, so the tests
  run the real timeout config (connect 3 s, request 5 s). `baseUrl` is read on every call,
  because M2's discovery can change the host. Typed values are `ScreenCommand` and
  `PanelCommand`. Offline = any `kotlinx.io.IOException` (Ktor's timeouts included). Not caught:
  a `200` whose body does not decode throws `SerializationException`. API.md's host test keeps
  the fixture equal to the firmware output, so this is a firmware bug, not a case the sealed
  type names. No Koin wiring yet: there is no host source until M2. New catalog entry:
  `ktor-client-mock` (test only). `DeskApi` needs no dispatcher from `grappim-kit-coroutines`
  (the engine does its own I/O threading); M0 added that dependency for it, and it stays unused.
  **Verify:** tests against Ktor `MockEngine` for each outcome, incl. 400/405/500 bodies and a
  timeout; a test proves two concurrent calls don't overlap. Confirmed: `DeskApiTest` 10/10,
  `./gradlew build` green. Negative checks: without the `Mutex` the overlap test failed
  (max in flight 2); with a 600 s request timeout the timeout test failed.

- [x] **M1.3** — Real-device smoke test: point the client at the actual display.
  **Note:** run as a throwaway `androidHostTest` in `core:api` (`DeskApi` + OkHttp engine),
  deleted afterwards; nothing committed but this tick. `desk.local` resolved on the dev
  machine (Linux) to `192.168.0.147`. The first `Toggle` returned `Success` but `panel_on` did
  not change 1.5 s later; in the same minute `screen` changed with no command from the app
  (see `docs/revisit.md`). The next toggles (3 s wait) flipped the panel each time.
  **Verify:** status parses from `http://desk.local` (or the IP) and a `panel?set=toggle`
  visibly flips the panel; the user confirms. Confirmed: `status()` decoded every section
  (`bvg` was `null`); `panel(Toggle)` flipped `panel_on` `true → false`, and the user saw
  the panel go dark. The panel was turned back on afterwards.

## M2 — discovery (`core:discovery`)

Find the display and give `DeskApi` its `baseUrl`. Broken down 2026-09-26.

Shared context for all of M2 (re-verify, don't re-derive):

- **Contract:** API.md "Finding the display". NSD service type `_http._tcp.`, filter by
  service name `Desk display`, then resolve to host + port. Fallback `desk.local`, then a
  manual IP. Re-read that section at the start of each step.
- **Search order:** saved host → NSD → `desk.local` → not found. A candidate counts as found
  only when `GET /api/status` on it returns `DeskResult.Success` (the "probe"). The first
  candidate that passes is saved as the last good host.
- **`grappim-kit-storage` does not fit** (checked 2026-09-26, kit `0.1.7`). It holds
  `NetworkMonitor`, `SecretCipher` and `TrustedCertStorage`, and no general key-value store.
  So the saved host is a small local store over DataStore Preferences, the same shape as
  `../wallosmobile/core/storage` (`ServerUrlStorageImpl`, and `StorageModule` for the
  `Context`-based file path).
- **Everything that decides is in `commonMain` and tested with hand-written fakes.** Only
  the NSD code is `androidMain`, behind an interface.
- **Manual IP:** M2 gives the API (probe, then save). The input field is M3's UI (the
  not-found state), because M2 adds no screen.
- **Offline → re-discover:** M2 gives a `rediscover()` call. M3's poll loop calls it when the
  saved host goes offline.
- **The emulator can't see the home LAN's mDNS.** Any step that needs the real display runs on
  the user's phone on the home WiFi, or on the dev machine as M1.3 did.

- [x] **M2.1** — Saved host store in `core:discovery`: read, save and clear one host string
  (for example `http://192.168.0.147`, the form `DeskApi.baseUrl` takes). DataStore Preferences
  in `commonMain`; the Android file path and Koin provider in `androidMain`, as wallosmobile's
  `StorageModule` does. Catalog: DataStore under one version key; take the current version,
  not wallosmobile's pin without a check. `core:discovery` then no longer needs its
  `Placeholder`.
  **Verify:** `commonTest` against a DataStore on a temp file: empty store reads `null`; a saved
  host reads back; it survives a new store instance on the same file; `clear()` empties it.
  `./gradlew build` green.
  Note: DataStore `1.2.1` is the newest stable (1.3.0 is alpha). `SavedHostStore` +
  `DiscoveryModule` (androidMain) exist, but `composeApp` does not include `DiscoveryModule`
  yet; that is M2.4. DataStore allows one active instance per file, so the "new instance" test
  cancels the first store's scope before it opens the second.

- [x] **M2.2** — NSD finder: an interface in `commonMain` (one call: "find the display, or
  `null` after a timeout"), the `NsdManager` implementation in `androidMain`. Discover
  `_http._tcp.`, match the service name `Desk display`, resolve it, return
  `http://<host>:<port>`. Always stop discovery, on success, timeout and cancellation.
  minSdk is 24: `resolveService` is deprecated from API 34 but still works; choose one path
  and say why in a comment.
  **Check first:** targetSdk is 37. Find out whether Android's local-network protection needs a
  permission for NSD or for LAN HTTP at this targetSdk (the M0.2 manifest has only `INTERNET`
  and `ACCESS_NETWORK_STATE`). Record the answer, with a source link, in the step's Note.
  **Verify:** the code compiles and `./gradlew build` is green. No unit test for `NsdManager`
  itself (see CLAUDE.md "Don't break production in favor of tests"); the real check is M2.4.
  Note: `DisplayFinder` (commonMain) and `NsdDisplayFinder` (androidMain, 5 s timeout, IPv4
  only). One resolve path: the deprecated `resolveService` on all API levels, because the
  API 34 replacement would still need it for API 24-33. **Permission answer: yes.** At
  targetSdk 37, on Android 17+, NSD and any TCP to a LAN address need the runtime permission
  `ACCESS_LOCAL_NETWORK` (group `NEARBY_DEVICES`). Without it, TCP typically times out. On
  Android 16 and lower, `INTERNET` grants it implicitly. Source:
  https://developer.android.com/privacy-and-security/local-network-permission . Not added in
  this step; see the M2.2 entry in `docs/revisit.md`, which M2.4 must handle.

- [x] **M2.3** — `HostLocator` in `commonMain`: runs the search order above and exposes the
  state (searching / found host / not found) as a `StateFlow`. Also `setManual(ip)` (probe,
  then save; an unreachable IP is not saved) and `rediscover()` (skip the saved host, search
  again). The probe uses `DeskApi.status()`; decide in the step whether `core:discovery`
  depends on `core:api` or takes the probe as a function, and note why.
  **Verify:** tests with fakes for the store, the NSD finder and the probe: saved host up →
  found without NSD; saved host down → NSD finds a new IP → the store holds the new IP; NSD
  finds nothing → `desk.local` is tried; nothing passes → not found and the store is not
  cleared; manual IP unreachable → not saved. `./gradlew build` green.
  Note: the probe is a `HostProbe` interface in `core:discovery`, not a `core:api` dependency.
  `HostLocator` probes a different host on each call, but `DeskApi` is built around one
  `baseUrl` and one engine. **M2.4 must implement `HostProbe`** (with `DeskApi`) and bind it;
  without it, `KoinGraphTest` fails once `DiscoveryModule` is in the graph. Nothing searches
  until a caller runs `locate()`; the start state is `Searching`. `setManual` takes an address
  without a scheme and adds `http://`. On a failed `setManual`, the state does not change.

- [x] **M2.4** — Wiring: Koin provides `HostLocator`, and `DeskApi` with `baseUrl` from the
  found host. `KoinGraphTest` gets `Context` in `extraTypes`. The placeholder screen shows the
  locator state and, when found, the status `time` (the M0.4 greeting goes). This is a
  temporary proof, replaced by M3's screen.
  **Verify:** on the user's phone on the home WiFi: first launch shows the display found via NSD
  (logcat names the path that won), then the status time. Put a wrong IP in the store (for
  example with a debug `adb` command); the next launch falls back to NSD and saves the right
  IP. `KoinGraphTest` passes; `./gradlew build` green.
  Note: phone Samsung SM-G998B, Android 15 (API 35), 2026-09-26. First launch: logcat
  `HostLocator: Found http://192.168.0.147:80 via NSD`, then the screen showed `Time: 18:57`.
  With `http://192.168.0.99` written into the DataStore file (`run-as`), the next launch logged
  `No display at http://192.168.0.99 (saved host)`, then found `.147` via NSD and saved it.
  Wiring: `ApiModule` (core:api androidMain) gives one OkHttp `HttpClientEngine`;
  `DeskHostProbe` (composeApp) implements `HostProbe` with one `DeskApi` whose `baseUrl` is the
  host under test; `AppModule.provideDeskApi` reads the locator state on every call and **throws
  `IllegalStateException` when the state is not `Found`**. M3's poll loop must not call
  `DeskApi` during a `rediscover()`. `MainActivity` asks for `ACCESS_LOCAL_NETWORK` on API 37+,
  then runs `locate()`; the phone is API 35, so that path is untested (see `docs/revisit.md`).
  **Check widened:** `KoinGraphTest` `extraTypes` gained `Function0` (a false positive, the
  `DeskApi` constructor's `baseUrl` lambda). The test does not see provider-function parameters:
  it still passes with `DiscoveryModule` removed from `includes`. Logging: `TimberLogger` +
  debug `Timber.DebugTree` in `DeskmateApp` (Timber `5.0.1` added to the catalog).

## M3 — status screen and controls (`feature:display`)

Show everything `/api/status` holds and switch the screen and panel. Broken down 2026-09-26.

Shared context for all of M3 (re-verify, don't re-derive):

- **Contract:** API.md `GET /api/status`, the section tables, "Command responses and
  timing" and "Client guidelines". Re-read them at the start of each step. Checked at this
  break-down: no esp32-desk-display commit after `0a9838e` touches `docs/API.md` or
  `docs/api/`.
- **Layers.** `feature:display:domain` maps `StatusDto` to a UI-ready model and holds every
  rule that decides something (bands, rain, BVG hint). It depends on `core:api` for the DTOs.
  It returns typed values (enums, sealed types, numbers), not display text: the text comes
  from `strings` in the UI. `feature:display:ui` holds the poll loop and the screen.
- **Tests without new seams.** `DeskApi` is tested against Ktor `MockEngine` (as in M1.2).
  `HostLocator` is a real instance over fakes of `SavedHostStore`, `DisplayFinder` and
  `HostProbe` (as in M2.3). No production seam only for a test (CLAUDE.md).
- **`DeskApi` throws `IllegalStateException` when the locator state is not `Found`**
  (M2.4, `composeApp/.../di/Koin.kt`). The poll loop calls it only in `Found`.
- **Real data needs the home LAN.** The emulator can't see mDNS/NSD. A manual IP from the
  emulator may still reach the display through the host's network; try it before assuming the
  phone is needed.
- **Revisit entries this milestone owns:** the M1.1 fixture drift, the M1.2
  `SerializationException` and the M1.2 unused `grappim-kit-coroutines` (M3.1 / M3.4), and the
  M0.2 detekt Compose rules (M3.5). Delete each entry in the step that settles it.

- [x] **M3.1** — `co2` in the API client. API.md gained a `co2` section (esp32-desk-display
  `0a9838e`). Add `Co2Dto` (`ppm: Int`) and a nullable `co2` to `StatusDto`, in API.md's field
  order. Re-sync `StatusExampleJson.kt` to that commit's `docs/api/status.example.json` and
  update its source comment. Settle the M1.2 unused `grappim-kit-coroutines` entry: remove the
  dependency from `core/api/build.gradle.kts` unless M3 needs it there (a dependency change:
  say so in the commit).
  **Verify:** the revisit entry's `awk … | diff` command prints nothing. `StatusDtoTest` asserts
  `co2.ppm`, and the all-`null` test covers `co2`. `./gradlew build` green.
  Note: the `grappim-kit-coroutines` catalog entry went too (no other user). `kotlinx.coroutines`
  in `core:api` comes from the KMP convention plugin. The fixture-drift revisit entry stays: the
  drift is fixed, the missing drift check is not.

- [x] **M3.2** — Domain model and mapper, all sections except `bvg`. One function
  `StatusDto → DisplayStatus`; `feature:display:domain` loses its `Placeholder`. Typed values:
  - `screen` → enum of API.md's five screens; `panel_on` as is.
  - `date` in 1970 → "clock not synced yet" (then `time` means nothing either).
  - `outdoor.weather_code` → the panel's groups (sun, cloud, rain, snow, storm; unknown →
    cloud). `rain` → the four panel cases (`NO RAIN 12H`, `RAIN <from>`, `RAIN TILL <until>`,
    `RAIN NEXT 12H`).
  - `co2.ppm` → the four bands in API.md's `co2` table. Boundaries (800, 1000, 1400) are not
    fully pinned by the text: pick one reading, say it in a KDoc, test each boundary.
  - `air.aqi_label` → an enum; an unknown label must not crash (the device can add one).
    Pollen → none / low / medium / high per API.md's scale, for all five keys.
  - `warning`: `null` → unknown, `count` 0 → none, else event + severity enum + started +
    onset. The device already picked the warning; the app does not re-pick.
  - `next_holiday` → name + date, and "today" when its `date` equals the top-level `date`.
  **Verify:** `commonTest` decodes the fixture and checks the mapped model. One test per rule,
  incl. every rain case, every band boundary (CO2, pollen), each `null` section on its own, an
  unknown `aqi_label`/`severity`/`screen`, a 1970 date. `./gradlew build` green.
  Note: `Screen` has an `UNKNOWN` entry (an unknown `screen` must not crash either). A `warning`
  with `count` > 0 but a missing field maps to `Warning.Unknown`. `date`/`time` stay strings (no
  `kotlinx-datetime`). The fixture moved to `core/api/src/commonTestFixture/`, compiled by both
  `core:api` and `feature:display:domain` tests (`srcDir` in both `build.gradle.kts`; one copy).

- [x] **M3.3** — BVG in the domain model. `bvg` `null` → no data. Otherwise the departures
  (line, direction, time, `in_min`), with the uncatchable ones (`in_min` < `walk_min`) marked
  or dropped as the panel does, and the hint for the first catchable one: `LEAVE IN
  <in_min - walk_comfort>` while positive, `GO NOW` at 0, `HURRY` below. An empty list and a
  list with no catchable departure both need a defined result.
  **Verify:** tests for each hint case at its boundary (`in_min - walk_comfort` = 1, 0, -1),
  `in_min` = `walk_min` (catchable) and `walk_min - 1` (not), empty list, all uncatchable,
  `null`. `./gradlew build` green.
  Note: uncatchable departures are dropped, as the panel does (`screens.c` `layout_bvg()`).
  `Bvg.hint` is `null` exactly when no departure is left (empty or all uncatchable; the panel
  shows `NO TRAINS`). The hint uses `in_min`; the panel uses its own clock, same meaning.

- [x] **M3.4** — Poll loop and commands, in `feature:display:ui` (`commonMain`; a `ViewModel`
  or a plain state holder, decide in the step and note why). One UI state `StateFlow`:
  locator state, last `DisplayStatus`, stale flag.
  - Poll `status()` every 5 s, only while the screen is visible, and only in `Found`.
  - `Offline` → keep the last data, mark it stale, call `rediscover()` once (not on every
    tick). `Found` again → poll again, stale clears on the next success.
  - A `200` that does not decode (the M1.2 revisit entry): the loop must not die. Catch it here
    or add a `DeskResult` variant in `core:api`; note which, and delete the entry.
  - `HttpError` → keep the last data; show the error, don't rediscover (the display answered).
  - `setManual(ip)` passes through for the not-found state; a failure is visible in the state.
  - Screen/panel commands: send, re-read `status()` ~150 ms later, then the normal poll. The
    device can take up to ~10 s to apply one; don't report a failure for that delay.
  **Verify:** `commonTest` with virtual time (`runTest`, Turbine is in the catalog), `MockEngine`
  and a real `HostLocator` over fakes: 5 s cadence; no call outside `Found`; offline → stale +
  one `rediscover()`; recovery clears stale; undecodable body → loop still polls; a command →
  one re-read ~150 ms later; polling stops when not visible. `./gradlew build` green.
  Note: a `ViewModel` (`DisplayViewModel`): commands and `rediscover()` need a scope that outlives a
  rotation. "Visible" = `uiState` has a collector (`WhileSubscribed()`, no stop timeout); M3.5 must
  collect it with `collectAsStateWithLifecycle`. The undecodable body is a new
  `DeskResult.Undecodable`, caught in `DeskApi.call()`. The ViewModel catches `DeskApi`'s
  `IllegalStateException`: the host state can leave `Found` while a call waits for `DeskApi`'s lock.
  Not in Koin yet: M3.5 wires it (and `feature:display:ui` into `composeApp`).

- [x] **M3.5** — Status screen. Replaces the M2.4 proof screen in `DeskmateAppContent`.
  - One card per section, each `null`-safe on its own; `null` shows "no data yet".
  - Stale data visibly marked; clock-not-synced visibly marked.
  - Not-found state: retry (`rediscover()`) and the manual IP field (`setManual`).
  - Screen buttons (the five screens plus next/prev) and panel on/off/toggle; the current
    `screen` and `panel_on` are visible.
  - All user text from `strings` (CMP resources), not hardcoded.
  - Settle the M0.2 detekt entry: turn the four Compose rules back on, run `./gradlew detekt`,
    keep one off only for a real finding (say so in the commit either way).
  **Verify:** `./gradlew build` green, `KoinGraphTest` passes. On the emulator
  (`emulator-testing` skill): the not-found state shows, the manual IP field works if the
  display is reachable from it, and each card renders (screenshot + `uiautomator dump`).
  Note: retry calls `locate()`, not `rediscover()`: `rediscover()` skips the saved host, so a
  saved manual IP (no NSD, no `desk.local`) would never come back. `DisplayViewModel.retry()`,
  tested. All four detekt Compose rules are on, with no finding (a planted violation confirmed
  they run). Strings: `strings` module, `Res` in `com.grappim.deskmate.strings.generated.resources`.
  Emulator: the manual IP reached the live display; every card rendered real data (`bvg` was
  `null`, so only its "no data yet" path). Airplane mode: stale banner and one search. No
  screen/panel command was sent from the emulator: M3.6 checks them (and the user said: no
  panel on/off, no restart of the display without asking).

- [x] **M3.6** — Real-device check, on the user's phone on the home WiFi. Nothing to build
  unless it finds a bug. Ask the user before panel off/on and before unplugging the display
  (user, 2026-09-26: "don't restart the device or on/off it").
  **Verify:** every section shows the same values as `curl http://desk.local/api/status`; each
  screen button changes the panel; panel off/on works; the user confirms. Unplug the display →
  data marked stale within ~10 s, logcat shows one `rediscover()`; plug it back → data fresh
  again without an app restart. Background the app → logcat shows no polls.
  Note: the user checked discovery (no manual IP), every card against `curl` (`bvg` had
  data this time), the screen buttons and panel on/off on the phone: "everything seems fine".
  **Skipped** (user, 2026-09-26: "skip them"): the unplug test (stale mark, one
  `rediscover()`, recovery) and the background test (no polls). The phone had dropped off adb,
  so no logcat was read. Only M3.4's `commonTest` covers those two behaviours.

## M4 — Glance widget (`widget`)

Outdoor/indoor temp + next departure on the home screen. Broken down 2026-09-26.

Shared context for all of M4 (re-verify, don't re-derive):

- **Refresh policy is decided** (IMPLEMENTATION_PLAN.md §5): periodic WorkManager at the
  15 min minimum, plus a refresh when the app polls, on widget tap, and a refresh button. API.md
  "Client guidelines" allows 30-60 s or longer for a background widget, so 15 min is well inside.
- **Contract:** checked at this break-down: no esp32-desk-display commit after `0a9838e` touches
  `docs/API.md` or `docs/api/`. Re-check at the start of each step that calls the API.
- **Data goes stale on the widget.** It can be up to 15 min old, and a rendered widget does not
  re-render by itself. So show the fetch time as a clock time ("as of 14:05"), not a relative
  age ("5 min ago") that would freeze. Same for the departure: show its `time`, not `in_min`.
- **Widget tap** opens the app; the app's poll then refreshes the widget (M4.5). The **refresh
  button** runs one fetch in the background, without opening the app.
- **The widget needs the display without the app on screen.** Koin starts in `DeskmateApp`, so a
  worker can inject `HostLocator` and `DeskApi`. `HostLocator` starts in `Searching` when only
  the worker runs: call `locate()` first. `DeskApi` throws outside `Found` (M2.4).
- **Tests without new seams** (as in M3.4): the fetch logic lives in a plain class, tested in
  `commonTest` with `MockEngine` and a real `HostLocator` over fakes. The `CoroutineWorker` and
  the Glance code stay thin wrappers, checked on the emulator. No production seam only for a test.
- **`widget` is Android-only in practice.** Glance and WorkManager have no `commonMain`
  artifact. Pure logic (snapshot mapping, fetch) can stay in `commonMain`; Glance, the receiver
  and the worker go in `androidMain`.
- **Revisit entry this milestone owns:** the M3.6 skipped real-device checks (M4.6). Delete it
  in the step that runs them.

- [x] **M4.1** — Empty widget on the home screen. Add Glance (`glance-appwidget`, plus
  `glance-material3` only if used) and WorkManager (`work-runtime-ktx`) to the catalog, with
  `# https://…` release-page comments as the other keys have. `widget` gets an `androidMain`
  with a `GlanceAppWidget` that shows one static text, its `GlanceAppWidgetReceiver`, the
  `appwidget-provider` XML, and the receiver in a manifest. Find out where the manifest and XML
  resources can live: the KMP Android library plugin may need `androidResources` turned on, or
  they go in `androidApp`. Note the choice. `widget` joins the app's dependencies. Delete
  `widget`'s `Placeholder.kt`. Dependency change: say so in the commit.
  **Verify:** `./gradlew build` green, `KoinGraphTest` passes. On the emulator: the widget is in
  the widget picker, places on the home screen and shows the static text (screenshot).
  Note: Glance `1.2.0` only. WorkManager moved to M4.3: nothing used it yet (Glance brings
  `work-runtime-ktx` 2.7.1 transitively anyway). `widget` applies `deskmate.kmp.library.compose`:
  it gives the Compose compiler and turns on `androidResources`. So the manifest and the XML live in
  `widget/src/androidMain/`, and the receiver merges into the app's manifest. The provider has
  `updatePeriodMillis="0"`; `targetCell` 2×1.

- [x] **M4.2** — Widget snapshot and its store. A `WidgetSnapshot`: outdoor temp, indoor temp,
  next catchable departure (line, direction, `time`), fetch time. One pure mapping
  `DisplayStatus + fetch time → WidgetSnapshot` in `commonMain`. Each field is `null`-safe on its
  own: a `null` section, `bvg` `null`, and a `Bvg` with `hint == null` (no catchable departure,
  M3.3) each give a defined result. Store the last snapshot so the widget can read it without a
  network call: Glance state (`PreferencesGlanceStateDefinition`) or a small DataStore of its own
  (as `SavedHostStoreImpl`). Pick one and note why. A new snapshot replaces the old one only on
  success; a failure keeps the old one.
  **Verify:** `commonTest`: the fixture maps to the expected snapshot; each `null` case above;
  the store round-trips a snapshot and returns `null` when empty. `./gradlew build` green.
  Note: own DataStore (`WidgetSnapshotStore`, `widget/commonMain`), not Glance state: Glance
  state is per widget instance and Android-only; this is one snapshot for all instances, tested in
  `commonTest`. The snapshot is one JSON string (`deskmate.kmp.serialization` added to `widget`);
  `fetchedAt` is a `kotlin.time.Instant`. A JSON that no longer decodes reads as `null`. The store
  is **not in Koin yet**: M4.3 wires it. `DiscoveryModule` already provides an unqualified
  `DataStore<Preferences>`, so the widget's DataStore needs a qualifier (or the store builds its
  own), or Koin resolves the wrong file.

- [x] **M4.3** — Background refresh. Add WorkManager (`androidx.work:work-runtime`, latest
  stable; a dependency change, say so in the commit). A plain `WidgetRefresher` (`commonMain`): if the locator
  is not `Found`, `locate()` first; then `status()`; on success, map and save the snapshot. On
  any failure keep the last snapshot; don't call `rediscover()` (the app's poll owns that, M3.4).
  A thin `CoroutineWorker` calls it, then updates the widget. Periodic unique work, 15 min,
  `KEEP`, network-connected constraint, enqueued from `DeskmateApp`. Log each run (`logcat`).
  **Verify:** `commonTest` (`MockEngine`, real `HostLocator` over fakes): `Searching` → one
  `locate()`, then one `status()`; success saves; `Offline`, `HttpError`, `Undecodable` and
  `NotFound` keep the old snapshot and call no `rediscover()`. `./gradlew build` green. On the
  emulator: `adb shell dumpsys jobscheduler` (or WorkManager's diagnostics) shows the periodic
  work; forcing it once logs a run.
  Note: `work-runtime` 2.12.0. `WidgetModule` (`widget/androidMain`) provides the store with its
  own DataStore file `widget`, so no second `DataStore<Preferences>` bean. `composeApp` now depends
  on `widget` to include `WidgetModule`. The worker gets `WidgetRefresher` as a `KoinComponent`
  (no `WorkerFactory`). Emulator: `dumpsys jobscheduler` shows the job (`CONNECTIVITY`, first run
  after ~15 min). **Not run:** the forced run. WorkManager defers a forced periodic job ("executed
  before schedule"), and the user chose not to wait 15 min. M4.4's refresh button runs the same
  worker on demand; M4.6 checks the 15 min run on the phone.

- [x] **M4.4** — Widget layout and refresh button. Render the snapshot: outdoor temp, indoor
  temp, next departure (or "no trains"), and "as of HH:mm". No snapshot yet → "no data yet".
  Tap anywhere else → open `MainActivity`. Refresh button → one-time unique work that runs
  `WidgetRefresher`, with `KEEP` so repeated taps queue no extra calls. User text comes
  from string resources: decide between CMP `getString` in `provideGlance` and Android
  resources, and note why. Match the app's look only as far as Glance allows; no new theme work.
  **Verify:** `./gradlew build` green. On the emulator with a manual IP that reaches the display
  (M3.5 did): the widget shows real values that match the app; the refresh button updates "as of"
  (screenshot before/after); the tap opens the app. With no snapshot: "no data yet".
  Note: strings from CMP `getString` (the `strings` module), not Android resources: all user text
  stays in one file. `getString` is `suspend`, so the text is resolved in a `Flow`, outside the
  composition. The widget collects `WidgetSnapshotStore.snapshots` (new): Glance calls
  `provideGlance` once per session, and an `update()` in a live session only recomposes, so a
  one-shot `read()` would show old data. "as of" uses the phone's time format (12/24 h). The
  departure reads "U5 21:40, HAUPTBAHNHOF": at 2×1 the direction gets cut, not the time. The
  indoor "°C" is cut at 2×1; the widget is resizable. The one-time work has the same
  network-connected constraint as the periodic one. No new external dependency (`widget` →
  `strings` is a project dependency). Emulator: 3 fast taps gave 2 runs (the tap during run 1 was
  dropped by `KEEP`); values match the app and `curl`; refresh from "No data yet" filled the
  widget without a re-install (the `Flow` path).

- [x] **M4.5** — App poll refreshes the widget. Each successful poll in `DisplayViewModel` saves
  the snapshot and asks the widget to update. `feature:display:ui` is `commonMain`, Glance is not,
  so it talks to the widget through an interface: the interface exists for the module
  boundary, not for the test. Decide where it lives and which module depends on which (no
  cycle), and note it. Throttle the widget update if every 5 s is too often (Glance may drop
  updates); note what you find.
  **Verify:** `DisplayViewModel` `commonTest`: a successful poll saves one snapshot; a failed
  one saves none. `./gradlew build` green, `KoinGraphTest` passes. On the emulator: open the
  app, go home, the widget's "as of" matches the last app poll.
  Note: `StatusListener` (`suspend fun onStatus(DisplayStatus)`) lives in
  `feature:display:domain`: `feature:display:ui` and `widget` both depend on it already, so no new
  module edge and no cycle. `WidgetStatusListener` (`widget/androidMain`, `@Single(binds)`, as
  `NsdDisplayFinder`) saves the snapshot, then `updateAll`. **No throttle:** on the phone
  (SM-A920F, API 29, run on the real device, not the emulator) a 70 s app run gave 15 widget
  renders ~5 s apart, none dropped. The updates keep one Glance `SessionWorker` alive for the
  whole run (one start); a live session only recomposes. The saved `fetchedAt` (21:42:53.168)
  matched the last render (21:42:53.244); no `WidgetRefreshWorker` run in logcat. A command's
  150 ms re-read saves a snapshot too (it is a poll).

- [x] **M4.6** — Real-device check, on the user's phone on the home WiFi. Nothing to build
  unless it finds a bug. Ask the user before unplugging the display (memory: no restart and no
  panel on/off without asking).
  **Verify:** the widget values match `curl http://desk.local/api/status`; the refresh button
  and the tap work; after ~15 min with the app closed, "as of" moved (logcat shows one worker
  run). The M3.6 skipped checks, if the user agrees: unplug the display → the app marks data
  stale within ~10 s and logs one `rediscover()`; replug → fresh data without an app restart;
  app in the background → no polls in logcat. Then delete the M3.6 revisit entry. The user
  confirms.
  Note: closed by the user, 2026-09-26: they use the app daily and report issues. The M3.6
  revisit entry is closed with it.
