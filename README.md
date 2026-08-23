# KernelSU X

A fork of [KernelSU-Next](https://github.com/KernelSU-Next/KernelSU-Next) — an advanced Kernel based root solution for Android devices, with extra fixes and UX improvements.

> Ядро и root-драйвер идентичны upstream. Отличия — в менеджере и инфраструктуре.

## Differences from upstream

| Change | Details |
|--------|---------|
| 📦 Auto-detect zip kind | One install entry instead of two: AnyKernel3 vs module zip is detected automatically ([upstream #1246](https://github.com/KernelSU-Next/KernelSU-Next/issues/1246)) |
| 🌐 WebUI fix | Module WebUI file operations (`writeFile`, `removeFile`, `moveFile`, `copyFile`) return correct results on success |
| 🔤 Language crash fix | No longer jumps to the OEM system locale picker that crashes on some ROMs; locale applied via `LocaleManager` on Android 13+. Indonesian selectable again ([upstream #987](https://github.com/KernelSU-Next/KernelSU-Next/issues/987)) |
| ⬆️ Fresher deps | 73 Rust dependency updates for ksud |
| 🔄 Auto-sync CI | Weekly automatic merge of upstream changes |

## Downloads

Grab APKs from the [Releases](https://github.com/SprizanX/KernelSU-Next/releases) page or build them via the **Build Manager CI** workflow in the Actions tab.

APK variants:
- **Regular** — standard build.
- **Spoofed** — randomized package name for environments that check the manager's identity.

> APKs are signed with a development key: uninstall any previously installed manager before installing this one.

## Compatibility

Same as upstream: kernels 4.4 – 6.6 (GKI 2.0 prebuilt/LKM support for 5.10+), architectures `arm64-v8a`, `armeabi-v7a`, `x86_64`.

The application id remains `com.rifsxd.ksunext` for kernel/module compatibility.

## Building

```bash
# manager APK
cd manager
./gradlew assembleRelease

# full CI build happens automatically on push to dev
```

## Credits

Built on the work of the [KernelSU-Next](https://github.com/KernelSU-Next/KernelSU-Next), [KernelSU](https://github.com/tiann/KernelSU) and [Magisk](https://github.com/topjohnwu/Magisk) teams.

## License

- `/kernel` directory: GPL-2.0-only
- Everything else: GPL-3.0-or-later
