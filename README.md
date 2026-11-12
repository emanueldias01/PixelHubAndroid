# PixelHub Android

Real-time collaborative pixel art drawing application for Android.

## Overview
PixelHub allows users to connect to a shared canvas server, log in, and draw collaboratively in real time. The app synchronizes drawing actions across connected clients via WebSockets and HTTP REST services.

## Features
- Real-Time Collaboration: Synchronized canvas updates powered by WebSockets.
- Authentication: Session-based login and logout via REST API.
- Drawing Tools: Pencil, Eraser, Paint Bucket, and Pan & Zoom controls.
- Custom Palette: Color selection for drawing on the pixel grid.
- Active Session Tracking: Displays a list of online participants in real time.

## Architecture & Tech Stack
- Language: Kotlin 2.0
- UI: Jetpack Compose with Material 3
- Architecture: MVVM with Kotlin `StateFlow`
- Navigation: Jetpack Navigation Compose
- Networking:
  - Retrofit 2 & Gson (REST API)
  - OkHttp3 (WebSocket implementation)
- Target SDK: 36 | Min SDK: 24

## Project Structure
```text
app/src/main/java/br/com/sd/pixelhubandroid/
├── data/          # Data models and UI state classes
├── features/      # Jetpack Compose UI screens (LoginScreen, BoardScreen)
├── navigation/    # Compose Navigation setup
├── network/       # Retrofit client and API interface definitions
├── ui/theme/      # Theme definitions, colors, and typography
└── viewmodels/   # Business logic and WebSocket connection handling
```

## Setup and Usage
1. Prerequisites:
   - Android Studio (Ladybug 2024.2.1 or newer)
   - Android device or emulator running API 24+
   - A PixelHub backend server running on port `8080`
2. Build and run the app from Android Studio.
3. On launch, enter a username and the server IP address to join the session.
