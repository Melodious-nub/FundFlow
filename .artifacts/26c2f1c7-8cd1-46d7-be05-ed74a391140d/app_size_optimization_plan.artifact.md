# App Size Optimization Plan

Your app's size is likely inflated by large transitive dependencies from Google Drive and the Material Icons Extended library. We can significantly reduce this (potentially by 40-50%) without losing any quality or functionality.

## Optimization Strategy

### 1. Resource & Code Shrinking
We will enable **Resource Shrinking** alongside the existing code minification (R8). While R8 removes unused code, Resource Shrinking removes unused resources (like icons from the huge Extended library).

### 2. Language Configuration
Android apps by default include translations for dozens of languages. We will restrict this to "en" (and "bn" if applicable) to strip out hundreds of unused string files from support libraries.

### 3. Packaging Exclusions
We will exclude redundant metadata and license files that are bundled by Google Drive and other Java libraries.

### 4. Icon Optimization
Instead of bundling the entire `material-icons-extended` library, we will selectively import only the icons we use.

## Proposed Changes

### [App Configuration]

#### [MODIFY] [build.gradle.kts](file:///D:/Projects/Android/FundFlow/app/build.gradle.kts)
- Add `isShrinkResources = true` to the release build type.
- Add `resConfigs("en")` to `defaultConfig`.
- Refine the `packaging` block to exclude more redundant files.
- **IMPORTANT**: Replace `material-icons-extended` with a more efficient approach (manual vectors or specific imports).

#### [NEW] [proguard-rules.pro](file:///D:/Projects/Android/FundFlow/app/proguard-rules.pro)
- Add specific rules for Google Drive and Room to ensure R8 doesn't over-shrink critical reflection-based code, while still being aggressive on other parts.

## Expected Results
- **Release APK**: Should drop from ~70MB to under 30MB (or even less depending on how aggressive R8 can be with the Google Drive libs).
- **Download Size**: Significantly lower for end users.

## Verification
- Run `./gradlew assembleRelease` and check the APK size in `app/build/outputs/apk/release/`.
- Use the **APK Analyzer** in Android Studio to verify what is taking up space.
