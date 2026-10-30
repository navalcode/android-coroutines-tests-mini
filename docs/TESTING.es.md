# Pruebas de coroutines y StateFlow

## Inyectar el dispatcher para controlar el test

El `UseCase` recibe un dispatcher por constructor. Eso permite que en producción uses `Dispatchers.IO` y en tests uses un `TestDispatcher` controlado.

## Controlar el tiempo con `runTest`

Aquí no hay `Thread.sleep()`. Los tests avanzan la ejecución con `runCurrent()` y `advanceUntilIdle()`. Así se controla cuándo se ejecutan las coroutines en lugar de esperar al reloj real.

## Comprobar el estado de `StateFlow`

La UI no necesita saber todos los detalles internos de la coroutine. Necesita saber qué estado tiene ahora mismo. Por eso el foco está en `uiState.value`.

## Sustituir una búsqueda con `collectLatest`

El ejemplo de búsqueda usa `collectLatest` para cancelar una búsqueda en curso cuando entra una consulta más reciente. El test comprueba que solo esta última aporta el resultado.

## Casos de prueba

### `LoadArticleUseCaseTest`

Tiene dos casos:

- Devuelve el artículo cuando el repositorio responde bien
- Propaga el error cuando el repositorio falla

Comprueba el contrato del caso de uso antes del estado de presentación.

### `ArticleViewModelTest`

Tiene cuatro comprobaciones:

- Arranca en `Idle`
- Pasa de `Loading` a `Success`
- Pasa de `Loading` a `Error`
- Mantiene la cancelación fuera del estado de error

Permite comprobar los cambios de estado y la propagación de cancelación.

### `ObserveSavedArticlesUseCaseTest`

Muestra un caso simple de `Flow` con varias emisiones y comprobación del orden esperado.

### `ArticleSearchViewModelTest`

Muestra el caso de `collectLatest`: si entra una segunda query antes de terminar la primera, la primera se cancela y solo cuenta la última.

