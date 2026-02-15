# Testing Guide

## Overview

The AO3 Reader app includes comprehensive testing at multiple levels:
- **Unit Tests** - Test business logic in isolation
- **Instrumented Tests** - Test Android components and UI
- **Manual Testing** - Critical user flows

## Running Tests

### All Tests
```bash
# Run all tests (unit + instrumented)
./gradlew test connectedAndroidTest

# Or separately:
./gradlew test                    # Unit tests only
./gradlew connectedAndroidTest    # Instrumented tests only
```

### Specific Test Classes
```bash
# Run specific test class
./gradlew test --tests WorkRepositoryTest

# Run specific test method
./gradlew test --tests WorkRepositoryTest.getWork_returns_cached_work_when_available
```

### With Coverage
```bash
# Generate coverage report
./gradlew jacocoTestReport

# View report at:
# app/build/reports/jacoco/jacocoTestReport/html/index.html
```

## Unit Tests

Located in `app/src/test/java/`

### Repository Tests
**WorkRepositoryTest** - Tests cache-first strategy
- ✅ Returns cached work when available
- ✅ Fetches from network when cache is empty
- ✅ Returns error when network fails
- ✅ Force refresh bypasses cache

**SearchRepositoryTest** - Tests search functionality
- ✅ Returns empty list for blank query
- ✅ Returns results from scraper
- ✅ Handles network errors

### ViewModel Tests
**WorkDetailViewModelTest** - Tests work detail screen logic
- ✅ Loads work data into UI state
- ✅ Handles error states
- ✅ Toggles bookmark
- ✅ Refreshes work data

### Writing New Unit Tests

Example template:
```kotlin
@Test
fun `test description in backticks`() = runTest {
    // Given - Set up mocks and test data
    val testData = createTestData()
    `when`(mockRepository.getData()).thenReturn(testData)

    // When - Execute the action
    val result = systemUnderTest.performAction()

    // Then - Verify the outcome
    assertEquals(expected, result)
    verify(mockRepository).getData()
}
```

## Instrumented Tests

Located in `app/src/androidTest/java/`

### UI Tests
**SearchFlowTest** - Tests search user flow
- ✅ Navigate from home to search
- ✅ Empty state displays correctly
- ✅ Back navigation works

### Running on Device/Emulator
```bash
# Connect device or start emulator first
adb devices

# Run tests
./gradlew connectedAndroidTest

# View results at:
# app/build/reports/androidTests/connected/index.html
```

### Writing UI Tests

Example:
```kotlin
@Test
fun testUserFlow() {
    // Find and interact with UI elements
    composeTestRule.onNodeWithText("Button Text")
        .performClick()

    // Verify UI state
    composeTestRule.onNodeWithText("Expected Text")
        .assertIsDisplayed()
}
```

## Manual Testing Checklist

### Critical Flows

#### 1. Search Flow
- [ ] Open app
- [ ] Navigate to Search
- [ ] Enter query: "Harry Potter"
- [ ] Wait 5+ seconds for results
- [ ] Verify results appear
- [ ] Tap a work card
- [ ] Verify work details load

#### 2. Reading Flow
- [ ] From work details, tap "Read"
- [ ] Verify chapter 1 loads
- [ ] Tap "Next Chapter"
- [ ] Verify chapter 2 loads
- [ ] Adjust font size with +/-
- [ ] Navigate back
- [ ] Verify progress saved

#### 3. Bookmark Flow
- [ ] On work detail, tap bookmark icon
- [ ] Navigate to Bookmarks
- [ ] Verify work appears
- [ ] Tap to continue reading
- [ ] Verify bookmark shows progress

#### 4. Download Flow
- [ ] On work detail, tap download icon
- [ ] Verify notification appears
- [ ] Wait for completion
- [ ] Enable airplane mode
- [ ] Tap work to read
- [ ] Verify works offline

#### 5. Following Flow
- [ ] On work detail, tap heart icon
- [ ] Navigate to Following
- [ ] Tap refresh icon
- [ ] Wait for update check
- [ ] Verify update indicators

### Edge Cases

#### Offline Mode
- [ ] Enable airplane mode
- [ ] Search shows error
- [ ] Cached works still readable
- [ ] Bookmarks still accessible
- [ ] Downloads work offline

#### Empty States
- [ ] Fresh install → No bookmarks
- [ ] Fresh install → No downloads
- [ ] Fresh install → No following
- [ ] Empty search → Shows prompt

#### Error Handling
- [ ] Invalid work ID → Error message
- [ ] Network timeout → Retry button
- [ ] Rate limit hit → Appropriate delay

## Performance Testing

### Database Performance
```kotlin
// Measure query time
val startTime = System.currentTimeMillis()
val results = workDao.searchWorks("query", limit = 50)
val duration = System.currentTimeMillis() - startTime
assertTrue(duration < 100) // Should be under 100ms
```

### Network Performance
- Rate limiting enforced (5 seconds minimum)
- Timeout configured (30 seconds)
- Retry logic with backoff

### Memory Leaks
```bash
# Use Android Studio Profiler
# 1. Run app
# 2. Open Profiler
# 3. Navigate through screens
# 4. Force GC
# 5. Check for retained objects
```

## Test Coverage Goals

- **Repository Layer**: 80%+ coverage
- **ViewModel Layer**: 70%+ coverage
- **Domain Models**: 90%+ coverage
- **UI Components**: 50%+ coverage (manual testing supplements)

## Continuous Integration

### GitHub Actions (Example)
```yaml
name: Android CI

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Run tests
        run: ./gradlew test
      - name: Upload coverage
        uses: codecov/codecov-action@v3
```

## Mocking Guidelines

### When to Mock
- External dependencies (network, database)
- Complex objects with side effects
- Time-consuming operations

### When NOT to Mock
- Simple data classes
- Pure functions
- Domain models

### Mocking Tools
- **Mockito** - For mocking interfaces and classes
- **MockK** - Alternative Kotlin-friendly mocking (optional)
- **Turbine** - For testing Kotlin Flow (optional)

## Test Data

### Test Fixtures
Create reusable test data:
```kotlin
object TestFixtures {
    fun createTestWork(id: String = "12345") = Work(...)
    fun createTestChapter(workId: String) = Chapter(...)
}
```

### Database Testing
```kotlin
@Before
fun createDb() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    database = Room.inMemoryDatabaseBuilder(
        context,
        AO3Database::class.java
    ).build()
}

@After
fun closeDb() {
    database.close()
}
```

## Known Testing Limitations

1. **Network Tests** - Require mocking (no real AO3 calls)
2. **Rate Limiting** - Can't easily test 5-second delays
3. **WorkManager** - Requires WorkManager test library
4. **Notifications** - Limited automated testing

## Troubleshooting

### Tests Won't Run
```bash
# Clean and rebuild
./gradlew clean build

# Invalidate caches (Android Studio)
File → Invalidate Caches / Restart
```

### Flaky Tests
- Use `runTest` for coroutine tests
- Avoid hardcoded delays, use `advanceUntilIdle()`
- Mock time-dependent operations

### Out of Memory
```bash
# Increase test heap size in gradle.properties
org.gradle.jvmargs=-Xmx4096m
```

## Best Practices

1. **Test Names** - Use backticks for readable test names
2. **Given-When-Then** - Structure tests clearly
3. **One Assertion** - Test one thing per test
4. **Fast Tests** - Keep unit tests under 100ms
5. **Isolated Tests** - No test dependencies
6. **Descriptive Failures** - Use custom assertions

## Resources

- [Android Testing Guide](https://developer.android.com/training/testing)
- [Compose Testing](https://developer.android.com/jetpack/compose/testing)
- [Mockito Documentation](https://site.mockito.org/)
- [Kotlin Coroutines Testing](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/)
