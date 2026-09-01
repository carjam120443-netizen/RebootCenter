# ⚡ RebootCenter

A modern Android reboot and device-control center with a clean Jetpack Compose interface and optional **Shizuku** integration.

> 🚧 **Early development:** RebootCenter is currently a starter project. Device-specific capabilities and privileged actions are being added incrementally.

## ✨ Planned features

- 🔄 Restart / reboot
- ⚡ Reboot to bootloader / fastboot when supported
- 🛠 Reboot to recovery when supported
- 🔌 Power off when supported
- 🔐 Shizuku connection and permission status
- 📱 Device and Android version information
- 🌙 Modern dark/light UI
- 🎨 Animated Material-style controls
- 🤖 GitHub Actions builds

## 🔐 Shizuku

RebootCenter is designed to use Shizuku for operations that are available through its privileged shell interface. The app **does not bypass Android security restrictions**. If an operation is unavailable on a particular Android version or device, RebootCenter should report that state instead of pretending it succeeded.

The current Shizuku release line is **13.6.x**, which includes Android 16 QPR1 support. citeturn0search2

You must install and start Shizuku separately and grant RebootCenter permission before features that require Shizuku can work.

## 🧰 Tech stack

- Kotlin
- Jetpack Compose
- Android Gradle Plugin
- Material 3
- Shizuku API
- GitHub Actions

## 📦 Building

Open the project in Android Studio and let Gradle sync. Then build the debug APK with:

```bash
./gradlew assembleDebug
```

The debug APK will be placed under `app/build/outputs/apk/debug/`.

## 📱 Compatibility

RebootCenter targets modern Android releases while keeping unsupported operations disabled gracefully. Actual reboot capabilities depend on Android version, device manufacturer, permissions, and whether Shizuku is running.

## ⚠️ Safety

Rebooting or powering off a device interrupts running applications and unsaved work. Bootloader/recovery operations can also behave differently across manufacturers. RebootCenter will not perform destructive actions such as wiping data as part of its normal reboot menu.

## 📄 License

License to be added.
