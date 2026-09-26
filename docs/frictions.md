# Frictions

Tooling friction hit during work, newest last. One line each. Promoted or fixed
entries get deleted — see /finalize.

- 2026-09-26 — `gh repo view grappim/<repo>` failed with "Could not resolve to a Repository": the
  GitHub owner is `Grigoriym`, not the `grappim` folder name. `git remote -v` in a sibling repo has it.
- 2026-09-26 — `./gradlew <test task> --rerun -q | grep -v PASSED` printed nothing on success, the
  same as if no test ran. Test counts had to come from `build/test-results/**/*.xml`.
- 2026-09-26 — `./gradlew build | tail; echo EXIT=$?` printed `EXIT=0` on a failed build: `$?` was
  `tail`'s. Use `${PIPESTATUS[0]}`.
- 2026-09-26 — A mutation check that made the ViewModel poll forever (`SharingStarted.Eagerly`) hung
  `testAndroidHostTest` past the 600 s Bash timeout: `runTest` kept advancing virtual time. Wrap
  mutation runs in `timeout`.
- 2026-09-26 — `pkill -f GradleWorkerMain` killed the calling shell too (exit 144): its own command
  line contains the pattern. Kill the PID from `pgrep -fa` instead.
- 2026-09-26 — Two mutations in one test run showed only one failing test: `configureTests()` sets
  `failFast = true` (`build-logic/.../Quality.kt`), so the run stops at the first failure. One
  mutation per run.
- 2026-09-26 — `adb -s <serial> logcat` after the phone disconnected: adb printed "waiting for device" and blocked until the 120 s timeout. Check `adb devices` first, or use `adb wait-for-device` with a `timeout`.
