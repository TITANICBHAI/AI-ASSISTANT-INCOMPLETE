# How to Build APK from This Project

## Quick Start for Android Studio

### Step 1: Open Project
1. Launch Android Studio
2. Select **"Open"**
3. Navigate to this project folder
4. Click **"OK"**

### Step 2: Wait for Gradle Sync
- Android Studio will automatically sync Gradle dependencies
- This may take 5-15 minutes on first open
- Watch the bottom status bar for progress
- **DO NOT** interrupt the sync process

### Step 3: Fix Any Sync Errors (if needed)

#### If you see "SDK not found":
1. Go to **File → Project Structure → SDK Location**
2. Set Android SDK location (usually `C:\Users\YourName\AppData\Local\Android\Sdk` on Windows)
3. Click **"Apply"**

#### If you see dependency errors:
1. Go to **File → Invalidate Caches / Restart**
2. Select **"Invalidate and Restart"**

### Step 4: Build Debug APK
1. Go to **Build → Build Bundle(s) / APK(s) → Build APK(s)**
2. Wait for build to complete (watch bottom status bar)
3. When done, click **"locate"** link in the notification popup

**APK Location:**
```
app/build/outputs/apk/debug/app-debug.apk
```

### Step 5: Install on Device
**Option A - USB Device:**
1. Enable **Developer Options** on your Android device
2. Enable **USB Debugging**
3. Connect device via USB
4. Click **Run** button (green triangle) in Android Studio
5. Select your device from the list

**Option B - Manual Install:**
1. Copy `app-debug.apk` to your phone
2. Open the file on your phone
3. Allow **"Install from Unknown Sources"** if prompted
4. Click **"Install"**

---

## Build Release APK (For Distribution)

### Prerequisites
You need a keystore file to sign release APKs.

### Create Keystore (First Time Only)
1. Go to **Build → Generate Signed Bundle / APK...**
2. Select **APK** → Click **Next**
3. Click **"Create new..."** under Key store path
4. Fill in:
   - **Key store path:** Choose location (e.g., `C:\keystore\ai-assistant.jks`)
   - **Password:** Create a strong password
   - **Alias:** `ai-assistant-key`
   - **Alias Password:** Same or different password
   - **Validity:** 25 years (recommended)
   - **Certificate:** Fill your details
5. Click **"OK"**
6. **IMPORTANT:** Save your passwords somewhere safe!

### Build Release APK
1. Go to **Build → Generate Signed Bundle / APK...**
2. Select **APK** → Click **Next**
3. Select your keystore file
4. Enter passwords
5. Select **"release"** build variant
6. Check both **V1** and **V2** signature versions
7. Click **"Finish"**

**Release APK Location:**
```
app/build/outputs/apk/release/app-release.apk
```

---

## Command Line Build (Alternative)

### For Windows:
```cmd
gradlew.bat assembleDebug
```

### For Mac/Linux:
```bash
./gradlew assembleDebug
```

**Output:** `app/build/outputs/apk/debug/app-debug.apk`

---

## Expected Build Issues & Fixes

### Issue 1: "Room schema export directory is not provided"
**Fix:** Already configured in `build.gradle` with `exportSchema = false`

### Issue 2: Missing Android SDK
**Fix:**
1. Open **Tools → SDK Manager**
2. Install **Android 11.0 (R)** / **API Level 30**
3. Install **Build Tools 30.0.3**

### Issue 3: Gradle version too old
**Fix:**
1. Update `build.gradle` (root):
```gradle
classpath "com.android.tools.build:gradle:7.0.0"
```
2. Update `gradle-wrapper.properties`:
```
distributionUrl=https\://services.gradle.org/distributions/gradle-7.0-all.zip
```

### Issue 4: TensorFlow Lite models missing
**Current Status:** Model files exist but are empty (0 bytes)
**Fix for Testing:** The app will handle missing models gracefully
**Fix for Production:** Add actual trained `.tflite` model files to:
- `app/src/main/assets/models/`
- `app/src/main/assets/ml_models/`

### Issue 5: OpenCV native library errors
**Fix:** Already bundled in `app/libs/OpenCV-android-sdk/`
**If issues persist:** Check that `build.gradle` includes:
```gradle
implementation 'org.opencv:opencv-android:4.5.3'
```

---

## Required Permissions Setup

After installing, the app will request permissions. You MUST grant:

### Critical Permissions:
- ✅ **Microphone** - For voice features
- ✅ **Phone** - For call handling
- ✅ **Contacts** - For caller profiles

### Optional Permissions:
- **Storage** - For PDF learning features
- **Accessibility** - For gaming AI features

---

## Testing the APK

### Test Checklist:
1. **App Launches** - MainActivity opens successfully
2. **No Crashes** - App runs without force closes
3. **Permissions** - Permission requests appear properly
4. **Basic Navigation** - Can navigate between screens

### Demo Features to Test:
- **Voice Demo** - Test voice synthesis
- **Call Handling** - Test call screening (requires calls)
- **Settings** - Open settings screen
- **Gaming Features** - Test game detection

---

## Troubleshooting

### Build fails with "Execution failed for task ':app:mergeDebugResources'"
**Fix:**
1. Clean project: **Build → Clean Project**
2. Rebuild: **Build → Rebuild Project**

### APK installs but crashes immediately
**Check:**
1. Logcat output in Android Studio
2. Missing required permissions
3. Incompatible Android version (needs API 24+)

### "App not installed" error on device
**Fix:**
1. Uninstall any previous version
2. Enable "Unknown Sources" in device settings
3. Check if storage is full

### Gradle sync stuck
**Fix:**
1. Cancel sync
2. Close Android Studio
3. Delete `.gradle` folder in project root
4. Delete `.idea` folder
5. Reopen project

---

## Project Stats
- **Total Java Files:** 787
- **Target Android Version:** 11 (API 30)
- **Minimum Android Version:** 7.0 (API 24)
- **Features:** AI/ML, Voice Recognition, Gaming AI, Call Automation

---

## After Building APK

### File Size
Expected APK size: **80-150 MB** (includes OpenCV, TensorFlow Lite, and ML models)

### Distribution
- **Debug APK:** For testing only, not for public release
- **Release APK:** Signed, optimized, ready for distribution
- **Google Play:** Use AAB (Android App Bundle) instead of APK

### Next Steps
1. Test on multiple devices
2. Test all permission flows
3. Verify all features work
4. Check for crashes in production

---

## Support

**Common Questions:**

**Q: APK is very large (150MB+)?**
A: This is normal due to:
- OpenCV library (40MB)
- TensorFlow Lite (20MB)
- ML models and assets (30MB+)
- Multiple architecture support

**Q: Can I reduce APK size?**
A: Yes, use **App Bundle** (AAB) format for Google Play, which delivers only required assets per device.

**Q: Do I need special setup for gaming features?**
A: Yes, enable Accessibility Service in Android Settings → Accessibility → AI Assistant

**Q: Voice features not working?**
A: Grant Microphone permission and test text-to-speech first

---

**Last Updated:** November 7, 2025
**Build System:** Gradle 4.2.2
**Android Studio Version:** Arctic Fox or later recommended
