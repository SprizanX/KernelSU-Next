<div align="center">

<img src="assets/kernelsu_next.png" width="110" alt="KernelSU X logo"/>

# KernelSU X

**Advanced kernel-based root solution for Android**

A maintained fork of [KernelSU-Next](https://github.com/KernelSU-Next/KernelSU-Next)
with extra fixes & UX improvements in the manager app and CI infrastructure.

[![Release](https://img.shields.io/github/v/release/SprizanX/KernelSU-Next?label=release&logo=github&color=success)](https://github.com/SprizanX/KernelSU-Next/releases/latest)
[![Build Manager CI](https://github.com/SprizanX/KernelSU-Next/actions/workflows/build-manager-ci.yml/badge.svg?branch=dev)](https://github.com/SprizanX/KernelSU-Next/actions/workflows/build-manager-ci.yml)
[![Sync with upstream](https://github.com/SprizanX/KernelSU-Next/actions/workflows/sync-upstream.yml/badge.svg?branch=dev)](https://github.com/SprizanX/KernelSU-Next/actions/workflows/sync-upstream.yml)
![Platform](https://img.shields.io/badge/platform-Android%204.4%20%E2%80%93%206.6-green?logo=android)

</div>

> [!NOTE]
> The kernel driver is identical to upstream — all differences live in the **manager app** and **build infrastructure**.

## ✨ Differences from upstream

| Change | Details |
|--------|---------|
| 📦 Auto-detect zip kind | One install entry instead of two: AnyKernel3 vs module zip is detected automatically ([upstream #1246](https://github.com/KernelSU-Next/KernelSU-Next/issues/1246)) |
| 🌐 WebUI fix | Module WebUI file operations (`writeFile`, `removeFile`, `moveFile`, `copyFile`) return correct results on success |
| 🔤 Language crash fix | No longer jumps to the OEM system locale picker that crashes on some ROMs; locale applied via `LocaleManager` on Android 13+. Indonesian selectable again ([upstream #987](https://github.com/KernelSU-Next/KernelSU-Next/issues/987)) |
| 🎨 Manager polish | Accent color picker, Material You toggle and root status widget |
| ⬆️ Fresher deps | 73 Rust dependency updates for ksud |
| 🔄 Auto-sync CI | Weekly automatic merge of upstream changes |

## 📥 Downloads

Grab APKs from the **[latest release](https://github.com/SprizanX/KernelSU-Next/releases/latest)** or build them via the *Build Manager CI* workflow in the [Actions tab](https://github.com/SprizanX/KernelSU-Next/actions/workflows/build-manager-ci.yml).

| Variant | Use case |
|---------|----------|
| **Regular** | Standard build for everyday use |
| **Spoofed** | Randomized package name for environments that check the manager's identity |

> [!WARNING]
> APKs are signed with a development key — uninstall any previously installed manager before installing this one.

## 🔧 Compatibility

Same as upstream:

- Kernels **4.4 – 6.6** (GKI 2.0 prebuilt/LKM support for 5.10+)
- Architectures: `arm64-v8a` · `armeabi-v7a` · `x86_64`

The application id remains `com.rifsxd.ksunext` for kernel/module compatibility.

## 🛠️ Building

```bash
# manager APK
cd manager
./gradlew assembleRelease

# full CI build happens automatically on push to dev
```

## 🤝 Credits

Built on the work of the [KernelSU-Next](https://github.com/KernelSU-Next/KernelSU-Next),
[KernelSU](https://github.com/tiann/KernelSU) and [Magisk](https://github.com/topjohnwu/Magisk) teams.

## 📄 License

| Path | License |
|------|---------|
| `/kernel` | GPL-2.0-only |
| everything else | GPL-3.0-or-later |
