# Deskmate checklist

**Current step:** M1.2 — M1.1 done 2026-09-26 (status DTOs, fixture tests).

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
