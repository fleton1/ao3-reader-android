#!/bin/bash

# AO3 Reader - APK Build Script
# Checks Java version and builds the APK

echo "🔨 AO3 Reader - APK Build Script"
echo "================================"
echo ""

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)

echo "☕ Detected Java version: $JAVA_VERSION"
echo ""

if [ "$JAVA_VERSION" != "17" ]; then
    echo "❌ ERROR: This project requires Java 17"
    echo "   Current Java version: $JAVA_VERSION"
    echo ""
    echo "📝 Solutions:"
    echo ""
    echo "Option 1: Install Java 17"
    echo "  # On Arch Linux:"
    echo "  sudo pacman -S jdk17-openjdk"
    echo "  sudo archlinux-java set java-17-openjdk"
    echo ""
    echo "  # On Ubuntu/Debian:"
    echo "  sudo apt install openjdk-17-jdk"
    echo ""
    echo "  # On macOS:"
    echo "  brew install openjdk@17"
    echo ""
    echo "Option 2: Use Android Studio"
    echo "  1. Open Android Studio"
    echo "  2. File → Open → Select $(pwd)"
    echo "  3. Click 'Run' button"
    echo "  4. APK builds automatically with Android Studio's built-in JDK"
    echo ""
    echo "Option 3: Build on another system"
    echo "  1. Push code to GitHub: ./push-to-github.sh"
    echo "  2. Clone on a system with Java 17"
    echo "  3. Build there: ./gradlew assembleDebug"
    echo ""
    echo "For more details, see: BUILD_INSTRUCTIONS.md"
    exit 1
fi

echo "✅ Java 17 detected - proceeding with build"
echo ""

# Make gradlew executable
chmod +x gradlew

# Build APK
echo "🔨 Building debug APK..."
echo ""
./gradlew assembleDebug --no-daemon

BUILD_STATUS=$?

echo ""
if [ $BUILD_STATUS -eq 0 ]; then
    echo "✅ Build successful!"
    echo ""
    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"

    if [ -f "$APK_PATH" ]; then
        APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
        echo "📦 APK Location: $APK_PATH"
        echo "📏 APK Size: $APK_SIZE"
        echo ""
        echo "📱 To install on device:"
        echo "   adb install $APK_PATH"
        echo ""
        echo "   Or copy APK to phone and install manually"
    fi
else
    echo "❌ Build failed"
    echo ""
    echo "Common issues:"
    echo "  1. Missing Android SDK - set ANDROID_HOME"
    echo "  2. Gradle sync issues - try: ./gradlew clean"
    echo "  3. Network issues - dependencies can't download"
    echo ""
    echo "For troubleshooting, see: BUILD_INSTRUCTIONS.md"
    exit 1
fi
