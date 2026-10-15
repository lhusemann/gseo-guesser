# GeoGuesser for Android

An Android app inspired by GeoGuessr. You see a photo, guess where it was taken by tapping a spot on a world map, and score points based on how close you got.

I built it as my individual project for the **Grundlagen Software Engineering (GSE)** module at **Bielefeld University** in the winter semester 2023/24. It was developed from user stories with acceptance criteria, tracked in a ticket system, and checked with Checkstyle.

> **Note:** The app's user interface, the code identifiers, the comments and the commit messages are all in German.

---

## Features

- **Photo-based rounds:** each round shows a photo with a short description. The real location is read from the photo's **EXIF GPS metadata**.
- **Interactive map:** you place your guess by tapping an **OpenStreetMap** map (via [osmdroid](https://github.com/osmdroid/osmdroid)). Once you confirm, the map shows the correct location, draws a line between it and your guess, and zooms to fit both.
- **Distance-based scoring:** the distance is calculated with the **Haversine formula**, and points (up to 5,000 per round) fall off logarithmically as the distance grows.
- **Difficulty levels:** each level sets the distance range used for scoring:

  | Difficulty | Max points within | 0 points beyond |
  |------------|-------------------|-----------------|
  | Einfach (Easy)   | 20 m | 20 km |
  | Normal           | 10 m | 10 km |
  | Schwer (Hard)    | 5 m  | 5 km  |

- **Configurable games:** you choose an album, the number of rounds (up to the number of photos in the album) and the time per round (10–60 s, with a countdown timer).
- **Download albums from a server:** you enter a server URL, fetch the list of available albums as JSON (Volley + Jackson) and download one. Its images are saved to internal storage and can then be played like the built-in albums.
- **Built-in albums:** *Ostwestfalen*, *Italien* and *Urlaub Österreich*.
- **Results screen:** shows each round's score and the total, and lets you restart with the same settings or go back to the main menu.
- **Route link:** after each round, a link opens an OpenStreetMap route from your guess to the real location.
- **Error handling:** dedicated exceptions cover albums without images, photos with missing or corrupt EXIF data, and invalid distances.

## Tech Stack

| Area | Technology |
|------|------------|
| Language | Java 8 |
| Platform | Android (minSdk 29, targetSdk 33) |
| Build | Gradle 8, Android Gradle Plugin 8.1 |
| Maps | osmdroid (OpenStreetMap / MAPNIK tiles) |
| Networking | Volley |
| JSON | Jackson Databind |
| Image metadata | AndroidX ExifInterface |
| Testing | JUnit 4, AssertJ |
| Code quality | Checkstyle, Javadoc |

## Architecture

The app uses an **MVC-style architecture** built on `java.beans.PropertyChangeSupport`:

- **Model:** `Game` is the single model. It holds the game state: album, round counter, timer, markers, scores and difficulty. It is kept in the `GameApplication` so all activities share it.
- **View/Controller:** the activities (`MainActivity`, `SpielansichtActivity`, `EndActivity`, `DownloadActivity`) send user input to the model. They implement `PropertyChangeListener` and update the UI when the model fires events such as `"Runde"`, `"Timer"`, `"Punkte"` or `"UserMarker"`.

```
app/src/main/java/com/gse23/lhusemann/
├── MainActivity.java          # Start screen: album, rounds, time, difficulty
├── SpielansichtActivity.java  # Game screen: photo + map
├── EndActivity.java           # Results screen
├── DownloadActivity.java      # Fetch & download albums from a server
├── Game.java                  # Central model (game logic, timer, events)
├── Punkte.java / Gesamtscore  # Scoring
├── Distanz.java               # Haversine distance
├── ReadExif.java              # EXIF GPS & description parsing
└── ...                        # Data classes & custom exceptions
```

## Getting Started

### Prerequisites
- Android Studio (Giraffe or newer recommended)
- An Android device or emulator running **Android 10 (API 29)** or higher
- An internet connection, which the map tiles need

### Build & Run
```bash
git clone https://github.com/<your-username>/<repo-name>.git
cd <repo-name>
./gradlew assembleDebug
```
Or open the project in Android Studio and run the `app` configuration.

### Run Tests & Checks
```bash
./gradlew test         # Unit tests (e.g. scoring acceptance criteria)
./gradlew checkstyle   # Code style check
```

## How to Play

1. Choose an album, and optionally a difficulty, the number of rounds and the time per round.
2. Press **Start**.
3. Look at the photo and its description, then tap the map where you think it was taken.
4. Confirm your guess to see the real location, the distance and your points.
5. Go on to the next photo. When the last round ends, you see your total score.

## Testing

The scoring logic (`Punkte`) has JUnit tests based on the acceptance criteria of user story 8. They cover the normal case, edge cases (minimum and maximum distance) and invalid input such as negative distances. See [`PunkteTest.java`](app/src/test/java/com/gse23/lhusemann/PunkteTest.java).

## Acknowledgements

- Made for the GSE course (WS 2023/24), Faculty of Technology, Bielefeld University
- Map data © [OpenStreetMap](https://www.openstreetmap.org/copyright) contributors
- Small code snippets adapted from public sources are credited in comments in the code
- *Claude (Anthropic, Claude Code)* has partially written this README. The code itself was written entirely without the help of an LLM.
