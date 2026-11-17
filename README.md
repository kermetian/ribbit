# Ribbit

![An ink-drawn frog with pale green markings leaps over a bewildered spy in a trenchcoat, leaving broken tracking chains behind.](.github/media/ribbit-banner.png)

**Share the link. Leave the trackers behind.**

Ribbit is a small Android share target that removes known tracking and attribution parameters from links before passing them to the app you actually want to share with.

No copy-and-paste detour. Just bounce the link through Ribbit.

## The idea

A shared link should point to a song, a video, or an article—not carry unnecessary information about who shared it or which campaign brought you there.

Ribbit adds a small cleaning step to Android's existing share flow. It removes explicitly recognized tracking fields while keeping the useful parts of a link: content IDs, playback timestamps, playlists, and other unrecognized parameters. Cleaning happens locally on your device, without visiting the link or contacting a cleaning service.

Also prevents workaround flows that involve copy-pasting to another app and then manually removing them. Apps with link preview tools like social / messaging apps (even privacy preserving open source apps) could in theory queue a request to a third party app server to generate the preview or cache links. We prevent that happening before you manually remove any personally identifiable tracking content leaving less of an attack surface, like the OS itself or a custom system level intent selection activity.

## How to use it

1. Tap **Share** on a link in your browser or another app.
2. Choose **Bounce!**, Ribbit's entry in the Android share sheet.
3. Choose the destination app from the share sheet that opens with the cleaned text.

The share sheet is the main interface; you do not need to open Ribbit's launcher screen first. Ribbit accepts shared plain text, including messages containing multiple HTTP(S) links, and preserves the surrounding text and whitespace.

For example:

```text
Before
https://www.youtube.com/watch?v=VIDEO_ID&si=SHARE_TOKEN&is=ANOTHER_TOKEN&t=30

After
https://www.youtube.com/watch?v=VIDEO_ID&t=30
```

The video and its starting timestamp stay. The recognized share tokens go.

## What it cleans

- Common campaign and click-attribution fields, including `utm_*`, `gclid`, and `fbclid`.
- Service-specific share parameters on supported URL families, including YouTube, Spotify, Instagram, Facebook, Reddit, and TikTok.
- Known affiliate and referral-credit tags on supported links, including Amazon and Apple media links. Removing that attribution is intentional.

Rules are scoped by hostname and, where needed, path. Ribbit does not assume that a generic name such as `source`, `ref`, or `t` means tracking everywhere.

The [tracking catalog and research notes](docs/url-tracking-research.md) describe the complete rules, evidence, exceptions, and process for extending coverage.

## What it leaves alone

- Unknown query fields and functional content, playback, and access parameters outside the removal catalog.
- URL fragments and links embedded inside another URL's query values.
- Opaque short-link paths: Ribbit does not expand redirects or try to decode hidden attribution from an identifier.
- URLs with recognized AWS or Google Cloud signature parameters, because changing their query can invalidate access.
- Unsupported or malformed URLs, rather than guessing how to repair them.

**Cleaner links are not anonymity.** Ribbit does not stop cookies, fingerprinting, IP-based tracking, or server-side attribution. The catalog is not exhaustive, and the signature safeguard does not recognize every signing scheme. Sites can also repurpose parameter names; please report links whose behavior changes after cleaning.

## Build and run

Requires **Android 7.0 / API 24 or newer** to run.

For development, open the project in a compatible Android Studio installation with **Android SDK 37** available. The checked-in Gradle daemon criteria select **JDK 25**, and the project configures Foojay toolchain resolution.

Build a debug APK:

```sh
./gradlew :app:assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`. You can also run the app directly from Android Studio on a device or emulator.

Run the focused URL-cleaning tests:

```sh
./gradlew :app:testDebugUnitTest --tests com.github.kermetian.ribbit.ScrubTest --console=plain
```

## Improve the catalog

Found a tracking field that survives, or a link that breaks? Include a **sanitized public example**, the parameter involved, and the behavior that should be preserved. Do not post real account identifiers, private invite links, access tokens, or signed credentials.

Changes should have a clear scope, supporting evidence, and regression examples for both removal and preservation. See the [investigation process](docs/url-tracking-research.md#repeatable-investigation-and-extension-process) before adding a rule.
