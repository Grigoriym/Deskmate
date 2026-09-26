# Deskmate checklist

**Current step:** M3.4 — M3.3 done (BVG in the domain model), 2026-09-26.

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

- [ ] **M3.4** — Poll loop and commands, in `feature:display:ui` (`commonMain`; a `ViewModel`
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

- [ ] **M3.5** — Status screen. Replaces the M2.4 proof screen in `DeskmateAppContent`.
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

- [ ] **M3.6** — Real-device check, on the user's phone on the home WiFi. Nothing to build
  unless it finds a bug.
  **Verify:** every section shows the same values as `curl http://desk.local/api/status`; each
  screen button changes the panel; panel off/on works; the user confirms. Unplug the display →
  data marked stale within ~10 s, logcat shows one `rediscover()`; plug it back → data fresh
  again without an app restart. Background the app → logcat shows no polls.

## M4 — Glance widget (`widget`) — break down at start

Outdoor/indoor temp + next departure. Refresh policy is decided (IMPLEMENTATION_PLAN.md §5):
15 min WorkManager + refresh on app poll, widget tap and a refresh button. Show the data's
age on the widget, since it can be up to 15 min old.

## M5 — release — deferred

No stores for now (2026-09-26). Until that changes: debug APK installed by hand. Scope this
milestone only when the user asks for a signed build or a store.
