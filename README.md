# Still

A minimalist Android launcher built around attention, not app icons.

Still gives you a text-first home screen, a fast searchable drawer, and a Focus pack that
puts real limits on the apps that tend to eat your day.

## What Still adds

- **Daily app limits** — Give any app a daily time budget. When the budget is spent, tapping
  that app opens a calm block screen instead of the app. Budgets reset every calendar day.
- **Focus windows** — Define recurring windows (for example 22:00–07:00, or work hours)
  during which a chosen set of apps is paused entirely. Windows may cross midnight, and each
  one has its own weekday selection.
- **Smart search** — Drawer search ranks matches by how often you actually open each app, so
  the app you mean surfaces first. Apps with equal or no history keep alphabetical order.
- **Backup & Restore** — Export favourites, folders, hidden apps, focus limits, schedules and
  every preference to a single JSON file, then restore it later.
- **Focus widget** — Today's screen time, whether a focus window is active, and your tightest
  remaining app budgets, at a glance on the home screen.

## Launcher basics

- **Minimalist home screen** — Text-first layout with optional app icons. Keep a few
  favourites visible, or leave the home screen empty.
- **Fast app search** — Searchable drawer with A-Z fast scrolling and optional automatic
  opening of the only matching result.
- **App management** — Reorder favourites, group apps into folders, rename or hide apps, set
  per-app launch delays, and open app info or uninstall from the launcher.
- **Personalisation** — Fonts (including your own `.ttf`), text size, spacing, alignment,
  themes, wallpaper, icon packs and icon placement.
- **Useful extras** — Optional clock, battery level, notification dots, screen-time widget,
  swipe gestures, and double-tap to lock.
- **Privacy** — No tracking, no analytics, no network permission. Everything stays on device.

## Permissions

Still asks only for what a launcher needs. Three are special:

| Permission | Why |
|---|---|
| Usage access | Read per-app foreground time to enforce limits, show screen time, and rank search. Nothing leaves the device. |
| Accessibility | Only for double-tap to lock the screen. |
| Notification access | Only to draw notification dots on app entries. |

## Build

Requires JDK 17+ and the Android SDK with platform 37.

```bash
./gradlew :app:assembleFossDebug
./gradlew :app:assemblePlayStoreRelease
```

Flavours: `foss` (no billing) and `playStore` (billing + in-app review).

## Tech stack

- Kotlin, Coroutines and Flow
- Jetpack Compose, Navigation, Lifecycle, ViewModel
- Room (schema migrations included), DataStore
- Hilt + KSP
- Timber
- Minimum SDK 26, target SDK 37

## Note on origin

Still is a fork of [Minimo Launcher](https://github.com/VaibhavLakhera/minimo-launcher),
which is MIT licensed. The launcher foundation — home screen, drawer, folders, theming,
icon packs, widgets and settings — comes from that project, and this repository keeps its
copyright notice and license intact.

The Focus pack (daily limits, focus windows, the block screen, smart search), Backup &
Restore, and the focus home screen widget are additions made for this fork.

## License

MIT. See [LICENSE](LICENSE).

Original work Copyright (c) 2024 Vaibhav Lakhera.
