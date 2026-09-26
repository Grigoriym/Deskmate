# Deskmate checklist

**Current step:** M3 break-down — M2 (discovery) done 2026-09-26.

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
