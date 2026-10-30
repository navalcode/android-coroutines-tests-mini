# Coroutines Tests Mini

[English](README.md) · [CI](https://github.com/navalcode/android-coroutines-tests-mini/actions/workflows/checks.yml)

Una pequeña app Android con Views y una suite de tests de coroutines. Carga un artículo fijo, observa Idle → Loading → Success y vuelve a cargarlo. El botón queda deshabilitado durante la petición; el ViewModel conserva el resultado al recrear la actividad.

<img src="docs/images/screen-es.png" width="300" alt="Coroutines Tests Mini ejecutado en un emulador Android">

## Qué puedes probar

1. Pulsa **Cargar artículo**. La pantalla muestra Cargando de inmediato y deshabilita la acción.
2. Espera al artículo fijo y pulsa **Volver a cargar**.
3. Recrea la actividad para comprobar que el ViewModel conserva el resultado. Reiniciar el proceso crea otro ViewModel; la app no persiste el artículo.

La fuente instalada siempre responde correctamente tras una breve espera. Los tests provocan errores y cancelación con repositorios controlados. Otro ViewModel demuestra la sustitución de consultas obsoletas; la app no tiene buscador ni backend de red. El artículo original está en inglés en ambos idiomas de la interfaz.

## Compilar y ejecutar

Usa JDK 17, Android SDK 36 y API 24 o superior para instalar. Abre el proyecto en Android Studio o configura el SDK en un `local.properties` sin seguimiento. Desde este directorio:

```sh
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
./gradlew :app:installDebug
```

El wrapper de Gradle 8.13 verifica el SHA-256 de su distribución. Las dependencias están fijadas en `gradle/libs.versions.toml`. Las reglas de idioma de los tests nativos requieren API 33+; sustituye `DEVICE_ID` por un serial concreto:

```sh
ANDROID_SERIAL=DEVICE_ID ./gradlew :app:connectedDebugAndroidTest
```

## Leer el código

1. [ArticleUiState.kt](app/src/main/java/com/example/coroutinetests/presentation/ArticleUiState.kt) y [ArticleViewModel.kt](app/src/main/java/com/example/coroutinetests/presentation/ArticleViewModel.kt): estado y ciclo de vida de la petición.
2. [ArticleViewModelTest.kt](app/src/test/java/com/example/coroutinetests/presentation/ArticleViewModelTest.kt): Idle, Loading/Success, fallo y cancelación.
3. [LoadArticleUseCaseTest.kt](app/src/test/java/com/example/coroutinetests/domain/LoadArticleUseCaseTest.kt) y [FakeArticleRepository.kt](app/src/test/java/com/example/coroutinetests/testdoubles/FakeArticleRepository.kt): resultados, fallos y esperas controladas.
4. [MainDispatcherRule.kt](app/src/test/java/com/example/coroutinetests/rules/MainDispatcherRule.kt): instala y restablece el dispatcher Main de prueba.
5. [ArticleSearchViewModelTest.kt](app/src/test/java/com/example/coroutinetests/presentation/ArticleSearchViewModelTest.kt): `collectLatest` cancela la búsqueda sustituida.

Los tests avanzan el reloj del scheduler con `runCurrent` y `advanceUntilIdle`, sin esperas de reloj real. `ObserveSavedArticlesUseCaseTest` comprueba varias emisiones Flow. La cancelación se propaga sin convertirse en un estado de error.

[Notas de testing](docs/TESTING.es.md): inyección del dispatcher, tiempo virtual y casos de prueba.

## Comprobaciones y límites

La suite contiene **9 tests JVM y 3 nativos**. [Verificación](docs/VERIFICATION.md), en inglés, recoge las ejecuciones, capturas, avisos de auditoría y la diferencia entre cobertura nativa local y CI. [DESIGN.md](DESIGN.md) explica la interfaz.

Hay recursos españoles con inglés de respaldo. No se atribuye validación física, certificación de accesibilidad, release optimizada ni publicación en tiendas. Se conservan licencias y atribuciones de fuentes/dependencias en [avisos de terceros](THIRD_PARTY.md).
