# Deskmate checklist

**Current step:** M2.1 — M2 broken down 2026-09-26 into M2.1-M2.4.

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

- [ ] **M2.1** — Saved host store in `core:discovery`: read, save and clear one host string
  (for example `http://192.168.0.147`, the form `DeskApi.baseUrl` takes). DataStore Preferences
  in `commonMain`; the Android file path and Koin provider in `androidMain`, as wallosmobile's
  `StorageModule` does. Catalog: DataStore under one version key; take the current version,
  not wallosmobile's pin without a check. `core:discovery` then no longer needs its
  `Placeholder`.
  **Verify:** `commonTest` against a DataStore on a temp file: empty store reads `null`; a saved
  host reads back; it survives a new store instance on the same file; `clear()` empties it.
  `./gradlew build` green.

- [ ] **M2.2** — NSD finder: an interface in `commonMain` (one call: "find the display, or
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

- [ ] **M2.3** — `HostLocator` in `commonMain`: runs the search order above and exposes the
  state (searching / found host / not found) as a `StateFlow`. Also `setManual(ip)` (probe,
  then save; an unreachable IP is not saved) and `rediscover()` (skip the saved host, search
  again). The probe uses `DeskApi.status()`; decide in the step whether `core:discovery`
  depends on `core:api` or takes the probe as a function, and note why.
  **Verify:** tests with fakes for the store, the NSD finder and the probe: saved host up →
  found without NSD; saved host down → NSD finds a new IP → the store holds the new IP; NSD
  finds nothing → `desk.local` is tried; nothing passes → not found and the store is not
  cleared; manual IP unreachable → not saved. `./gradlew build` green.

- [ ] **M2.4** — Wiring: Koin provides `HostLocator`, and `DeskApi` with `baseUrl` from the
  found host. `KoinGraphTest` gets `Context` in `extraTypes`. The placeholder screen shows the
  locator state and, when found, the status `time` (the M0.4 greeting goes). This is a
  temporary proof, replaced by M3's screen.
  **Verify:** on the user's phone on the home WiFi: first launch shows the display found via NSD
  (logcat names the path that won), then the status time. Put a wrong IP in the store (for
  example with a debug `adb` command); the next launch falls back to NSD and saves the right
  IP. `KoinGraphTest` passes; `./gradlew build` green.

## M3 — status screen and controls (`feature:display`) — break down at start

First: API.md gained a `co2` section (esp32-desk-display `0a9838e`, 2026-09-26). Add it to
`StatusDto` and re-sync the `StatusExampleJson.kt` fixture to that commit (see the M1.1 fixture
entry in `docs/revisit.md`). The current app ignores the field, so nothing breaks before then.

Domain model + formatting (rain text, pollen bands, AQI label, CO2 band per API.md's `co2`
table, warning pick, BVG
`LEAVE IN`/`GO NOW`/`HURRY` with `walk_min`/`walk_comfort`, 1970 date before clock sync),
all tested. UI: one card per section, each independently `null`-safe; a not-found state with
the manual IP field (M2's `setManual`); poll every 5 s only while visible; offline → last data
marked stale, and call M2's `rediscover()`; screen/panel buttons → re-read after
~150 ms, tolerate the up-to-10 s delay.

## M4 — Glance widget (`widget`) — break down at start

Outdoor/indoor temp + next departure. Refresh policy is decided (IMPLEMENTATION_PLAN.md §5):
15 min WorkManager + refresh on app poll, widget tap and a refresh button. Show the data's
age on the widget, since it can be up to 15 min old.

## M5 — release — deferred

No stores for now (2026-09-26). Until that changes: debug APK installed by hand. Scope this
milestone only when the user asks for a signed build or a store.
