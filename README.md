# AO3 Reader - Android Fanfiction Reader

A native Android app for reading fanfiction from Archive of Our Own (AO3) with offline capabilities and a clean, modern interface.

## Features

### Core Features (MVP) ✅
- **Search & Browse** - Search works by keywords, browse by fandom, rating, and tags
- **Reading Interface** - Clean, customizable reading experience with chapter navigation
- **Bookmarks** - Save works locally with automatic reading progress tracking
- **Downloads** - Download entire works for offline reading
- **Following** - Track favorite works and check for updates

### Technical Features
- **Offline-First** - All data cached locally in Room database
- **Rate Limiting** - Respects AO3's ToS with 5-second delays between requests
- **Guest Mode** - No authentication required, all data stored locally
- **Material 3 Design** - Modern, clean UI with dynamic color support
- **Ethical Scraping** - Proper User-Agent, rate limiting, and responsible usage

## Architecture

### Tech Stack
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose + Material 3
- **Architecture:** MVVM with Repository pattern
- **Dependency Injection:** Hilt
- **Database:** Room (SQLite)
- **Networking:** OkHttp + Jsoup (HTML parsing)
- **Async:** Kotlin Coroutines + Flow

### Project Structure
```
app/src/main/java/com/ao3reader/
├── data/
│   ├── local/              # Room database, DAOs, entities
│   ├── remote/             # Web scraper, rate limiter
│   └── repository/         # Repository implementations
├── domain/
│   ├── models/             # Business models
│   └── usecases/           # Use cases (future)
├── ui/
│   ├── components/         # Reusable UI components
│   ├── navigation/         # Navigation graph
│   ├── screens/            # Screen composables + ViewModels
│   └── theme/              # Material 3 theme
└── di/                     # Hilt dependency injection modules
```

## Building the Project

### Prerequisites
- Android Studio Ladybug or newer
- JDK 17 or newer
- Android SDK 35
- Minimum Android 8.0 (API 26) device/emulator

### Setup
1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd AO3Reader
   ```

2. Open the project in Android Studio

3. Sync Gradle files

4. Run on device or emulator:
   ```bash
   ./gradlew installDebug
   ```

### Build Commands
```bash
# Debug build
./gradlew assembleDebug

# Release build (requires keystore)
./gradlew assembleRelease

# Run tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## Usage Guide

### Searching for Works
1. Launch the app and tap "Search Works"
2. Enter keywords (title, author, fandom, or tags)
3. Wait 5+ seconds for results (rate limiting)
4. Tap a work to view details

### Reading a Work
1. From work details, tap "Read"
2. Use Previous/Next buttons to navigate chapters
3. Adjust font size with +/- buttons in toolbar
4. Reading progress auto-saves to bookmarks

### Bookmarking
1. Tap the bookmark icon on work detail page
2. View all bookmarks from home screen
3. Resume reading from where you left off

### Downloading for Offline
1. Tap the download icon on work detail page
2. Wait for all chapters to download (progress shown)
3. Access downloaded works from "Downloads" section
4. Read offline without internet connection

### Following Works
1. Tap the heart icon on work detail page
2. View followed works in "Following" section
3. Tap refresh icon to check for new chapters
4. Works with updates are highlighted

## Technical Details

### Rate Limiting
The app enforces a **5-second minimum delay** between all AO3 requests to comply with their Terms of Service. This is why searches and page loads may feel slower than the website - it's intentional and respectful.

### Data Storage
All data is stored locally on your device:
- Works and chapters cached in SQLite database
- Bookmarks with reading progress
- Download status and offline content
- Following list (not synced with AO3)

### Privacy
- No user accounts or authentication
- No data sent to external servers (except AO3 itself)
- All bookmarks and history stored locally
- No tracking or analytics

## Limitations

### Current Limitations
- Guest mode only (no AO3 login)
- Local bookmarks don't sync with AO3
- No series navigation (yet)
- No comments or kudos (read-only)
- Search uses basic AO3 search (no advanced filters yet)

### Known Issues
- HTML rendering is basic (uses Android's HtmlCompat)
- Large works may take time to download
- No background sync for following updates

## Roadmap

### Phase 6: Advanced Features (Planned)
- [ ] Background download worker
- [ ] Update notifications for followed works
- [ ] Enhanced home screen with recent activity

### Phase 7: Polish & Testing (Planned)
- [ ] Unit tests for repositories and ViewModels
- [ ] UI tests for critical flows
- [ ] Offline mode testing
- [ ] Performance optimization

### Future Enhancements
- [ ] Dark mode preferences
- [ ] Custom color themes
- [ ] Export to EPUB
- [ ] Text-to-speech
- [ ] Tablet optimization
- [ ] Series tracking
- [ ] Custom collections
- [ ] Cloud backup (optional)

## Contributing

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable names
- Add comments for complex logic
- Keep functions focused and small

### Git Workflow
1. Create feature branch
2. Make changes
3. Write tests
4. Submit pull request

## License

This project is for educational purposes. AO3 content belongs to its respective authors. Always respect AO3's Terms of Service.

## Disclaimer

This is an unofficial app and is not affiliated with or endorsed by Archive of Our Own (AO3) or the Organization for Transformative Works (OTW).

## Acknowledgments

- Archive of Our Own for providing an amazing platform
- The fanfiction community
- Material Design team for UI guidelines
- Android Jetpack team for excellent libraries

---

**Built with ❤️ for the fanfiction community**
