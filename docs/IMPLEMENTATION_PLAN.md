# Deskmate implementation plan

This is the *why* document. For the *what next*, see `docs/CHECKLIST.md`. If the two ever
disagree, `CLAUDE.md` (root) wins over both.

## 1. Goal

An Android app for the ESP32 desk display (`../esp32-desk-display`). It finds the display
on the home WiFi, shows everything the panel shows, and switches the panel's screen and
on/off state. A Glance home-screen widget shows the most-glanced values. This is
`esp32-desk-display`'s ROADMAP step 12.

Not in scope: history or charts (the device keeps none; readings already go to the home
InfluxDB/Grafana), remote access outside the LAN, firmware configuration.

## 2. The device contract

**`../esp32-desk-display/docs/API.md` is the only source of truth for the device.** This
plan does not copy it. Facts that shape the app's design:

- LAN only, plain HTTP on port 80, no auth. The app must allow cleartext traffic.
- Discovery: NSD `_http._tcp.`, filter by service name `Desk display`. Fallback:
  `desk.local`, then a manual IP. The IP comes from DHCP and can change.
- `GET /api/status` — one JSON object. Every section (`outdoor`, `indoor`, `air`,
  `warning`, `next_holiday`, `bvg`) can be `null` independently. `bvg` is `null` for long
  stretches (the upstream API is often down).
- `POST /api/screen?go=…`, `POST /api/panel?set=…` — queued, applied within ~300 ms, up to
  ~10 s during a device fetch. Re-read status ~150 ms later.
- Small server: one request at a time, ~7 connections. Short timeouts (3-5 s), no
  parallel requests, poll ≥ 1 s (5 s while visible, 30-60 min+ for the widget).
- Timeout / refused = "display offline": keep the last data, mark it stale.
- Unknown JSON fields must be ignored (`ignoreUnknownKeys`).
- Parsing fixture: `../esp32-desk-display/docs/api/status.example.json`. The firmware has a
  host test that keeps that file equal to its real output.

## 3. Reference projects

- **`../wayprint`** — the most recent app bootstrapped from scratch. Its
  `docs/CHECKLIST_ARCHIVE.md` M0 is the model for this app's M0 (build-logic copy, module
  skeletons, Koin skeleton, CLAUDE.md template merge).
- **`../wallosmobile`** — source of `build-logic` (incl. `KmpNetworkConventionPlugin`,
  which wayprint skipped) and of the Ktor client setup. It already sets
  `usesCleartextTraffic="true"` (self-hosted Wallos), so it is also the precedent for that.
- **`../grappim-kit`** — shared KMP library on Maven Central
  (`io.github.grigoriym:grappim-kit-*`). Read `grappim-kit/CONSUMING.md` before adding a
  module. Use one `grappimKit` version key in `libs.versions.toml` for all of them.
- **`../agentic-grappim`** — shared skills (wired globally via `~/.claude/skills/`) and
  `templates/CLAUDE.md.template`.

## 4. Module layout (lean)

Small app: one data source, one main screen, one widget. No
`feature:*:{data,domain,dto,mapper,ui}` split — that layering exists in wallosmobile for many
backends/features and would be empty structure here.

| Module | Role | Notes |
|---|---|---|
| `androidApp` | Android entry point, manifest (cleartext, network permissions) | thin |
| `composeApp` | DI root, app shell, navigation | |
| `core:api` | Ktor client, DTOs, `DeskApi` (status, screen, panel), error → result mapping, request serialisation (one at a time) | `commonMain`; fixture-tested against `status.example.json` |
| `core:discovery` | find the display: NSD (`androidMain`), `desk.local`, manual IP; remember the last good host | `expect`/`actual` or an interface with an Android impl |
| `feature:display:domain` | UI-ready model: rain text, pollen levels, AQI band, warning, `LEAVE IN`/`GO NOW`/`HURRY`, staleness | pure Kotlin, the logic that gets tests |
| `feature:display:ui` | status screen (one card per section) + screen/panel controls, polling while visible | |
| `widget` | Glance widget (outdoor/indoor temp, next departure) | Android-only |
| `strings` | CMP string resources | |

From `grappim-kit` (check `CONSUMING.md` per module when added): `logger`, `coroutines`,
`uikit`, `testing`, and `navigation` only if a second screen (settings/manual IP) is added.
`storage` does not fit the saved host (checked 2026-09-26, kit `0.1.7`: no key-value store);
`core:discovery` keeps its own small DataStore store instead.

Deferred until needed: desktop/JVM target (possible later — `core:api` and the domain stay
in `commonMain` so it stays cheap), iOS, `benchmark`, `detekt-rules`.

## 5. Decisions and open questions

Decided here (change by editing this table):

| Topic | Choice | Why |
|---|---|---|
| Name | `deskmate`, package `com.grappim.deskmate` | companion to "Desk display"; picked 2026-09-26, rename is cheap until M0.1 |
| Stack | KMP + Compose Multiplatform + Koin + Ktor, Android target first | same as the other apps; ROADMAP 12 asked for Kotlin/Compose |
| Build logic | copy `wallosmobile/build-logic` and rename, as wayprint did (duplicated, not shared) | user, 2026-09-26: `grappim-kit/build-logic` would mean linking the repos locally via `includeBuild`, the same model already rejected for the other apps. Don't re-propose |
| Distribution | none for now: debug/sideload builds only, no F-Droid/Play, no store flavors | user, 2026-09-26 ("for now no stores"). Revisit in M5 |
| Widget refresh | as fresh as Android allows: periodic WorkManager at the 15 min minimum, plus a refresh when the app polls, on widget tap, and a refresh button on the widget | user, 2026-09-26 ("let's do what we can"). 30-60 s isn't possible in the background |
| Cleartext | allowed for the whole app | the display IP comes from DHCP, so a per-host network-security-config can't list it; the app only ever talks to the LAN |
| Model source | DTOs mirror `API.md` field-for-field; domain maps them | the JSON is the contract; keep display logic out of DTOs |

Open (decide in the step that needs it, don't guess):

- **Several displays.** API assumes one; the app assumes one too unless the user says
  otherwise.
