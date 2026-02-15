# Setup Notes

## Required Manual Steps

### 1. Notification Icons
The app uses `R.drawable.ic_launcher_foreground` for notifications. If you don't have this resource:

**Option A: Use Android Studio to generate launcher icons**
1. Right-click on `res` folder
2. New → Image Asset
3. Configure your icon
4. This will generate `ic_launcher_foreground.xml`

**Option B: Use a simple placeholder**
Create `app/src/main/res/drawable/ic_launcher_foreground.xml`:
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#990000"
        android:pathData="M32,64C32,46.3 46.3,32 64,32C81.7,32 96,46.3 96,64C96,81.7 81.7,96 64,96C46.3,96 32,81.7 32,64Z"/>
</vector>
```

### 2. Launcher Icons
You'll need to create launcher icons:
- `mipmap-*/ic_launcher.png` (or .webp)
- `mipmap-*/ic_launcher_round.png` (or .webp)

Use Android Studio's Image Asset tool or provide your own icons.

### 3. Notification Permission (Android 13+)
For devices running Android 13 (API 33) or higher, you'll need to request notification permission at runtime.

Add this to your MainActivity:
```kotlin
private val notificationPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
) { isGranted ->
    // Handle permission result
}

// Request permission when needed
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    if (!NotificationHelper.hasNotificationPermission(this)) {
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
```

### 4. Gradle Sync
After setting up, sync Gradle:
```bash
./gradlew clean build
```

### 5. Testing WorkManager
To test background downloads and update checks:

**View scheduled work:**
```bash
adb shell dumpsys jobscheduler | grep -A 20 com.ao3reader
```

**Force run periodic work (for testing):**
```bash
adb shell am broadcast -a "androidx.work.impl.background.systemalarm.RescheduleReceiver"
```

### 6. Build Variants
- **Debug**: Includes logging, easier debugging
- **Release**: Requires signing key, optimized with ProGuard

### 7. Testing Notifications
Enable notifications in device settings:
- Settings → Apps → AO3 Reader → Notifications → Allow

Test channels:
- "Work Downloads" - Shows download progress
- "Work Updates" - Shows when followed works have new chapters

## Known Issues

1. **First build may be slow** - Gradle needs to download dependencies
2. **Notification icons** - May appear as squares without proper drawables
3. **Rate limiting** - All AO3 requests have 5-second delays
4. **Large downloads** - Works with 100+ chapters take significant time

## Development Tips

- Use **Build Variants** to switch between debug/release
- Check **Logcat** for scraping errors and rate limiting logs
- Use **Database Inspector** to view cached data
- Test **offline mode** by enabling airplane mode

## Performance

- Database queries are optimized with indices
- Network requests are rate-limited (5 seconds)
- UI updates use Kotlin Flow for reactive updates
- WorkManager handles background tasks efficiently
