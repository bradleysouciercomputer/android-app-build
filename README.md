# Firestick Message

A minimal Android / Amazon Fire TV app. It displays a single centered, inert button-shaped control reading **This app runs on firestick**.

## Automatic APK build (no Android Studio required)

1. Open the **Actions** tab in this repository.
2. Select **Build Fire TV APK**.
3. Click **Run workflow** (or wait for a build after a push).
4. When the run completes, open it and download the **FirestickMessage-debug-apk** artifact.
5. Extract the downloaded ZIP to get `app-debug.apk`.

The workflow builds a debug APK using Java 17, Gradle 8.9, and Android SDK Platform 35. The app includes the Android TV `LEANBACK_LAUNCHER` category and does not require a touchscreen.

## App behavior

The button is deliberately non-interactive: it cannot be clicked, focused, or activated. The app requests no network permissions and contains no login, menus, or extra features.

## Manual build

With Java 17, Gradle 8.9, and Android SDK Platform 35 installed, run:

```bash
gradle assembleDebug
```

The APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.