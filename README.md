# NCAA Basketball Scores App

An Android app that shows NCAA Men's and Women's college basketball scores.

## Features
- View scores for any date using the date picker
- Toggle between Men's and Women's games
- Live scores show the current clock and period
- Refresh button and pull down to get the latest scores
- Works offline using previously saved scores

## How to Run
1. Open the project in Android Studio
2. Click the green **Run** button
3. Select your emulator or connected Android device

## How to Test
Basketball season runs **November through March**. Try these dates:
- `Feb 15, 2026` — lots of games
- `Feb 17, 2026` — lots of games
- `Mar 13, 2026` — March Madness week

Weekends have the most games. If a date shows "No games found", try a different date.

## Project Structure
```
app/src/main/java/edu/nd/pmcburne/hwapp/one/
│
├── MainActivity.kt          # Entry point, launches the app
├── GameViewModel.kt         # Fetches data, holds app state
│
├── network/
│   ├── ApiClient.kt         # Retrofit setup
│   └── ApiModels.kt         # JSON data classes
│
├── database/
│   └── Database.kt          # Room database (offline storage)
│
└── ui/
    └── GameScreen.kt        # All the UI / composables
```

## API
Data comes from `https://ncaa-api.henrygd.me` which proxies ESPN's NCAA scoreboard.

[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/NYuLn2p4)
