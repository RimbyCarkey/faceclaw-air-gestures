# Upstream contribution status

This fork follows the Contributing section of the root README.

- Faceclaw's original GPLv3 LICENSE is preserved. New integration code was written
  in this development session; the existing Faceclaw and Air Gestures code is
  attributed to its original authors, not presented as newly authored work.
- Air Gestures and band-core are pinned external dependencies. Their MIT licenses
  and font/icon/dictionary notices remain in the unmodified upstream trees.
- PRIVACY describes the new Meta account/hardware/firmware interactions, optional
  Android access, native input handling and GitHub/GitLab release checks, with
  provider privacy-policy links. Meta sign-in/claiming is optional and uses explicit
  user actions; no assumption is made that supplying an API key covers this flow.
- Automated tests, builds and emulator checks are recorded in the development doc.
  They are not described as physical, real-world testing.
- The maintainer reported a phone-only pass on 2026-11-06 using native.2 on a
  physical Zinwa Q25 (Android 17, ARM64): installation, touchscreen preview,
  native manager screens, glasses/phone profile switching, and relaunch persistence.
  Original phone mappings and app associations were preserved. No crash or ANR
  was reported. This is not verification of actual band gestures or G2 delivery.
- Reported clipping is specific to the maintainer's phone-side sizing setup. The
  maintainer has a solution; no Faceclaw layout change is requested or required.
- Physical band/G2 integration, handwriting and actual profile-action execution
  remain untested and are still required before upstream submission.

Do not submit this integration upstream as fully hardware-tested until its real-world
requirements have been verified and recorded. No PR to jimrandomh/faceclaw has
been opened during this setup.
