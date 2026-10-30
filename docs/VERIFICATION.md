# Verification record

30 October 2026. Editorial revision of bilingual resources, README, design notes and teaching text. No domain, ViewModel, effect ownership, dependencies or workflow code changed. Core scenario messages and probe identifiers remain stable. Resource-backed instructions and observer labels are revised.

## Current local results

- JDK 17.0.14, SDK 36; all Gradle tasks rerun on the final application sources.
- **9 JVM tests**, zero failures/errors/skips.
- Lint: **0 errors, 10 warnings**. Existing target/dependency warnings remain; no warning suppression was added.
- Debug and Android-test APK builds passed. This revision does not build or validate an optimized release.

Dedicated API 37 emulator, 360 × 800 dp:

| Run | Result |
|---|---|
| English, 100% text | 3/3 native tests passed |
| 150% text, qaLanguage=es | 3/3 native tests passed |
| Spanish MainActivity capture, 100% text | 1/1 selected journey passed |

The locale rule applies to every test in this project’s native suite. The complete enlarged-text run uses Spanish.


The app declares a lower minimum SDK, while native locale rules use API 33+ and the local device is API 37. A minimum-SDK declaration does not prove testing on that version. No physical-device, TalkBack or formal accessibility certification is claimed.

## Commands

From the repository root:

```sh
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleDebugAndroidTest
ANDROID_SERIAL=DEVICE_ID ./gradlew :app:connectedDebugAndroidTest
```

Replace `DEVICE_ID` with the serial of a dedicated test emulator. For opt-in language/captures, install the app/test APK and use the Android runner:

```sh
ANDROID_SERIAL=DEVICE_ID ./gradlew :app:installDebug :app:installDebugAndroidTest
adb -s DEVICE_ID shell am instrument -w -r -e qaLanguage es -e captureScreenshots true com.example.coroutinetests.test/androidx.test.runner.AndroidJUnitRunner
```

The capture argument is optional. It writes test PNGs inside this demo’s private files directory, retrievable with `adb exec-out run-as`. Locale rules restore the previous per-app locale after a test. Use a disposable emulator for viewport/text-scale changes and restore its settings afterward. Screenshots are executed frames, without pixel editing.

The three MainActivity journeys check immediate Loading/disabled action, confirmed success, recreation and loading again. The installed fixture always succeeds. UI error text now identifies the retry action, but failures/cancellation are verified with controlled JVM repositories, not a forced runtime storage/network failure. Article state is retained during activity recreation, not persisted after process death.

## Artifact audit

Debug APK SHA-256: `920b7cf30f36fe2ef4fed3be5860ea6267b9ff21c5201b87aa5ef45d2eb9cedf`.

The advisory audit measured **12.14 MB uncompressed DEX**. No native libraries are present, so native alignment checks are empty. R8 percentage metadata is absent. An overall tool pass does not establish an optimization rate, official Play policy, signing or store approval. The audit did not remove dependencies or alter any artifact.

## Hosted CI and earlier evidence

The linked workflow runs JVM tests, lint and the debug build. Native journeys here are local; a green badge does not imply hosted device coverage. Check the workflow against the published commit. Earlier 29 October runs, including dark/landscape checks and a repaired scroll-ownership regression, remain historical evidence and are not silently attributed to this revision.
