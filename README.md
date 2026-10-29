# Coroutines Tests Mini

[Español](README.es.md) · [![CI](https://github.com/navalcode/android-coroutines-tests-mini/actions/workflows/checks.yml/badge.svg)](https://github.com/navalcode/android-coroutines-tests-mini/actions/workflows/checks.yml)

A small Android example for testing coroutines and `StateFlow` without a large framework. A minimal Views activity requests an article while the presentation moves through `Idle`, `Loading`, `Success` or `Error`.

The main learning material is the implementation and its tests: dispatcher injection, fake repositories, virtual time, cancellation and latest-query behavior.

## Read in this order

1. `ArticleUiState` and `ArticleViewModel` under `app/src/main/java/com/example/coroutinetests/presentation/`.
2. `ArticleViewModelTest`: initial state, loading, success, error and cancellation.
3. `LoadArticleUseCaseTest` and `FakeArticleRepository`: controlled results and delays without a mocking framework.
4. `MainDispatcherRule`: install/reset a scheduler-controlled `Dispatchers.Main`.
5. `ObserveSavedArticlesUseCaseTest`: several Flow emissions and their order.
6. `ArticleSearchViewModelTest`: `collectLatest` cancels an obsolete search when a newer query arrives.

Tests use `runTest`, `runCurrent` and `advanceUntilIdle` rather than wall-clock sleeps. Cancellation remains cancellation instead of becoming an error state. The repository boundary supports a static implementation in the app and fakes in tests.

## Use the screen

<img src="docs/images/screen-en.png" alt="Coroutines Tests Mini showing its idle state and load action on an Android emulator" width="300">

A scrollable screen separates the state name, result and primary action. Loading feedback appears immediately and **the action stays disabled until that request finishes**. Success offers Load again; recreation preserves the ViewModel result. State changes have a polite accessibility live region. Spanish resources and an English fallback cover the controls; the original fixed article fixture remains in English.

The installed sample always returns that static article after a short delay. Errors, cancellation and replacing obsolete searches are exercised by JVM tests, rather than a fake network or extra UI controls. See [screen decisions](DESIGN.md) and [third-party notices](THIRD_PARTY.md).

## Run

Requires JDK 17, Android SDK 36 and API 24+ for installation. Open this repository in Android Studio or configure the SDK using untracked `local.properties`.

```sh
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
./gradlew :app:installDebug
```

The Gradle 8.13 wrapper verifies its distribution SHA-256. The longer [Spanish guide](README.es.md) explains the examples and test decisions.

## Validation scope

Locally checked on 29 October 2026: **nine JVM tests and three real MainActivity journeys on an API 37 emulator**, plus lint and debug/test APK builds with JDK 17. Native tests check immediate Loading/disabled action, confirmed success, recreation and starting another request after completion. All three also pass at 150% text on 360×800 dp. The load journey additionally passes in English/Spanish portrait, Spanish dark mode and English landscape. Images above are actual emulator captures.

Lint reports no errors and ten existing target/dependency-version warnings. The linked CI runs JVM tests, lint and a debug build; native journeys are local, not CI device coverage. To run them, use a single API 33+ emulator/device (the test locale rule uses the platform per-app locale API):

```sh
./gradlew :app:assembleDebugAndroidTest :app:connectedDebugAndroidTest
```

The app declares API 24+, but this review does not establish every supported Android version, TalkBack, physical-device behavior or store approval. The debug APK has about 12.14 MB of uncompressed DEX and no native libraries to align. R8 metadata is absent and release minification remains disabled, so no optimized release or optimization rate is established. Existing project/dependency licenses are preserved.
