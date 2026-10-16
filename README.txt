КАТЯ 2.0 — Android voice assistant prototype

WHAT WORKS
- Russian voice recognition via installed Android speech service, microphone permission
- Russian text-to-speech via installed Android TTS engine
- Manual text commands as a fallback
- Open Todoist, ChatGPT, camera, Android settings, calendar
- Open system alarm screen with numeric HH:MM time
- Open route in an installed map application
- Neon-themed native UI, tappable examples

NOT IMPLEMENTED
- Continuous wake word listening with locked screen
- Direct Todoist API sync (opening the app is not synchronization)
- Autonomous ChatGPT conversation (no API access)
- Smart planning, location monitoring or productivity tracking

BUILD APK WITH GITHUB
1. Create a private GitHub repository.
2. Upload the CONTENTS of this ZIP to repository root, including .github directory.
3. Open Actions > Build Katya 2 APK > Run workflow (or push to main).
4. Download the Katya2-debug-apk artifact, unzip it and install app-debug.apk.
5. Grant microphone permission. Install a Russian speech recognition / TTS service if missing.

The APK build has NOT been run locally: Android SDK/Gradle are unavailable in this environment.
