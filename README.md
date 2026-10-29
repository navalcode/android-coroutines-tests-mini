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

## Run

Requires JDK 17, Android SDK 36 and API 24+ for installation. Open this repository in Android Studio or configure the SDK using untracked `local.properties`.

```sh
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
./gradlew :app:installDebug
```

The Gradle 8.13 wrapper verifies its distribution SHA-256. The longer [Spanish guide](README.es.md) explains the examples and test decisions.

## Validation scope

Nine JVM tests, lint and a debug APK build passed locally on 29 October 2026 with JDK 17. Lint reported no errors and 13 warnings, including dependency/target updates. No emulator UI suite or physical-device validation is claimed here.

This is an educational sample, with no production article service or store release. Its release configuration currently disables minification. The debug artifact lacks R8 metadata and does not demonstrate optimized release behavior.
