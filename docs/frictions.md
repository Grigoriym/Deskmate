# Frictions

Tooling friction hit during work, newest last. One line each. Promoted or fixed
entries get deleted — see /finalize.

- 2026-09-26 — `gh repo view grappim/<repo>` failed with "Could not resolve to a Repository": the
  GitHub owner is `Grigoriym`, not the `grappim` folder name. `git remote -v` in a sibling repo has it.
- 2026-09-26 — `./gradlew <test task> --rerun -q | grep -v PASSED` printed nothing on success, the
  same as if no test ran. Test counts had to come from `build/test-results/**/*.xml`.
- 2026-09-26 — `./gradlew build | tail; echo EXIT=$?` printed `EXIT=0` on a failed build: `$?` was
  `tail`'s. Use `${PIPESTATUS[0]}`.
