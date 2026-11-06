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
- Physical, real-world testing has not yet been completed and recorded. Actual
  band/G2 integration testing remains a requirement before upstream submission.

Do not submit this integration upstream as hardware-tested until its real-world
requirements have been verified and recorded. No PR to jimrandomh/faceclaw has
been opened during this setup.
