# Mi Freeform (Android 15 Remake)

An open-source floating window and multitasking manager for Android, inspired by MIUI/HyperOS floating windows and remade from [sunshine0523/Mi-Freeform](https://github.com/sunshine0523/Mi-Freeform) with modern Jetpack Compose, an interactive window size manager, Android 15 windowing API enhancements, and full Bahasa Indonesia localization.

---

## 🚀 Key Features

- **Interactive Window Size Manager**:
  - Live phone display mockup with interactive drag-to-resize corner handle.
  - Real-time aspect ratio calculation (16:9 Cinema, 4:3, 1:1, 3:4, 9:16).
  - Fine-tuning sliders for Width %, Height %, X/Y screen position, corner radius, and window opacity.
  - Built-in presets (*Standard Float, Compact Mini, Split Left, Split Right, 16:9 Cinema, Desktop Square*) and custom presets.

- **Per-App Window Rules**:
  - Automatically open specific apps in custom window dimensions (e.g. YouTube in 16:9, Calculator in Mini Float, Browser in Split Screen).

- **Android 15 & Multi-Tasking Engine**:
  - Target SDK 36 (Android 15+) with full edge-to-edge layout.
  - Uses `ActivityOptions.makeBasic().setLaunchBounds(Rect)` and `FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_MULTIPLE_TASK`.
  - Android Quick Settings Tile (`MiFreeformTileService`) to toggle the floating dock from the notification shade.
  - Floating Edge Dock (`MiFreeformOverlayService`) that docks on the screen edge and expands into a multitasking drawer.
  - Developer Options & Shizuku/ADB integration for system-wide freeform mode.

- **Built-in Floating Mini-Apps**:
  - 🧮 **Floating Calculator**: Quick arithmetic, history, draggable titlebar, minimize to bubble.
  - 📝 **Floating Scratchpad**: Persistent note-taking tool over any app.
  - ⏱️ **Floating Stopwatch**: Lap tracker and timer.
  - 🌐 **Floating Web Browser**: Live WebView with search and navigation bar.

- **Dual Language Support**:
  - Full English and Bahasa Indonesia (Indonesian) translations with in-app language switching under Settings.

---

## 🛠️ Enabling System Freeform Windows (AOSP / Stock Android)

To allow any app to be launched in freeform windows on stock Android:

1. Enable **Developer Options** on your device (tap *Build Number* 7 times in *Settings > About Phone*).
2. Inside Developer Options, toggle **Enable Freeform Windows** and **Force activities to be resizable**.
3. Or run the following ADB command:
   ```sh
   adb shell settings put global enable_freeform_support 1
   adb shell settings put global force_resizable_activities 1
   ```
4. Restart your device.

---

## 📦 GitHub Release & Automated Builds

This repository includes a GitHub Actions workflow (`.github/workflows/release.yml`). 
Whenever a version tag is pushed (e.g., `v1.0.0`), GitHub Actions will automatically compile the APK and attach `app-debug.apk` directly to the GitHub Release page!
