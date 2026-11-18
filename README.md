# Material Notes

A native Android note-taking app built with Google's official Jetpack Compose Material 3 components.

## Features

- Create and edit notes with a title and body
- Automatically persist notes on the device
- Search note titles and content
- Pin important notes
- Archive notes to keep the main list tidy
- Move notes to Trash, restore them, or permanently delete them
- Grid and list layouts
- Note color choices
- System, Light, and Dark appearance
- Android 12+ Dynamic Color support

Notes are stored locally on the device. The app does not need an account or network access.

## Build an APK with GitHub Actions

No Android Studio installation is required.

1. Open the **Actions** tab in this repository.
2. Select **Build Material Notes APK**.
3. Open the latest successful run.
4. Download the **MaterialNotes-debug-apk** artifact.
5. Extract the ZIP to get `app-debug.apk`.

The workflow also builds automatically whenever changes are pushed to `main`.

## Technology

- Kotlin
- Jetpack Compose
- Google's Material 3 for Compose
- Android Gradle Plugin 9.3.1
- Kotlin 2.4.21
- Compose BOM 2026.09.00
- Gradle 9.5.0
