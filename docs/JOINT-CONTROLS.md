# Joint Faceclaw + Air Gestures controls

This fork builds one Android APK. Faceclaw provides the G2 interface; Air Gestures
provides band enrollment, connection, profiles, handwriting, drawings, practice,
phone controls and firmware management. The band is a first-class Faceclaw input
source called **band**, with its own native **band-gesture** events. It does not
use the watch remote, a TCP connector, or Android automation broadcasts.

## First setup

1. Install this fork's unified APK on an Android 13+ ARM64 phone. If switching from
   another signer, protect your settings and export any enrolled band's confirmed
   owner bundle before uninstalling the previous app. Android does not allow us to
   bypass a signing mismatch or read another app's private owner keys.
2. Tap the **Band** tab beside **Watch** → **Band settings & Air Gestures** →
   **Enrollment, import and export**. The phone menu → Neural Band remains a shortcut.
   Enroll through Meta, or import an existing Air Gestures owner bundle. You do not
   need an owner bundle if enrolling from scratch. Read the claim dialog: claiming
   changes ownership. Keep exported bundles private.
3. Connect your band in **Air Gestures · all features**, and connect your G2 glasses
   in Faceclaw. Grant the Android permissions required for the features you use.
   Meta sign-in uses a separate process/WebView cookie store; keys and encrypted
   account sessions are excluded from Android backups.
4. Tap **Toggle glasses / phone mode** on the Band tab or the band settings page
   to choose where the band controls go. The Band tab shows the current mode,
   including changes made with the band gesture. Selecting **Faceclaw glasses**
   in Air Gestures also enters Glasses mode. Selecting a phone profile returns
   to Phone mode and keeps that profile selected.

## Glasses mode

This selects a dedicated **Faceclaw glasses** gesture profile while remembering your
previous phone profile. The new profile has a separate internal ID, so an older
prototype profile cannot hide its default mode-toggle mapping. Only this profile's Faceclaw actions route to the glasses.
Other Air Gestures feature screens remain accessible in the same APK. Automatic
phone-app profile associations are temporarily suspended in glasses mode and
resume unchanged in phone mode.

| Band gesture | Action in the default Faceclaw glasses profile |
|---|---|
| Index tap | Select/click |
| Middle tap | Back/double-click; normal Faceclaw sleep/wake behavior applies |
| Swipe up/down/left/right | Navigate in that direction |
| Index double tap | Open a draft for the current app, if it accepts text |
| Index pinch hold | Toggle back to phone mode |

If the global Draw control uses index hold, Glasses mode temporarily frees that
gesture for the mode toggle. Phone mode restores the saved Draw binding.

Band gestures also control the phone display in Preview Only. Open **Band settings & Air Gestures** to see recent gesture routing and shell results. The trace stays in memory.

Native input requires an active preview or unlocked glasses. The native
mode-switch action still works independently of the G2 connection, so you can
return to phone controls without reconnecting the glasses. Re-selecting the profile
does not overwrite your custom Faceclaw glasses mappings.

## Phone mode and a gesture to switch back

Phone mode restores the profile you used before entering glasses mode. Its media,
volume, navigation, drawing, dial/slider, accessibility and other mappings keep
their normal Air Gestures behavior; we do not replace that profile with a generic
watch layout.

To switch in both directions without reaching for the phone:

1. Open **Air Gestures · all features → Gestures**, then edit your phone profile.
2. Choose a gesture you can spare and assign **Faceclaw: toggle glasses / phone mode**.
3. Use that gesture to enter glasses mode. In the default Faceclaw profile, index
   pinch hold switches back. You can remap that toggle there too.

The app does not silently overwrite a phone gesture to make room for this. If you
delete the remembered phone profile, switching back uses another available phone
profile; if none remains, create one in Air Gestures first.

## Text and handwriting

Index double tap opens a draft bound to the original Faceclaw window. Edit it on
the phone, review it, and explicitly confirm delivery to the named destination.
Delivery can execute an action in that app. Switching windows, locking or losing
the G2 connection blocks delivery and retains the draft in this process.

Air Gestures handwriting accessibility can edit the focused phone draft field,
with its normal dictionary and correction features. Enable it through its Setup
screen in Android Accessibility settings. Native, phone-free handwriting capture
straight into the glasses composer is not complete. Drafts are process-local and
can be lost if Android terminates the app.

## Updates

Air Gestures source stays separate in a pinned Git submodule. Its host changes
are reviewed patches applied to a generated build directory, never edits inside
the upstream checkout. Faceclaw keeps normal fork history so changes can be sent
upstream as focused pull requests. Source-sync automation checks both projects'
releases, prepares update review branches, and dispatches test/APK builds. It does not silently
merge new upstream code or install an APK on your phone.

One installed APK needs one update channel: the embedded update UI points to this
fork's unified releases, not the standalone Air Gestures APK. Do not install a
standalone APK over this build expecting it to preserve the integration. Firmware
update tools are still Air Gestures' own explicit controls, not app updates.

## Current validation and limits

All existing feature screens and native implementations are included; this is not
a claim that every combination has been hardware-tested. Real band enrollment,
physical mode switching, firmware operations, handwriting accuracy and G2 delivery
need hardware testing. No physical device was flashed or enrolled during setup.

See the [fork privacy notice](../PRIVACY) for Meta services, account/session storage,
optional Android access and release checks.
