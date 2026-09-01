# ⚡ RebootCenter

A modern Android reboot and device-control center built with **Kotlin + Jetpack Compose + Shizuku**.

## ✨ Current features

- 🔄 Restart Android
- ⚡ Reboot to bootloader / fastboot when supported
- 🛠 Reboot to recovery when supported
- 🔌 Power off when supported
- 🔐 Detect whether Shizuku is running
- 🔑 Request Shizuku permission from the app
- 🟢 Enable reboot controls only after permission is granted
- 🛡️ No bootloader unlocking, data wiping, or partition flashing
- 🤖 Automatic APK builds with GitHub Actions

## 🔐 Shizuku

RebootCenter uses the official Shizuku API for privileged operations. Shizuku provides access to Android system capabilities through its ADB/root-backed service, but ADB permissions are limited and can differ between Android versions. RebootCenter therefore treats unsupported operations as unavailable instead of trying to bypass Android security.

Official Shizuku project: https://github.com/RikkaApps/Shizuku

Install and start Shizuku separately, then grant RebootCenter permission when the app requests it.

## 🧰 Tech stack

- Kotlin
- Jetpack Compose
- Material 3
- Android Gradle Plugin 8.13
- Shizuku API 13.6.0
- GitHub Actions

## 🤖 Automatic APK builds

Every push to `main` automatically starts the **Build APK** GitHub Actions workflow.

The workflow:

1. Checks out the repository.
2. Installs JDK 17.
3. Sets up Gradle 8.13.
4. Runs `gradle assembleDebug`.
5. Uploads the resulting debug APK as the `RebootCenter-debug-apk` workflow artifact.

You can also start the workflow manually with GitHub Actions → **Build APK** → **Run workflow**.

## 📦 Local build

Open the project in Android Studio and let Gradle sync, or run:

```bash
gradle assembleDebug
```

The APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 📱 Compatibility

Reboot behavior depends on the Android version, manufacturer, available shell permissions, and whether Shizuku is running. Bootloader and recovery targets are especially device-dependent.

## ⚠️ Safety

Rebooting interrupts apps and can discard unsaved work. RebootCenter intentionally does **not** include destructive commands such as factory reset, bootloader unlocking, or partition flashing.

## 📄 License

License to be added.
