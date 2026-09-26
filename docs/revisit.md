# Revisit

Real problems found outside the current task. One entry each, with `file:line` or a link so a
cold session can act on it. Delete an entry when it's fixed.

- 2026-09-26 (M0.2) — `config/detekt/detekt.yml` is wayprint's file copied whole. It carries
  wayprint's rule choices, incl. Compose rules turned off with a "todo investigate" comment:
  `LambdaParameterInRestartableEffect` (line 893), `MultipleEmitters` (line 935),
  `ModifierMissing` (line 909), `CompositionLocalAllowlist` (line 862). Turn each back on and
  run `./gradlew detekt` once real UI exists (M3); keep one off only for a real finding.
