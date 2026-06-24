# android-coroutines-tests-mini

Proyecto Android pequeño para ver cómo testear coroutines y `StateFlow` en local, sin meter más piezas de las necesarias.

La app hace una sola cosa: pulsas un botón, se lanza una carga y la UI pasa por `Idle`, `Loading` y `Success` o `Error`. Lo útil no es la pantalla, sino cómo están montados los tests.

## Qué hay dentro

- Una `Activity` mínima para que el proyecto sea Android de verdad
- Un `ViewModel` con `StateFlow<ArticleUiState>`
- Un `UseCase`
- Un `Repository`
- Una implementación estática para la app
- Una fake para tests
- Varios tests locales sobre coroutines, `Flow` y cancelación

## Estructura

```text
app/src/main/java/com/example/coroutinetests/
├── MainActivity.kt
├── data/
│   ├── ArticleRepository.kt
│   └── StaticArticleRepository.kt
├── domain/
│   ├── Article.kt
│   └── LoadArticleUseCase.kt
└── presentation/
    ├── ArticleUiState.kt
    └── ArticleViewModel.kt

app/src/test/java/com/example/coroutinetests/
├── domain/LoadArticleUseCaseTest.kt
├── presentation/ArticleViewModelTest.kt
├── rules/MainDispatcherRule.kt
└── testdoubles/FakeArticleRepository.kt
```

## Por dónde leerlo

Si quieres entenderlo rápido, este orden suele funcionar bien:

1. `ArticleUiState`: define los estados que ve la UI.
2. `ArticleViewModel`: enseña el flujo principal.
3. `ArticleViewModelTest`: aquí está casi toda la gracia del ejemplo.
4. `FakeArticleRepository`: explica cómo se simula éxito, error y delay sin mocks.
5. `MainDispatcherRule`: deja claro cómo se controla `Dispatchers.Main` en tests.

## Qué está intentando enseñar

### El dispatcher no debería estar hardcodeado

El `UseCase` recibe un dispatcher por constructor. Eso permite que en producción uses `Dispatchers.IO` y en tests uses un `TestDispatcher` controlado.

### `runTest` cambia la forma de pensar el tiempo

Aquí no hay `Thread.sleep()`. Los tests avanzan la ejecución con `runCurrent()` y `advanceUntilIdle()`. Esa es la parte que suele desbloquear muchos tests inestables.

### `StateFlow` se comprueba como estado

La UI no necesita saber todos los detalles internos de la coroutine. Necesita saber qué estado tiene ahora mismo. Por eso el foco está en `uiState.value`.

### `collectLatest` sirve cuando lo último invalida lo anterior

El ejemplo de búsqueda usa `collectLatest` para cancelar una búsqueda en curso cuando entra una query más nueva. Es un patrón muy habitual cuando la entrada del usuario cambia rápido.

## Tests que merece la pena mirar

### `LoadArticleUseCaseTest`

Tiene dos casos:

- Devuelve el artículo cuando el repositorio responde bien
- Propaga el error cuando el repositorio falla

Sirve para ver el caso más pequeño posible antes de subir al `ViewModel`.

### `ArticleViewModelTest`

Tiene cuatro comprobaciones:

- Arranca en `Idle`
- Pasa de `Loading` a `Success`
- Pasa de `Loading` a `Error`
- Mantiene la cancelación fuera del estado de error

Es el archivo que mejor resume la intención del proyecto.

### `ObserveSavedArticlesUseCaseTest`

Muestra un caso simple de `Flow` con varias emisiones y comprobación del orden esperado.

### `ArticleSearchViewModelTest`

Muestra el caso de `collectLatest`: si entra una segunda query antes de terminar la primera, la primera se cancela y solo cuenta la última.

## Ejecutarlo

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```
