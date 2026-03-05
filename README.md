# 📺 StreamTV — Modern IPTV App

A modern, feature-rich IPTV application built with Kotlin, following **MVVM Clean Architecture** with the latest Android Jetpack libraries.

---

## ✨ Features

| Feature | Description |
|---|---|
| 🎬 **Live Streaming** | HLS, DASH, and RTMP stream playback via ExoPlayer/Media3 |
| 📋 **M3U Playlist Support** | Import any `.m3u` or `.m3u8` playlist via URL |
| 🔍 **Smart Search** | Real-time channel search with instant results |
| 🏷️ **Category Filter** | Filter channels by group/category with chip UI |
| ❤️ **Favorites** | Save and manage favorite channels |
| 🕐 **Recently Watched** | Auto-tracks your last 20 watched channels |
| 📱 **Picture-in-Picture** | Continue watching while using other apps |
| 🌙 **Dark Theme** | Modern dark UI with Material Design 3 |
| 💾 **Offline Database** | Room DB persists all channels locally |
| 🚀 **Fast & Efficient** | Kotlin Coroutines + Flow for smooth performance |

---

## 🏗️ Architecture

```
streamtv/
├── data/
│   ├── local/          # Room Database (entities, DAOs)
│   ├── model/          # Domain models (Channel, Playlist, UiState)
│   ├── remote/         # M3U Parser, networking
│   └── repository/     # ChannelRepository (single source of truth)
├── di/                 # Hilt dependency injection modules
└── ui/
    ├── home/           # Home & Favorites screens
    ├── channels/       # Channel list & adapter
    ├── player/         # ExoPlayer + PlaybackService
    └── settings/       # Playlist management
```

**Pattern:** MVVM + Repository + StateFlow/LiveData

---

## 🛠️ Tech Stack

| Library | Purpose |
|---|---|
| **Kotlin** | Primary language |
| **Hilt** | Dependency injection |
| **Room** | Local SQLite database |
| **Media3 / ExoPlayer** | Video playback (HLS, DASH) |
| **OkHttp** | HTTP client for M3U fetching |
| **Coroutines + Flow** | Async & reactive data |
| **Navigation Component** | Fragment navigation |
| **Glide** | Image loading for channel logos |
| **Material Design 3** | Modern dark UI components |
| **DataStore** | User preferences storage |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 17
- Android SDK 26+

### Setup
```bash
git clone https://github.com/yourname/StreamTV.git
cd StreamTV
```

Open in Android Studio and sync Gradle.

### Build
```bash
./gradlew assembleDebug
```

### Install on device
```bash
./gradlew installDebug
```

---

## 📱 How to Use

1. **Open the app** — You'll see the Home screen
2. **Add a Playlist** — Tap ⚙ Settings → "Add Playlist" → Enter name + M3U URL
3. **Browse Channels** — Tap 📺 Channels to see all channels
4. **Search** — Use the search bar to find channels by name or category
5. **Filter by Category** — Tap category chips to filter
6. **Watch** — Tap any channel to launch the full-screen player
7. **Favorites** — Tap ♡ on any channel to add to favorites

---

## 📺 Supported Stream Formats

- **HLS** (`.m3u8`) — Recommended for live TV
- **DASH** (`.mpd`)
- **MP4 / MKV** direct streams
- **RTMP** (via ExoPlayer RTMP extension)

---

## 📂 Project Structure

```
StreamTV/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           ├── java/com/streamtv/app/
│           │   ├── StreamTVApp.kt
│           │   ├── data/
│           │   ├── di/
│           │   └── ui/
│           └── res/
│               ├── drawable/
│               ├── layout/
│               ├── menu/
│               ├── navigation/
│               ├── values/
│               └── xml/
├── build.gradle.kts
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
├── gradle.properties
└── settings.gradle.kts
```

---

## 🔧 Extending the App

### Add EPG (Electronic Program Guide)
- Implement `EpgRepository` to fetch XMLTV format EPG data
- Add `EpgEntity` to Room and display schedule in the player

### Add Stream Quality Selection
- Use Media3's track selector API
- Expose available quality levels in player UI

### Add Chromecast Support
- Add `media3-cast` dependency
- Implement `CastPlayer` alongside `ExoPlayer`

---

## 📝 License

MIT License © 2024 StreamTV
