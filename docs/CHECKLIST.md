# Deskmate checklist

**Current step:** M0.7 — M0.6 done 2026-09-26 (repo `Grigoriym/Deskmate`, public; CI green).

## How to use this

Each step is done in **one fresh session with zero memory of prior sessions**:

1. Start a fresh session and say "read `docs/CHECKLIST.md` and do step `<N>`."
2. Do exactly that step. Don't pull later steps forward.
3. Pass the step's **Verify** line before calling it done.
4. Tick the box, update the **Current step** banner, add a one-line `Note:` if anything
   deviated.
5. Commit (see "Branches and PRs" below), then run `/finalize`.
6. When a milestone is fully ticked, move it verbatim into `docs/CHECKLIST_ARCHIVE.md` in
   the same commit.

## Branches and PRs

- **Pure `master`, no branches, no PRs** (user, 2026-09-26: "for now pure master"). One
  commit per step, pushed to `master` (`https://github.com/Grigoriym/Deskmate`) straight away.
  Committing and pushing are one action here — don't hold a commit back "until told to push".
  Only switch to branches/PRs when the user says so, for the scope they name.

Ground rules (the *why* is in `docs/IMPLEMENTATION_PLAN.md`):
- Root `CLAUDE.md` overrides this file if they conflict.
- `../esp32-desk-display/docs/API.md` is the device contract. Re-read it at the start of any
  step that touches the API; it may have changed since this checklist was written.
- A prior session's claim ("already checked X") gets re-verified, not trusted.
- Tests go in the same step as the logic. Hand-written fakes, no mocking library.
- `ktlint` and `detekt` pass before a step is ticked.
- Milestones after M1 get broken into steps at the start of that milestone
  ("step-break-down" commit, as in wayprint) — the rough scope below is not binding.

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

- [ ] **M0.7** — Tell the other projects the app exists: update the `deskmate` paragraph in
  `../grappim-watcher/CLAUDE.md` (added 2026-09-26, planning-only) with the repo URL and
  status, and add a pointer from `esp32-desk-display` ROADMAP step 12 to this repo (that
  repo's own PR/commit rules apply).
  **Verify:** both pointers name the right path/repo URL.

## M1 — API client (`core:api`)

- [ ] **M1.1** — DTOs for `/api/status`, mirroring API.md field-for-field; every section
  nullable; `ignoreUnknownKeys`. Copy `status.example.json` into test resources, with a comment
  naming its source path and the `esp32-desk-display` commit it came from.
  **Verify:** a test decodes the fixture and asserts every field; a second test decodes it
  with every section set to `null`; a third decodes it with an extra unknown field.

- [ ] **M1.2** — `DeskApi`: `status()`, `screen(go)`, `panel(set)` with typed values (no raw
  strings at call sites). 3-5 s timeouts, requests serialised (one at a time), results as a
  sealed type: success / HTTP error (status + body) / offline (timeout, refused, unknown host).
  **Verify:** tests against Ktor `MockEngine` for each outcome, incl. 400/405/500 bodies and a
  timeout; a test proves two concurrent calls don't overlap.

- [ ] **M1.3** — Real-device smoke test: point the client at the actual display.
  **Verify:** status parses from `http://desk.local` (or the IP) and a `panel?set=toggle`
  visibly flips the panel; the user confirms.

## M2 — discovery (`core:discovery`) — break down at start

NSD discovery filtered by service name `Desk display` → resolve host+port; `desk.local`
fallback; manual IP entry; remember the last good host; re-discover when the saved host
goes offline. Check `grappim-kit-storage` fits for the saved host before writing a local store.

## M3 — status screen and controls (`feature:display`) — break down at start

Domain model + formatting (rain text, pollen bands, AQI label, warning pick, BVG
`LEAVE IN`/`GO NOW`/`HURRY` with `walk_min`/`walk_comfort`, 1970 date before clock sync),
all tested. UI: one card per section, each independently `null`-safe; poll every 5 s only
while visible; offline → last data marked stale; screen/panel buttons → re-read after
~150 ms, tolerate the up-to-10 s delay.

## M4 — Glance widget (`widget`) — break down at start

Outdoor/indoor temp + next departure. Refresh policy is decided (IMPLEMENTATION_PLAN.md §5):
15 min WorkManager + refresh on app poll, widget tap and a refresh button. Show the data's
age on the widget, since it can be up to 15 min old.

## M5 — release — deferred

No stores for now (2026-09-26). Until that changes: debug APK installed by hand. Scope this
milestone only when the user asks for a signed build or a store.
