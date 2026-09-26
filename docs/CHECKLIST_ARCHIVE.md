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
