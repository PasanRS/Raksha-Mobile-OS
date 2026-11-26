# Attributions, Licenses & Upstream Credits

**Raksha Mobile OS** stands on the shoulders of extraordinary open-source projects. Every upstream project, author, and license is credited here and preserved in our build artifacts.

> **Important Repository Scope Note**
> No proprietary vendor firmware, Samsung blobs, Google APKs, or full system images are committed to this Git repository. Our integration with upstream [OneOS](https://github.com/fahadalijaved/OneOS) is published as a clean source patch ([`oneos-patch/raksha-mobile-os-v0.5.36.patch`](oneos-patch/raksha-mobile-os-v0.5.36.patch)) that applies on top of upstream OneOS `v0.5.36`, respecting upstream's `NOTICE` regarding [OneLineage](https://github.com/WeWaffles/OneLineage)-derived session scripts.

---

## Core Platform & Dual-Boot Engine

| Project | Authors / Maintainers | License | Role in Raksha Mobile OS |
|---|---|---|---|
| **[OneOS](https://github.com/fahadalijaved/OneOS)** (`v0.5.36`) | Fahad Ali Javed & The OneOS Authors | Apache-2.0 (see upstream `NOTICE`) | Container-based dual-boot switcher running Android 17 guests beside stock Samsung One UI without wiping partitions |
| **[OneLineage](https://github.com/WeWaffles/OneLineage)** | WeWaffles | Original upstream permission in OneOS `NOTICE` | Origin of OneOS's container session architecture |
| **[MysticGSI](https://github.com/MysticGSI/mysticgsi)** | The MysticGSI Authors (incl. TrebleDroid by Pierre-Hugues Husson) | Apache-2.0 | GSI porting toolchain used to prepare the Android 17 system image |
| **[crDroid Android](https://github.com/crdroidandroid)** & **[LineageOS](https://github.com/LineageOS)** | crDroid & LineageOS Contributors | Apache-2.0 / GPL-2.0 | Android 17 (SDK 37) AOSP base framework and system components |
| **[OneUIProject (SESL / oneui-design)](https://github.com/OneUIProject)** | Yanndroid, BlackMesa123 & contributors | MIT | Native One UI UI library used in OneOS & OneOSGuest |

---

## Bundled Open-Source AI, Security & Utility Stack

| Component | Source / Upstream | License | Role in Raksha Mobile OS |
|---|---|---|---|
| **[llama.cpp](https://github.com/ggml-org/llama.cpp)** (`b12633`) | Georgi Gerganov & ggml-org contributors | MIT | Native ARM64 on-device LLM inference server (`127.0.0.1:8081`) |
| **[Qwen3.5-4B](https://huggingface.co/Qwen)** (`Q4_K_M` via Unsloth GGUF) | Alibaba Qwen Team / Unsloth | Apache-2.0 | Baked 2.6 GB on-device 4B LLM (`/system/etc/openclaw/models/`) |
| **[OpenClaw](https://github.com/openclaw/openclaw)** & **OpenClaw Android** | OpenClaw Contributors | MIT | Local loopback autonomous agent gateway (`127.0.0.1:18789`) |
| **[Kali NetHunter](https://www.kali.org/get-kali/#kali-mobile)** | Offensive Security / Kali Linux Team | GPL-2.0 / GPL-3.0 | Pre-installed NetHunter, Terminal, KeX, static ARM64 `busybox_nh`, and offline Full ARM64 RootFS (`1.75 GB`) |
| **[Dolphy](https://github.com/unvoiddd/Dolphy-App)** (`v2.7`) | `unvoiddd` | GPL-3.0 | Wireless protocol research & hardware emulation multi-tool (NFC, BLE, IR) |
| **[Termux](https://github.com/termux/termux-app)** (`v0.118.3`) | Termux Developers | GPL-3.0 | Native Linux terminal environment on Android |
| **[RootlessJamesDSP](https://github.com/ThePBone/RootlessJamesDSP)** (`1.6.14`) | Tim Schneeberger (`ThePBone`) & James Fung | GPL-3.0 | Audio DSP engine |
| **[F-Droid](https://f-droid.org/)** (`2.0.1`) | F-Droid Contributors | GPL-3.0 | Open-source Android app repository |
| **[NeoZygisk](https://github.com/JingMatrix/NeoZygisk)** & **[Vector](https://github.com/JingMatrix/Vector)** | JingMatrix Contributors | GPL-3.0 | Guest runtime & Zygisk/Xposed framework managed by OneOS |
| **[MindTheGapps](https://gitlab.com/MindTheGapps/vendor_gapps)** (`17.0.0`) | MindTheGapps Maintainers | Google Proprietary (redistribution per MTG packaging) | Minimal Android 17 Google Play Services & Play Store support |

---

## Raksha Mobile OS Project Maintainer

- **PasanRS** — Raksha Mobile OS integration, Sinhala **"රාක්ෂ"** visual identity & RRO branding overlays (`raksha/rros/`), `RakshaControl` native onboarding & awakening hub (`raksha/control/`), on-device `llama.cpp` + OpenClaw loopback integration (`raksha/scripts/`), and OneOS `v0.5.36` guest `libhwui` + single-wizard patches (`oneos-patch/`).

## Trademarks
Not affiliated with Samsung Electronics, Google LLC, OPPO, OnePlus, or Offensive Security. Galaxy and One UI are trademarks of Samsung Electronics; Android and Pixel are trademarks of Google LLC; Kali NetHunter is a trademark of Offensive Security.
