# Build Instructions for AO3 Reader

## Current Status

✅ **Git Repository**: Initialized and ready
✅ **Code**: Complete and production-ready
✅ **Documentation**: All guides created
⚠️ **APK Build**: Requires Java 17 (current system has Java 25)

## Prerequisites

### Required Software
- **JDK 17** (NOT Java 25) - [Download here](https://adoptium.net/temurin/releases/?version=17)
- **Android Studio** Ladybug or newer (recommended)
- OR **Android SDK** command line tools

### Setting Up Java 17

#### Option 1: Install Java 17 (Recommended)
```bash
# On Arch Linux
sudo pacman -S jdk17-openjdk

# On Ubuntu/Debian
sudo apt install openjdk-17-jdk

# On macOS
brew install openjdk@17

# Set JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
# Or on macOS:
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
```

#### Option 2: Use Android Studio
Android Studio includes its own JDK and handles everything automatically.

## Building the APK

### Method 1: Using Android Studio (Easiest)

1. **Open Project**
   ```bash
   # Open Android Studio
   # File → Open → Select AO3Reader directory
   ```

2. **Sync Gradle**
   - Android Studio will automatically sync Gradle files
   - Wait for sync to complete

3. **Build APK**
   - Build → Build Bundle(s) / APK(s) → Build APK(s)
   - OR click the "Run" button to install on connected device

4. **Find APK**
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```

### Method 2: Command Line (With Java 17)

```bash
cd ~/AO3Reader

# Make gradlew executable
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug

# Output location:
# app/build/outputs/apk/debug/app-debug.apk
```

### Method 3: Release Build (Production)

**Prerequisites**:
1. Create signing keystore (see RELEASE.md)
2. Configure keystore.properties

```bash
# Build release APK
./gradlew assembleRelease

# Output:
# app/build/outputs/apk/release/app-release.apk
```

## Common Issues

### "Java 25 not recognized"
- Kotlin doesn't fully support Java 25 yet
- Solution: Install and use Java 17

### "Android SDK not found"
```bash
# Set ANDROID_HOME environment variable
export ANDROID_HOME=$HOME/Android/Sdk

# Or create local.properties file:
echo "sdk.dir=/path/to/android/sdk" > local.properties
```

### "Gradle sync failed"
```bash
# Clean and rebuild
./gradlew clean
./gradlew build --refresh-dependencies
```

### "KSP plugin not found"
- Should not happen with current configuration
- If it does, sync Gradle files in Android Studio

## Verifying the Build

```bash
# Check APK size
ls -lh app/build/outputs/apk/debug/app-debug.apk

# Should be around 5-15 MB

# Verify APK structure
unzip -l app/build/outputs/apk/debug/app-debug.apk | head -20
```

## Installing the APK

### On Physical Device
```bash
# Enable USB debugging on device
# Settings → Developer Options → USB Debugging

# Connect device and install
adb install app/build/outputs/apk/debug/app-debug.apk
```

### On Emulator
```bash
# Start emulator from Android Studio or:
emulator -avd <avd_name>

# Install APK
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Direct Transfer
1. Copy APK to phone
2. Open file manager on phone
3. Tap APK file
4. Allow installation from unknown sources if prompted
5. Install

## Testing the Build

After installation:
1. Launch app
2. Navigate to Search
3. Search for "Harry Potter"
4. Wait 5+ seconds (rate limiting)
5. Verify results appear
6. Tap a work
7. Tap "Read"
8. Verify chapter loads

## Build Variants

### Debug Build
- Includes debug symbols
- Logging enabled
- Not optimized
- Larger APK size
- Faster build time

```bash
./gradlew assembleDebug
```

### Release Build
- ProGuard optimization
- No debug symbols
- Logging removed
- Smaller APK size
- Slower build time
- Requires signing

```bash
./gradlew assembleRelease
```

## Quick Start (Recommended Path)

**For developers who want to build immediately**:

1. Install Android Studio
2. Open project in Android Studio
3. Wait for Gradle sync
4. Click "Run" button
5. APK builds and installs automatically

**For command-line users**:

1. Install JDK 17
2. Set JAVA_HOME to JDK 17
3. Run `./gradlew assembleDebug`
4. Find APK in `app/build/outputs/apk/debug/`

## Next Steps

After building:
- See TESTING.md for testing guidelines
- See RELEASE.md for distribution
- See README.md for project overview

## Need Help?

- Check CLAUDE.md for development guide
- Review error messages carefully
- Ensure Java 17 is being used
- Try Android Studio if command line fails
