# Voice Room Companion (Android starter)

## Features
- One-tap launch of installed imo (package name can vary by region/distribution)
- Manual Kick/Remove and Disconnect event logging
- Local history of the last 100 events
- Local notification when an event is logged
- No internet permission, account password collection, or remote storage

## Important limitation
This app does **not** automatically detect or prevent an administrator's server-side Kick. The event buttons record an event after the user observes it. Automatic detection needs a reliable permitted signal from imo, such as an official API or an explicit system notification. The app cannot override room administrators.

## Build
This project is in the `voice-room-companion/` subfolder. GitHub Actions can build it using the workflow at `.github/workflows/build-voice-room-companion.yml`. Run the workflow manually from the Actions tab and download the `Voice-Room-Companion-APK` artifact.

## Privacy
Events are stored only in this app's private local preferences. No internet permission is declared. Uninstalling the app clears the local history.
