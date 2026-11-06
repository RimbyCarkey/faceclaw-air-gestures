# Native band development and upstream updates

This is a real GitHub fork of jimrandomh/faceclaw. The root keeps its source/history
so Faceclaw can review small native-input and optional-host changes upstream.
Air Gestures is vendor/air-gestures, a pinned submodule with its own band-core
submodule. Clone both codebases together:

```sh
git clone --recurse-submodules https://github.com/RimbyCarkey/faceclaw-air-gestures.git
cd faceclaw-air-gestures
```

The native module builds the full Air Gestures source and resources, not a reduced
copy. Host patches are applied to native/air-gestures/build/upstream, leaving the
upstream worktree untouched. Keep integration code in app/band/, native/air-gestures/
and integrations/air-gestures/; avoid reformatting unrelated Faceclaw files.

## Build

Install Node 24, NativeScript CLI 9.1.2, JDK 21, Android SDK 36/build-tools 35.0.1,
Android NDK, Rust 1.99.1 with aarch64-linux-android target. Set JAVA_HOME,
ANDROID_HOME and ANDROID_NDK_HOME. Then:

```sh
git submodule update --init --recursive
npm ci
bash scripts/build-neural-band-rust.sh
ns build android
```

The native-band feature is enabled by default. FACECLAW_NEURAL_BAND=0 omits its
AAR and restores upstream Android SDK requirements for comparison builds. The
integration build requires Android 13+ ARM64. The normal app/test build outputs,
JNI binaries, keystores and private settings are not committed.

## Upstream releases

upstreams.lock.json records the audited Faceclaw/Air Gestures commits, band-core
commit and latest-release baselines. Initial Air Gestures pin includes work after
its recorded release; checking the baseline does not roll it backward.

`python3 scripts/upstream-sync.py` checks GitHub and GitLab release feeds without
changing source. The daily/manual Check upstream app releases workflow uses
--apply: merge a new Faceclaw release into a review branch, advance Air Gestures'
submodule to a new release, update the lock, and provide a link to provide a link to open an internal PR. It dispatches
Native band Android on that branch because bot-created PRs do not automatically
trigger every GitHub workflow. Merge conflicts or changed patches fail visibly
for review. No upstream update is silently merged into main.

Native band Android tests and builds on main pushes, PRs and manual dispatches,
producing a downloadable debug APK artifact. Successful source-sync builds are
for review, not automatic installation. Debug artifacts from different runners
can have different signing keys. Stable install-over updates require a maintained
release-signing key and a separately approved release; no signing key is published
or uploaded from a developer machine during this setup.

The embedded app updater points to the fork's unified releases. Air Gestures'
standalone APK would not be a valid integration update. Hardware firmware updates
remain explicit Air Gestures features. Upstream PRs are not sent to the original
maintainers automatically; this fork is organized to make those contributions
reviewable when requested.

## First native integration validation

1,064 JavaScript tests passed (3 skipped), with TypeScript typecheck passing. The
complete Android APK built locally. Six headless emulator scenarios passed:
full JNI load, phone-profile restoration and automatic-app association preservation, glasses-mode-only
native dispatch, unified manager/setup/mode UI, and isolated sign-in cookies.
Physical band and G2 validation is still pending.

## Maintainer-reported phone test — 2026-11-06

The native.2 preview passed phone-only checks on a physical Zinwa Q25 running
Android 17 (ARM64). Installation, interactive preview, native manager access,
mode/profile restoration and relaunch persistence passed. Original mappings and
app associations remained unchanged; no crash or ANR was reported.

The observed clipping belongs to the phone's own sizing configuration; the
maintainer has a phone-side solution. No application layout fix is needed.
The full phone checklist/report and screenshots are not published here.

Actual band input, handwriting/text entry, enrollment, firmware operations and
G2 delivery have not been tested on physical hardware. Phone-only results do not
remove those upstream-contribution requirements.
