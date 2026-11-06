# Air Gestures host integration

- vendor/air-gestures: unmodified, recursively pinned upstream Git submodule.
- native/air-gestures: Android library packaging its full source/resources/assets/JNI.
- integrations/air-gestures/host.patch: narrow host adaptations and added actions.
- scripts/prepare-air-gestures.cjs: copies and patches sources under build/, failing
  when an upstream change no longer matches. The upstream checkout stays clean.
- app/band: direct native listener and first-class band input payloads.

The full Air Gestures UI is retained. Toggle destination by swapping profiles and
remembering the previous phone profile. There is one BandService transport owner.
Meta sign-in is isolated, secrets are backup-excluded, and APK update links point
to this fork's unified releases. Firmware update controls are unchanged.

The manifest is generated from upstream on every preparation. All screens,
services and receivers are retained, including the standard profile announcement
receiver. Only the app-level attributes, standalone launcher and Meta sign-in
process are adapted for the host. New upstream components are therefore not
silently omitted by a stale copied manifest.
