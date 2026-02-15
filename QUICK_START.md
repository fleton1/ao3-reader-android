# Quick Start Guide - GitHub Push & APK Build

## 🎯 What You Need to Do

You have **2 tasks** to complete:
1. ✅ Push project to GitHub
2. ⚠️ Build APK (requires Java 17)

---

## 📤 TASK 1: Push to GitHub

### Prerequisites
- GitHub account (create at https://github.com/join if needed)
- Your GitHub username

### Step-by-Step Instructions

#### 1. Create Repository on GitHub
1. Go to: **https://github.com/new**
2. Fill in:
   - **Repository name**: `ao3-reader-android` (or your choice)
   - **Description**: `Native Android fanfiction reader for AO3 with offline support`
   - **Visibility**: Public ✅ (or Private)
   - **DO NOT** check "Initialize with README" ❌
3. Click **"Create repository"**

#### 2. Run the Push Script

I've created an interactive script for you:

```bash
cd ~/AO3Reader
./push-to-github.sh
```

The script will:
1. Ask for your GitHub username
2. Ask for repository name (default: ao3-reader-android)
3. Add the remote
4. Push all code to GitHub

**Alternative: Manual Method**

If you prefer to do it manually:

```bash
cd ~/AO3Reader

# Replace YOUR_USERNAME with your actual GitHub username
git remote add origin https://github.com/YOUR_USERNAME/ao3-reader-android.git

# Push to GitHub
git push -u origin main
```

#### 3. Enter GitHub Credentials

When prompted:
- **Username**: Your GitHub username
- **Password**: Your Personal Access Token (NOT your GitHub password!)

**How to create a Personal Access Token**:
1. Go to: https://github.com/settings/tokens
2. Click "Generate new token" → "Generate new token (classic)"
3. Select scopes: `repo` (full control of private repositories)
4. Click "Generate token"
5. **COPY THE TOKEN** (you won't see it again!)
6. Use this token as your password when pushing

#### 4. Verify on GitHub

After pushing, visit:
```
https://github.com/YOUR_USERNAME/ao3-reader-android
```

You should see all your files!

### Post-Upload: Make Your Repo Look Professional

1. **Add Topics** (on GitHub repo page):
   - Click the ⚙️ gear icon next to "About"
   - Add topics: `android`, `kotlin`, `jetpack-compose`, `fanfiction`, `ao3`, `material-design`

2. **Update Description**:
   - Same place, add: "Native Android fanfiction reader for Archive of Our Own with offline support"

3. **Star Your Own Repo** ⭐
   - Shows it's an active project

---

## 📱 TASK 2: Build APK

### The Problem
Your system has **Java 25**, but this project requires **Java 17**.

Kotlin doesn't support Java 25 yet, so we have **3 options**:

---

### ✅ OPTION 1: Use Android Studio (EASIEST - RECOMMENDED!)

**Why this works**: Android Studio includes its own JDK 17 and handles everything automatically.

#### Steps:
1. **Download Android Studio**: https://developer.android.com/studio
2. **Install Android Studio**
3. **Open Project**:
   - Launch Android Studio
   - Click "Open"
   - Navigate to `~/AO3Reader`
   - Click "OK"
4. **Wait for Gradle Sync** (first time takes 5-10 minutes)
5. **Click "Run" Button** ▶️ (green play button in toolbar)
6. **Select Device** (connected phone or create emulator)
7. **APK builds and installs automatically!** 🎉

**Find the APK**:
```
~/AO3Reader/app/build/outputs/apk/debug/app-debug.apk
```

---

### ✅ OPTION 2: Install Java 17

#### On Arch Linux (your system):
```bash
# Install Java 17
sudo pacman -S jdk17-openjdk

# Switch to Java 17
sudo archlinux-java set java-17-openjdk

# Verify
java -version
# Should show: openjdk version "17.x.x"

# Build APK
cd ~/AO3Reader
./build-apk.sh
```

**APK Location**: `app/build/outputs/apk/debug/app-debug.apk`

#### On Ubuntu/Debian:
```bash
sudo apt update
sudo apt install openjdk-17-jdk
sudo update-alternatives --config java  # Select Java 17
```

#### On macOS:
```bash
brew install openjdk@17
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
```

---

### ✅ OPTION 3: Build on Another System

If you can't install Java 17:

1. **Push to GitHub** (Task 1 above)
2. **Clone on a system with Java 17**:
   ```bash
   git clone https://github.com/YOUR_USERNAME/ao3-reader-android.git
   cd ao3-reader-android
   ```
3. **Build there**:
   ```bash
   ./gradlew assembleDebug
   ```
4. **Copy APK back** to your main system

---

### ✅ OPTION 4: Use GitHub Actions (Automatic Cloud Build)

Push to GitHub, then set up GitHub Actions to build automatically:

1. Push code to GitHub
2. GitHub will show an "Actions" tab
3. APK builds in the cloud (with Java 17)
4. Download from artifacts

**Note**: Requires setting up GitHub Actions workflow (see GITHUB_SETUP.md)

---

## 📦 After Building: Install APK

### On Your Phone:

**Method 1: USB (via ADB)**
```bash
# Connect phone with USB cable
# Enable USB Debugging on phone:
#   Settings → About Phone → Tap "Build Number" 7 times
#   Settings → Developer Options → Enable "USB Debugging"

# Install
adb install app/build/outputs/apk/debug/app-debug.apk
```

**Method 2: Direct Transfer**
1. Copy `app-debug.apk` to your phone (email, USB, cloud storage)
2. Open file manager on phone
3. Tap the APK file
4. Allow "Install from Unknown Sources" if prompted
5. Tap "Install"

---

## 🎯 Summary

### Task 1: GitHub ✅
```bash
cd ~/AO3Reader
./push-to-github.sh
```
**OR** manually:
```bash
git remote add origin https://github.com/YOUR_USERNAME/ao3-reader-android.git
git push -u origin main
```

### Task 2: Build APK

**EASIEST**: Use Android Studio ⭐
- Download: https://developer.android.com/studio
- Open project → Click Run

**OR**: Install Java 17
```bash
sudo pacman -S jdk17-openjdk
sudo archlinux-java set java-17-openjdk
cd ~/AO3Reader && ./build-apk.sh
```

**OR**: Build on another system with Java 17

---

## 🆘 Troubleshooting

### GitHub Push Fails
- **"Repository not found"** → Create repo on GitHub first
- **"Authentication failed"** → Use Personal Access Token, not password
- **"Permission denied"** → Check username/token

### Build Fails
- **"Java 25 not recognized"** → Need Java 17 (see Option 2)
- **"Android SDK not found"** → Use Android Studio or install SDK
- **"Gradle sync failed"** → Check internet connection

### Can't Install APK on Phone
- **"Install blocked"** → Enable "Unknown Sources" in Settings
- **"App not installed"** → Make sure it's the debug APK
- **"Parse error"** → APK may be corrupted, rebuild

---

## 📚 Need More Help?

- **GitHub**: See `GITHUB_SETUP.md`
- **Building**: See `BUILD_INSTRUCTIONS.md`
- **Testing**: See `TESTING.md`
- **General**: See `README.md`

---

## ✨ You Got This!

1. Push to GitHub with `./push-to-github.sh` ✅
2. Build with Android Studio (easiest) ✅
3. Enjoy your app! 🎉

**Questions?** Check the documentation files - everything is covered!
