# Legacy Profiler JSON contract — 1.0

`summary.json` requires `schema_version`, `profiler_version`, `build_version`, start/end timestamps, duration, recording mode, `profile`, `metadata`, `validation_warnings`, `stages`, `counters`, `profiler_statistics`, `extensions`, and `sampled_traces`. `profile.source` is always present and identifies the consumer that initiated the session. `profile.display_name` and `profile.purpose` are omitted when not supplied. A stage object's `source` independently identifies the stage provider.

```json
"profile": {
  "source": "ExampleMod",
  "display_name": "Example Mod",
  "purpose": "Network performance investigation"
}
```

`timeline.csv` retains its original seven columns and appends `record_type`, `profile_source`,
and `profile_purpose`. Its first data row is `session_metadata` and carries attribution once;
later rows leave those values empty and are classified as `sample` or `event`.

Readers must ignore unknown fields. Minor releases may add optional fields and extension values; they will not remove, rename, or reinterpret required 1.0 fields. A breaking contract increments the schema major version. Legacy compatibility fields remain readable throughout schema 1.x.

In implementation version 1.0.0, `extensions` is always an empty object. Although metadata and
timeline provider interfaces exist in the source tree, built-in discovery and writers do not
consume them yet; integrations must not rely on extension values being exported.
