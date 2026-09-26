# Deskmate checklist

**Current step:** M4.5 — M4.4 (widget layout and refresh button) done 2026-09-26.

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

- [ ] **M4.5** — App poll refreshes the widget. Each successful poll in `DisplayViewModel` saves
  the snapshot and asks the widget to update. `feature:display:ui` is `commonMain`, Glance is not,
  so it talks to the widget through an interface: the interface exists for the module
  boundary, not for the test. Decide where it lives and which module depends on which (no
  cycle), and note it. Throttle the widget update if every 5 s is too often (Glance may drop
  updates); note what you find.
  **Verify:** `DisplayViewModel` `commonTest`: a successful poll saves one snapshot; a failed
  one saves none. `./gradlew build` green, `KoinGraphTest` passes. On the emulator: open the
  app, go home, the widget's "as of" matches the last app poll.

- [ ] **M4.6** — Real-device check, on the user's phone on the home WiFi. Nothing to build
  unless it finds a bug. Ask the user before unplugging the display (memory: no restart and no
  panel on/off without asking).
  **Verify:** the widget values match `curl http://desk.local/api/status`; the refresh button
  and the tap work; after ~15 min with the app closed, "as of" moved (logcat shows one worker
  run). The M3.6 skipped checks, if the user agrees: unplug the display → the app marks data
  stale within ~10 s and logs one `rediscover()`; replug → fresh data without an app restart;
  app in the background → no polls in logcat. Then delete the M3.6 revisit entry. The user
  confirms.

## M5 — release — deferred

No stores for now (2026-09-26). Until that changes: debug APK installed by hand. Scope this
milestone only when the user asks for a signed build or a store.
