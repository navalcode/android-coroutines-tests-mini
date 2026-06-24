package com.example.coroutinetests.presentation

import com.example.coroutinetests.domain.Article
import com.example.coroutinetests.domain.LoadArticleUseCase
import com.example.coroutinetests.rules.MainDispatcherRule
import com.example.coroutinetests.testdoubles.FakeArticleRepository
import java.util.concurrent.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArticleViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // EN: Verifies the initial UI contract before any action is triggered.
    // ES: Verifica el contrato inicial de UI antes de lanzar ninguna acción.
    @Test
    fun `starts idle`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = ArticleViewModel(
            loadArticleUseCase = LoadArticleUseCase(
                repository = FakeArticleRepository(),
                ioDispatcher = mainDispatcherRule.testDispatcher,
            ),
        )

        assertEquals(ArticleUiState.Idle, viewModel.uiState.value)
    }

    // EN: Verifies the normal path from Loading to Success when the async work finishes well.
    // ES: Verifica el camino normal de Loading a Success cuando el trabajo asíncrono termina bien.
    @Test
    fun `goes from loading to success`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = ArticleViewModel(
            loadArticleUseCase = LoadArticleUseCase(
                repository = FakeArticleRepository(
                    delayMs = 1_000,
                    result = Result.success(Article("A tiny app can still teach a lot")),
                ),
                ioDispatcher = mainDispatcherRule.testDispatcher,
            ),
        )

        viewModel.loadArticle()

        // Flush until the first suspension point so the intermediate Loading state is observable.
        // EN: We stop here to inspect the temporary UI state before the fake repository completes.
        // ES: Nos detenemos aquí para inspeccionar el estado temporal de UI antes de que termine el repositorio fake.
        runCurrent()

        assertEquals(ArticleUiState.Loading, viewModel.uiState.value)

        advanceUntilIdle()

        assertEquals(
            ArticleUiState.Success("A tiny app can still teach a lot"),
            viewModel.uiState.value,
        )
    }

    // EN: Verifies the error path from Loading to Error when the use case fails.
    // ES: Verifica el camino de error de Loading a Error cuando falla el caso de uso.
    @Test
    fun `goes from loading to error`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = ArticleViewModel(
            loadArticleUseCase = LoadArticleUseCase(
                repository = FakeArticleRepository(
                    delayMs = 1_000,
                    result = Result.failure(IllegalStateException("Article source is empty")),
                ),
                ioDispatcher = mainDispatcherRule.testDispatcher,
            ),
        )

        viewModel.loadArticle()

        // Same idea here: stop before the fake finishes and read the temporary UI state.
        // EN: This lets the test prove that Loading appears before the final Error state.
        // ES: Esto permite demostrar que Loading aparece antes del estado final de Error.
        runCurrent()

        assertEquals(ArticleUiState.Loading, viewModel.uiState.value)

        advanceUntilIdle()

        assertEquals(
            ArticleUiState.Error("Article source is empty"),
            viewModel.uiState.value,
        )
    }

    // EN: Verifies that coroutine cancellation is not converted into a user-facing error state.
    // ES: Verifica que la cancelación de la coroutine no se convierte en un estado de error visible para la UI.
    @Test
    fun `does not turn cancellation into ui error`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = ArticleViewModel(
            loadArticleUseCase = LoadArticleUseCase(
                repository = FakeArticleRepository(
                    delayMs = 1_000,
                    result = Result.failure(CancellationException("Load cancelled")),
                ),
                ioDispatcher = mainDispatcherRule.testDispatcher,
            ),
        )

        viewModel.loadArticle()

        runCurrent()

        assertEquals(ArticleUiState.Loading, viewModel.uiState.value)

        advanceUntilIdle()

        assertEquals(ArticleUiState.Loading, viewModel.uiState.value)
    }
}
