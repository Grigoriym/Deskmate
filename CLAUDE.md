# Deskmate

Android app (KMP/Compose) for the user's ESP32 desk display (`../esp32-desk-display`). It
finds the display on the home WiFi, shows its data, and switches its screen and panel.
Personal use, LAN only, no store release for now.

## Device contract

`../esp32-desk-display/docs/API.md` and `../esp32-desk-display/docs/api/status.example.json`
describe the device. Link them, don't copy facts from them. If the app and API.md disagree,
API.md wins; changes to the contract happen in the firmware repo, not here.

## How work happens here

One checklist step per session: "read `docs/CHECKLIST.md` and do step `<N>`". The workflow
rules are at the top of that file. Do exactly that step, run its Verify line, tick it, update
the banner.

**Before starting a step:** check `git status` and the current branch. A prior session can
leave uncommitted work or a feature branch behind. Commit stray work separately (or ask);
don't fold it into the new step.

**Status:** see the `docs/CHECKLIST.md` banner.

## What this file is not

**Point, don't copy.** Where another file owns a fact, link it and stop. A version
number, a progress marker, a task list restated here is a second copy free to drift
from the first, and it will.

- Status / progress → `docs/CHECKLIST.md` banner
- Versions → `gradle/libs.versions.toml` (from M0.1)
- Architecture rationale → `docs/IMPLEMENTATION_PLAN.md`

**Not a growing catalogue, either.** A convention that fits in a sentence or two, with
at most one example, belongs here. The moment a rule starts accumulating dated,
confirmed cases ("also true for X (date)... and for Y (date)...") or a worked-example
script, it has become reference material earned by a specific investigation, not a
day-to-day rule every session needs to read — split it into its own doc under `docs/`
and leave a one-line pointer where the rule used to live. Do this the first time a
section outgrows the rest of the file, not after several more sessions have added to it.

## Shared skills and agents

Skills and agents live in the **`agentic-grappim`** repo and are symlinked into
`~/.claude/skills/` and `~/.claude/agents/`. They are wired up **per machine, not per
clone** — there is nothing in this repo to install.

Consequences worth knowing before touching one:

- **An edit there changes behaviour in every project on this machine.** That is the
  point of the repo, not a hazard to avoid — but it has to be said out loud, and it
  has to be committed in `agentic-grappim`, not here.
- **Never edit a shared skill for a fact about this project.** One occurrence in one
  project is project knowledge and belongs in this file.
- A stale in-repo copy of a shared agent will silently shadow the real one. Don't
  duplicate them locally.

`.claude/agents/` in this repo holds **only** project-specific agents: none.

## Close-out

At the end of each checklist step (and any other non-trivial task), without being asked:

1. Run the **`/finalize` skill** — the work almost always taught something the plan
   didn't know, and this is where it gets written down instead of dying with the
   context.
2. **Check the docs for claims the work just made false.** Grep for what changed
   rather than trusting a read-through; a file full of stale present-tense statements
   is actively misleading, and the next session will cite it as current. Correcting it
   is part of the work, not a follow-up.
3. One commit per checklist step, subject `M<n>.<m>: <what>` (wayprint's format). Push —
   see "Branches and PRs" in `docs/CHECKLIST.md` for where.

## Changing a check means saying so

The gates constrain what a session may write; nothing constrains a session from
widening a gate so its own work passes. Loosening one is often right — doing it
quietly never is.

Say so explicitly, in the commit message and in the report, whenever you:

- suppress, ignore, or disable a rule, warning, or test
- lower a threshold, floor, or limit
- delete a rule from this file, or a step from a plan document
- change lint, formatting, dependency, or CI configuration

A suppression **carried over from another project** is worth deleting and re-running
first — it is often for a problem this toolchain doesn't have.

## Verification

**"Done" means the relevant check ran and passed.** If it didn't run, say that instead.

- **A narrow pass proves your change works, not that you broke nothing.** When a
  failure shows up alongside your change, A/B it against a clean tree (`git stash -u`)
  before assuming you caused it — or that you didn't.
- **A check that looks the same whether the thing worked or not is not a check.**
  Before trusting one, name what it would show if the change had done nothing.
- **Before/after comparisons need equally fresh runs.** A baseline taken from a
  partially cached build measures a different universe than the after-run. A moved
  total is a signal the two runs aren't comparable, not a result.
- **Confirm the baseline shows the pre-change value before trusting it**, and copy each
  report to a distinct path immediately. Overwrite the "before" once and the diff comes
  back showing nothing changed anywhere — which reads like a plausible result rather
  than a mistake.
- **An A/B of a Gradle test forces the test to run** (`--rerun` on the test task). M2.4:
  after a change to a Koin `includes` list, `testAndroidHostTest` came back `UP-TO-DATE`, and
  "BUILD SUCCESSFUL" looked like a pass.

## Plain technical English

Write for a reader whose first language is not English. The model is ASD-STE100
Simplified Technical English, **minus its approved-word dictionary** — that list isn't
something I can verify against, so the rules below are the binding part, not "STE
compliance".

- **One word per idea.** Pick a term and reuse it. No synonyms for variety.
- **Short sentences.** Around 20 words for an instruction, 25 for an explanation. One
  instruction per sentence.
- **Active voice.** "Run the task", not "the task should be run".
- **One topic per paragraph**, six sentences at most.
- **Domain terms are fine.** Identifiers, task names, library names and API names are
  technical names — use them exactly, don't paraphrase them into plain words.

Where it yields: **uncertainty and conditions win over brevity.** If a claim holds only
under some condition, say the condition even if the sentence gets long. A short
sentence that drops the caveat is wrong, not simple.

## Chat replies

Answer in chat as a tl;dr: short, plain, human, straightforward. I will not read a
novel there — give me the result and the next step, not the reasoning that got you
there.

- This is about chat only. Docs, code, comments, commit messages: write them however
  the artifact and this file's other rules call for.
- If something genuinely doesn't compress — a real tradeoff, a caveat that changes the
  answer — explain it in full. Don't let that become the default excuse for length.

## Working agreements

**Tradeoff:** these bias toward caution over speed. For trivial tasks, use judgment.

### Think before coding

Don't assume. Don't hide confusion. Surface tradeoffs.

- State assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them — don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.
- **An answer is not an instruction to act.** If the user states a preference or decision that a
  later, not-yet-requested step will need, record it for when that step is asked for — don't treat
  it as authorization to run the step now.

### Simplicity first

Minimum code that solves the problem. Nothing speculative.

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask: "would a senior engineer say this is overcomplicated?" If yes, simplify.

### Surgical changes

Touch only what you must. Clean up only your own mess.

- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor what isn't broken. Match existing style, even if you'd do it
  differently.
- Don't add UI or navigation that wasn't asked for.
- Remove imports, variables and functions **your** change orphaned. Leave pre-existing
  dead code alone — mention it instead.

**Every changed line should trace directly to the request.**

### Don't break production in favor of tests

Production code must not be shaped by testing needs. If a code path is flaky or can't
be observed deterministically as written, fix or remove the *test* — don't add a seam,
injectable parameter, or abstraction to production code purely so a test can control
it. This holds even when the change is small, additive, and provably safe (e.g. a
defaulted constructor parameter verified not to affect the DI graph) — the question is
not "is this change safe," it's "does this belong in production code at all."

Prefer, in order: (1) a lower-level test that already covers the behavior
deterministically without the racy synchronization; (2) simplify the test to avoid it;
(3) delete the test and say so plainly, rather than leaving a known flake undocumented.

Always ask before adding any production-code testability seam, even a well-verified
one.

### Determinism over process

If a task has one correct, computable answer, use a tool for it. Don't ask the agent to
follow a fixed procedure by hand.

- A checksum, a sort order, a date calculation, a schema check: write a script or a
  hook. Call it as a tool.
- An agent following prose steps can skip a step, or get one wrong. A script cannot.
- Reserve the agent's judgment for what needs judgment: ambiguous input, a plan, a
  choice between options.
- Writing a new skill: find a step that says "always do X the same way." Replace it
  with a tool call, not a longer instruction.

Ask: "does this step have one right answer, computable without judgment?" If yes,
write the tool, not the instruction.

### Goal-driven execution

Turn a task into a verifiable goal — "fix the bug" becomes "write a failing test, then
make it pass". For multi-step work, state the steps with a check each, then loop until
they pass.

### A real problem outside the task goes in writing

Write it into `docs/revisit.md` and keep going. Not fixed inline — that makes the
diff unreviewable. Not dropped. And **not just mentioned in chat: chat is not
persistence.** Give the entry enough evidence (`file:line`, or a link) that a cold
session can act on it without re-deriving anything.

### Friction goes in writing too

The rule above is for problems in the **code**. This one is for friction in the
**tooling**, and it is the one that silently never gets reported: a guessed URL that
404s, an auth error on something another tool already reaches, a command that needed
different quoting, a check that confidently returned the wrong answer. The reflex is to
route around it and say nothing.

Add a line to `docs/frictions.md` before moving on — **create the file if it isn't there**,
that is not a decision worth pausing over:

```markdown
# Frictions

Tooling friction hit during work, newest last. One line each. Promoted or fixed
entries get deleted — see /finalize.

- 2026-08-05 — `rg` with an unquoted `**` glob: zsh expanded it first and failed with
  "no matches found" before rg ran. Quote the glob, or use `--files`.
```

One line, past tense, naming the tool and the surprise. Don't stop working to write it
and don't editorialise. **This is for what you routed around without mentioning** — not
for failures you were going to report anyway.

At the end of the task, **read the file** and list what you added, with a count, even when
the count is zero. Read it rather than recalling it: small friction is gone from recall by
then, which is the whole reason the file exists. And a silent miss must not look like a
smooth run.

The same friction three times is a fix, not a fourth line — a permission entry, a line in
this file, or a skill. `/finalize` is where that promotion happens.

## Settled decisions

Weighed and declined — don't re-propose these.

| Not used | Instead | Why |
|---|---|---|
| F-Droid/Play release, store flavors, release signing | Debug APK installed by hand | User, 2026-09-26: "for now no stores". Revisit only when asked |
| Full `feature:*:{data,domain,dto,mapper,ui}` layering | Lean module list, IMPLEMENTATION_PLAN.md §4 | One data source, one screen, one widget |
| `grappim-kit/build-logic` via `includeBuild` | Own copy of wallosmobile's `build-logic` | User, 2026-09-26: it links the repos locally; same model rejected for the other apps |
| Per-host cleartext `network-security-config` | `usesCleartextTraffic="true"` | Display IP is DHCP-assigned; can't be listed in advance |

## Reference projects

Read these rather than guessing; the conventions here are ported from them.

- `../wayprint` — the most recent app built from zero. `docs/CHECKLIST_ARCHIVE.md` M0 is
  the model for this app's M0 (build-logic port, skeletons, Koin, `KoinGraphTest`).
- `../wallosmobile` — `build-logic` (incl. `KmpNetworkConventionPlugin`), Ktor client setup,
  `usesCleartextTraffic`.
- `../grappim-kit` — shared modules on Maven Central. Read `CONSUMING.md`'s section for a
  module before adding it; one `grappimKit` version key for all of them.

**Trust their code over their docs.** Another project's `CLAUDE.md` can contradict its
own implementation — wayprint and wallosmobile have both had exactly that. Note a drift here when
you find one.
