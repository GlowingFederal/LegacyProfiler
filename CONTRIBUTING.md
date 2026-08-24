# Contributing and issue reports

Legacy Profiler's source is public, but contributions and use remain subject to the project license.
The final source-available license text is still pending; discuss contribution terms with the
copyright holder before submitting substantial code.

When reporting a problem, include the Legacy Profiler version, Java version, host mod version,
Minecraft/Forge versions (when applicable), recording mode, reproduction steps, and the generated
`profile.log`. Attach `summary.json` or `timeline.csv` only after checking it for environment and mod
information you do not wish to disclose.

For API issues, include the stage registration and balanced `enter`/`exit` call site. For export
issues, include the relevant exception and whether the destination under `logs/legacyprofiler` was
writable. Do not attach generated run directories or compiled artifacts to source changes.
