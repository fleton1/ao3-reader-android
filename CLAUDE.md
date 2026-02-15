# Claude AI Developer Guide for AO3 Reader

This file provides context and instructions for Claude AI when working on this project.

## Project Overview

**AO3 Reader** is a native Android fanfiction reader app for Archive of Our Own (AO3). It provides offline reading, bookmarks, downloads, and following capabilities while respecting AO3's Terms of Service through proper rate limiting.

## Architecture

### Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVVM with Repository pattern
- **DI**: Hilt (Dagger)
- **Database**: Room (SQLite)
- **Network**: OkHttp + Jsoup (HTML parsing)
- **Background**: WorkManager
- **Async**: Kotlin Coroutines + Flow

### Project Structure
```
app/src/main/java/com/ao3reader/
├── data/
│   ├── local/              # Room database entities and DAOs
│   ├── remote/             # AO3 web scraper with rate limiting
│   └── repository/         # Repository implementations
├── domain/
│   └── models/             # Business logic models
├── ui/
│   ├── screens/            # Composable screens + ViewModels
│   ├── components/         # Reusable UI components
│   ├── navigation/         # Navigation graph
│   └── theme/              # Material 3 theme
├── workers/                # WorkManager background workers
└── di/                     # Hilt dependency injection modules
```

## Key Principles

### 1. AO3 Terms of Service Compliance
**CRITICAL**: Always maintain rate limiting!
- Minimum 5-second delay between all AO3 requests
- Implemented in `RateLimiter.kt`
- Never bypass or reduce this delay
- Proper User-Agent: "AO3Reader/1.0 (Educational Project)"

### 2. Offline-First Architecture
- Always check local cache first
- Network is fallback only
- All data cached in Room database
- User can read downloaded works without internet

### 3. Clean Architecture Layers
```
UI Layer (Compose)
    ↓
ViewModel (State Management)
    ↓
Repository (Cache + Network)
    ↓
Data Sources (Room + AO3Scraper)
```

### 4. Reactive Data Flow
- Use Kotlin Flow for reactive streams
- ViewModels expose StateFlow for UI state
- DAOs return Flow for automatic updates
- Resource wrapper for loading/success/error states

## Common Tasks

### Adding a New Feature

1. **Create Domain Model** (`domain/models/`)
   ```kotlin
   data class NewFeature(
       val id: String,
       val data: String
   )
   ```

2. **Create Database Entity** (`data/local/entities/`)
   ```kotlin
   @Entity(tableName = "new_feature")
   data class NewFeatureEntity(
       @PrimaryKey val id: String,
       val data: String
   )
   ```

3. **Create DAO** (`data/local/dao/`)
   ```kotlin
   @Dao
   interface NewFeatureDao {
       @Query("SELECT * FROM new_feature")
       fun getAll(): Flow<List<NewFeatureEntity>>

       @Insert(onConflict = OnConflictStrategy.REPLACE)
       suspend fun insert(item: NewFeatureEntity)
   }
   ```

4. **Add to Database** (`data/local/AO3Database.kt`)
   ```kotlin
   @Database(
       entities = [..., NewFeatureEntity::class],
       version = 2  // Increment version!
   )
   abstract class AO3Database : RoomDatabase {
       abstract fun newFeatureDao(): NewFeatureDao
   }
   ```

5. **Create Repository** (`data/repository/`)
   ```kotlin
   @Singleton
   class NewFeatureRepository @Inject constructor(
       private val dao: NewFeatureDao
   ) {
       fun getAll(): Flow<List<NewFeature>> {
           return dao.getAll().map { entities ->
               entities.map { it.toDomain() }
           }
       }
   }
   ```

6. **Create ViewModel** (`ui/screens/newfeature/`)
   ```kotlin
   @HiltViewModel
   class NewFeatureViewModel @Inject constructor(
       private val repository: NewFeatureRepository
   ) : ViewModel() {
       private val _uiState = MutableStateFlow(NewFeatureUiState())
       val uiState: StateFlow<NewFeatureUiState> = _uiState.asStateFlow()

       init {
           loadData()
       }
   }
   ```

7. **Create Screen** (`ui/screens/newfeature/`)
   ```kotlin
   @Composable
   fun NewFeatureScreen(
       viewModel: NewFeatureViewModel = hiltViewModel()
   ) {
       val uiState by viewModel.uiState.collectAsState()

       // UI implementation
   }
   ```

### Adding a New Scraper Method

When adding new AO3 scraping functionality:

1. **Always use RateLimiter** (`data/remote/AO3Scraper.kt`)
   ```kotlin
   suspend fun newScraperMethod(param: String): Result<Data> =
       withContext(Dispatchers.IO) {
           rateLimiter.throttle {
               try {
                   val url = "$BASE_URL/path?param=$param&view_adult=true"
                   val document = fetchDocument(url)
                   val data = parseData(document)
                   Result.success(data)
               } catch (e: Exception) {
                   Result.failure(e)
               }
           }
       }
   ```

2. **Defensive Parsing**
   - Use `?.` for nullable elements
   - Provide fallback values
   - Catch parsing exceptions
   - Test with real HTML from AO3

3. **Always add `?view_adult=true`** to URLs

### Database Migrations

When changing schema:

1. **Increment version number** in `AO3Database.kt`
2. **Add migration**:
   ```kotlin
   val MIGRATION_1_2 = object : Migration(1, 2) {
       override fun migrate(database: SupportSQLiteDatabase) {
           database.execSQL("ALTER TABLE works ADD COLUMN new_field TEXT")
       }
   }

   // In DatabaseModule:
   Room.databaseBuilder(...)
       .addMigrations(MIGRATION_1_2)
       .build()
   ```

3. **Or use destructive migration** (loses data):
   ```kotlin
   .fallbackToDestructiveMigration()
   ```

### Adding Tests

**Unit Test Template**:
```kotlin
@Test
fun `test description`() = runTest {
    // Given
    val testData = createTestData()
    `when`(mockRepository.getData()).thenReturn(testData)

    // When
    val result = viewModel.performAction()

    // Then
    assertEquals(expected, result)
    verify(mockRepository).getData()
}
```

**UI Test Template**:
```kotlin
@Test
fun testUserFlow() {
    composeTestRule.onNodeWithText("Button")
        .performClick()

    composeTestRule.onNodeWithText("Expected")
        .assertIsDisplayed()
}
```

## Important Files

### Configuration
- `build.gradle.kts` (app) - Dependencies, build config
- `gradle/libs.versions.toml` - Version catalog
- `proguard-rules.pro` - Release optimization rules

### Core Classes
- `AO3Scraper.kt` - HTML parsing and rate limiting
- `AO3Database.kt` - Room database definition
- `WorkRepository.kt` - Main repository with cache-first logic
- `WorkDetailViewModel.kt` - Example ViewModel pattern
- `MainActivity.kt` - App entry point

### Background Processing
- `DownloadWorker.kt` - Background downloads
- `UpdateCheckerWorker.kt` - Periodic update checks
- `WorkManagerHelper.kt` - WorkManager utility

## Code Style

### Naming Conventions
- **Classes**: PascalCase (`WorkRepository`)
- **Functions**: camelCase (`getWork()`)
- **Constants**: UPPER_SNAKE_CASE (`BASE_URL`)
- **Private vars**: start with underscore (`_uiState`)

### Compose Best Practices
```kotlin
@Composable
fun ScreenName(
    parameter: Type,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Hoisting state when needed
    // Side effects in LaunchedEffect
    // Remember expensive operations
}
```

### Flow Best Practices
```kotlin
// In Repository
fun getData(): Flow<Resource<Data>> = flow {
    emit(Resource.Loading())

    // Check cache first
    dao.getData().firstOrNull()?.let {
        emit(Resource.Success(it))
        return@flow
    }

    // Network fallback
    scraper.getData()
        .onSuccess { emit(Resource.Success(it)) }
        .onFailure { emit(Resource.Error(it.message)) }
}

// In ViewModel
viewModelScope.launch {
    repository.getData().collect { resource ->
        when (resource) {
            is Resource.Loading -> updateLoading(true)
            is Resource.Success -> updateData(resource.data)
            is Resource.Error -> updateError(resource.message)
        }
    }
}
```

## Dependencies

### Adding New Dependencies
1. Add to `gradle/libs.versions.toml`:
   ```toml
   [versions]
   new-lib = "1.0.0"

   [libraries]
   new-lib = { module = "com.example:lib", version.ref = "new-lib" }
   ```

2. Add to `app/build.gradle.kts`:
   ```kotlin
   implementation(libs.new.lib)
   ```

3. Update ProGuard rules if needed

### Current Major Dependencies
- Compose BOM: 2024.12.01
- Room: 2.7.0-alpha12
- Hilt: 2.54
- OkHttp: 4.12.0
- WorkManager: 2.10.0

## Debugging

### Common Issues

**Issue**: Network requests not working
- Check rate limiting (5-second delay)
- Verify User-Agent is set
- Check `?view_adult=true` parameter

**Issue**: Database errors
- Check version number incremented
- Verify migration added
- Check entity annotations

**Issue**: Compose not recomposing
- Ensure using StateFlow
- Check `collectAsState()` is called
- Verify state updates in ViewModel

**Issue**: WorkManager not running
- Check constraints (network required)
- Verify Hilt worker factory setup
- Check AndroidManifest permissions

### Logging
```kotlin
// Only in debug builds (removed by ProGuard in release)
Log.d("AO3Reader", "Debug message")
Log.i("AO3Reader", "Info message")
```

## Testing

### Running Tests
```bash
# Unit tests
./gradlew test

# UI tests (requires device/emulator)
./gradlew connectedAndroidTest

# Specific test
./gradlew test --tests WorkRepositoryTest
```

### Test Files Location
- Unit tests: `app/src/test/java/`
- UI tests: `app/src/androidTest/java/`

## Building

### Debug Build
```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Release Build
```bash
# Requires keystore setup (see RELEASE.md)
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

## Performance Considerations

### Database
- Use indices on frequently queried columns
- Limit query results with pagination
- Use `Flow` to observe changes reactively

### Network
- Always cache responses
- Rate limit enforced (cannot be changed)
- Show loading states during 5-second delays

### UI
- Use `remember` for expensive calculations
- Lazy loading with `LazyColumn`
- Avoid recomposition with stable keys

## Security

### Never Commit
- Keystore files (`.keystore`, `.jks`)
- `keystore.properties`
- API keys or secrets

### Rate Limiting
**DO NOT** modify or bypass rate limiting - it's required by AO3 ToS

### User Data
- All stored locally on device
- No external servers
- No tracking or analytics

## Future Enhancements

Ideas for future development:
- [ ] Advanced search filters
- [ ] Reading statistics
- [ ] Export to EPUB
- [ ] Text-to-speech
- [ ] Tablet optimization
- [ ] Custom themes
- [ ] Series navigation

## Resources

- [AO3 Website](https://archiveofourown.org)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Hilt DI](https://developer.android.com/training/dependency-injection/hilt-android)
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)

## Getting Help

1. Check existing documentation (README.md, TESTING.md, RELEASE.md)
2. Review similar implementations in codebase
3. Check Android documentation
4. Search GitHub issues (if project is public)

## Notes for Claude

When working on this project:
1. **Always respect rate limiting** - This is non-negotiable
2. **Maintain clean architecture** - Don't mix layers
3. **Follow existing patterns** - Consistency is key
4. **Write tests** - For new features
5. **Update documentation** - Keep README and guides current
6. **Think offline-first** - Cache everything
7. **Be defensive** - Handle errors gracefully

## Project Status

**Current Version**: 1.0.0
**Status**: Production-ready
**Last Updated**: Phase 7 completion

All 7 development phases complete:
✅ Foundation
✅ Network Layer
✅ Repository & Domain
✅ UI Foundation
✅ Core Screens
✅ Advanced Features
✅ Polish & Testing
