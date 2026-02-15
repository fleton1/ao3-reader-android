# Release Build Guide

Complete guide for building and releasing the AO3 Reader Android app.

## Prerequisites

### 1. Create Keystore
First time only - create a signing keystore:

```bash
keytool -genkey -v -keystore ao3reader-release.keystore \
  -alias ao3reader \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000

# Follow prompts to set passwords and info
```

**Important**: Store keystore and passwords securely! You cannot update your app without the original keystore.

### 2. Configure Signing
Create `keystore.properties` in project root (NOT in git):

```properties
storePassword=YOUR_STORE_PASSWORD
keyPassword=YOUR_KEY_PASSWORD
keyAlias=ao3reader
storeFile=../ao3reader-release.keystore
```

Update `.gitignore`:
```
keystore.properties
*.keystore
*.jks
```

### 3. Update Build Configuration
In `app/build.gradle.kts`, add signing config:

```kotlin
android {
    signingConfigs {
        create("release") {
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = Properties()
                keystoreProperties.load(FileInputStream(keystorePropertiesFile))

                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

## Version Management

### Update Version
In `app/build.gradle.kts`:

```kotlin
defaultConfig {
    versionCode = 2  // Increment for each release
    versionName = "1.1.0"  // Semantic versioning
}
```

### Version Naming
Follow [Semantic Versioning](https://semver.org/):
- **MAJOR** - Breaking changes (e.g., 2.0.0)
- **MINOR** - New features (e.g., 1.1.0)
- **PATCH** - Bug fixes (e.g., 1.0.1)

## Building Release APK

### Clean Build
```bash
# Clean previous builds
./gradlew clean

# Build release APK
./gradlew assembleRelease

# Output: app/build/outputs/apk/release/app-release.apk
```

### Verify Build
```bash
# Check APK info
aapt dump badging app/build/outputs/apk/release/app-release.apk | grep version

# Check size
ls -lh app/build/outputs/apk/release/app-release.apk

# Verify signing
apksigner verify --verbose app/build/outputs/apk/release/app-release.apk
```

### Test Release Build
```bash
# Install on device
adb install app/build/outputs/apk/release/app-release.apk

# Or use:
./gradlew installRelease
```

## Building App Bundle (AAB)

For Google Play Store, use Android App Bundle:

```bash
# Build bundle
./gradlew bundleRelease

# Output: app/build/outputs/bundle/release/app-release.aab
```

### Test Bundle Locally
```bash
# Install bundletool
# Download from: https://github.com/google/bundletool/releases

# Generate APKs from bundle
bundletool build-apks --bundle=app/build/outputs/bundle/release/app-release.aab \
  --output=app-release.apks \
  --ks=ao3reader-release.keystore \
  --ks-key-alias=ao3reader

# Install on connected device
bundletool install-apks --apks=app-release.apks
```

## Pre-Release Checklist

### Code Quality
- [ ] All tests passing (`./gradlew test`)
- [ ] No lint errors (`./gradlew lint`)
- [ ] ProGuard rules tested
- [ ] No hardcoded secrets/API keys
- [ ] Code reviewed

### Functionality
- [ ] Search works correctly
- [ ] Reading flow smooth
- [ ] Bookmarks save/restore
- [ ] Downloads work offline
- [ ] Following tracks updates
- [ ] Notifications appear
- [ ] Rate limiting enforced

### Performance
- [ ] App starts quickly (<3 seconds)
- [ ] Smooth scrolling (60 FPS)
- [ ] No memory leaks
- [ ] Database queries fast (<100ms)
- [ ] Battery usage acceptable

### Compatibility
- [ ] Test on API 26 (Android 8.0)
- [ ] Test on API 35 (Android 15)
- [ ] Test on phone and tablet
- [ ] Test on different screen sizes
- [ ] Test with dark mode

### Legal & Privacy
- [ ] Updated privacy policy (if applicable)
- [ ] Rate limiting respects AO3 ToS
- [ ] Attribution in app (About screen)
- [ ] Open source licenses included

## Release Notes Template

Create `CHANGELOG.md`:

```markdown
# Changelog

## [1.1.0] - 2024-01-15

### Added
- Background downloads with notifications
- Automatic update checking every 6 hours
- Following screen with update indicators

### Changed
- Improved search performance
- Better offline support

### Fixed
- Fixed crash when opening work with missing chapters
- Fixed bookmark progress not saving

## [1.0.0] - 2024-01-01

### Added
- Initial release
- Search and browse AO3 works
- Clean reading interface
- Bookmarks with progress tracking
- Offline downloads
- Following works and authors
```

## Distribution

### Direct Distribution (APK)
1. Build release APK
2. Upload to website/GitHub releases
3. Share download link
4. Users enable "Unknown Sources" to install

### Google Play Store
1. Create Google Play Console account ($25 one-time)
2. Create app listing
3. Upload AAB file
4. Complete store listing (screenshots, description)
5. Submit for review
6. Wait for approval (1-7 days)

### F-Droid (Open Source)
1. Ensure build is reproducible
2. Submit to F-Droid repository
3. Follow F-Droid packaging guidelines
4. Wait for inclusion

## Post-Release

### Monitor
- Crash reports (if crash reporting added)
- User reviews and ratings
- GitHub issues
- Performance metrics

### Update Strategy
- **Hotfixes** - Critical bugs (1.0.1)
- **Minor Updates** - New features monthly (1.1.0)
- **Major Updates** - Breaking changes yearly (2.0.0)

## Rollback Plan

If critical bug found:

1. **Immediate**
   ```bash
   # Remove APK from distribution
   # Post warning to users
   ```

2. **Fix and Release**
   ```bash
   # Fix bug in new branch
   # Increment version (e.g., 1.0.1)
   # Fast-track release
   ```

3. **Communicate**
   - Notify users via app (if possible)
   - Post on GitHub/website
   - Email mailing list

## Security

### Code Signing
- Never share keystore or passwords
- Store in secure location (encrypted backup)
- Use different keystore for debug/release

### ProGuard
- Obfuscates code
- Removes unused code
- Makes reverse engineering harder

### Security Checklist
- [ ] No API keys in code
- [ ] No hardcoded passwords
- [ ] User data encrypted at rest
- [ ] HTTPS only (AO3 already uses HTTPS)
- [ ] Input validation on all user input

## Troubleshooting

### Build Fails with ProGuard
```bash
# Check ProGuard mapping
cat app/build/outputs/mapping/release/mapping.txt

# Add keep rules for missing classes
-keep class com.problematic.Class { *; }
```

### APK Too Large
```bash
# Enable resource shrinking
android {
    buildTypes {
        release {
            isShrinkResources = true
        }
    }
}

# Check APK contents
unzip -l app-release.apk | sort -k4 -rn | head -20
```

### Signing Issues
```bash
# Verify keystore
keytool -list -v -keystore ao3reader-release.keystore

# Check APK signature
apksigner verify --verbose --print-certs app-release.apk
```

## Best Practices

1. **Always Test Release Builds** - They behave differently than debug
2. **Increment Version Code** - Required for updates
3. **Keep Keystore Safe** - Cannot be recovered if lost
4. **Test on Real Devices** - Emulators don't catch everything
5. **Monitor Crash Reports** - Fix critical bugs immediately
6. **Semantic Versioning** - Makes updates clear to users

## Resources

- [Android App Bundle](https://developer.android.com/guide/app-bundle)
- [ProGuard Manual](https://www.guardsquare.com/manual/home)
- [Google Play Console](https://play.google.com/console)
- [F-Droid Inclusion Guide](https://f-droid.org/docs/Inclusion_How-To/)
