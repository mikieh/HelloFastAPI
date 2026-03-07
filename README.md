# CallFinder

An Android app for managing, archiving, and searching call recordings with AI-powered transcription.

## Features

- **Call Recording Scanner** - Scans device storage for call recordings, extracts metadata from filenames and Android CallLog
- **Google Drive Upload** - Backs up recordings to a dedicated `CallFinder/` folder in Google Drive
- **AI Transcription** - Uses Gemini 2.5 Flash to transcribe calls, extract keywords, and generate summaries
- **Full-Text Search** - Search across phone numbers, contacts, dates, keywords, and transcripts
- **Audio Player** - Built-in player with ExoPlayer, displaying transcript and editable keywords
- **Scheduled Sync** - Background sync via WorkManager (daily/weekly/monthly)

## Tech Stack

- Kotlin + Jetpack Compose + Material Design 3
- MVVM + Clean Architecture
- Hilt (DI), Room (DB), WorkManager (background sync)
- Retrofit + OkHttp (networking)
- ExoPlayer / Media3 (audio playback)
- EncryptedSharedPreferences (secure API key storage)
- Google Sign-In + Drive API v3

## Setup

1. Clone the repository
2. Open in Android Studio (Hedgehog or newer)
3. Add your `google-services.json` for Google Sign-In
4. Build and run on a device/emulator (API 26+)
5. Enter your Gemini API key in Settings to enable transcription

## Project Structure

```
app/src/main/java/com/callarchive/app/
├── data/          # Data layer (DB, API, scanner, repositories)
├── di/            # Hilt dependency injection modules
├── domain/        # Domain layer (models, interfaces, use cases)
├── presentation/  # UI layer (screens, ViewModels, theme)
├── worker/        # WorkManager sync components
├── CallFinderApp.kt
└── MainActivity.kt
```
