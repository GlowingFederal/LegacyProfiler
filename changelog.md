# Change log

Changes are listed from oldest to newest. Each release-preparation entry names its implementing
commit when available.

## 1.0.0 repository readiness

- (b71993f Prepare Legacy Profiler for public integration) Replaced the template-facing repository
  presentation with accurate user, build, output, licensing, and developer integration guidance.
- Moved the standalone library to the repository's standard root source layout, made project and
  artifact identity consistent, and added a copyable consuming-mod example.
- Documented and clarified public API contracts and added cacheable stage lookup to the v1 facade.

## 1.0.0 standalone Forge mod

- (8e7f407 Convert Legacy Profiler into a standalone Forge mod) Added the Forge 1.7.10 mod
  container, shared-runtime initialization, operator session command, server-shutdown finalization,
  owned report location, Forge metadata, and a directly installable distribution build.
- Added Maven Local publication under `com.glowingfederal:legacy-profiler:1.0.0` and documented the
  required `required-after:legacyprofiler` consumer dependency and non-shaded deployment model.
- Reworked the user and API guides around consumer-only stage registration and the single runtime,
  while documenting the existing process-global stage-name limitation.
