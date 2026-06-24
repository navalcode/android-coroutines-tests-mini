package com.example.coroutinetests.presentation

import com.example.coroutinetests.data.ArticleSearchRepository
import com.example.coroutinetests.domain.SearchArticleUseCase
import com.example.coroutinetests.rules.MainDispatcherRule
import java.util.concurrent.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArticleSearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // EN: Verifies the initial state for the search example before any query is submitted.
    // ES: Verifica el estado inicial del ejemplo de búsqueda antes de enviar ninguna query.
    @Test
    fun `starts idle`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = ArticleSearchViewModel(
            searchArticleUseCase = SearchArticleUseCase(
                repository = FakeArticleSearchRepository(
                    delaysByQuery = emptyMap(),
                    resultsByQuery = emptyMap(),
                ),
                ioDispatcher = mainDispatcherRule.testDispatcher,
            ),
        )

        assertEquals(ArticleSearchUiState.Idle, viewModel.uiState.value)
    }

    // EN: Verifies that collectLatest cancels the old search and keeps only the latest query result.
    // ES: Verifica que collectLatest cancela la búsqueda anterior y conserva solo el resultado de la última query.
    @Test
    fun `latest query wins when previous search is still running`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeArticleSearchRepository(
            delaysByQuery = mapOf(
                "first" to 2_000L,
                "second" to 500L,
            ),
            resultsByQuery = mapOf(
                "first" to "First result",
                "second" to "Second result",
            ),
        )
        val viewModel = ArticleSearchViewModel(
            searchArticleUseCase = SearchArticleUseCase(
                repository = repository,
                ioDispatcher = mainDispatcherRule.testDispatcher,
            ),
        )

        viewModel.submitQuery("first")

        // EN: Let the first query start so the ViewModel exposes its Loading state.
        // ES: Deja arrancar la primera query para que el ViewModel exponga su estado Loading.
        runCurrent()
        assertEquals(ArticleSearchUiState.Loading("first"), viewModel.uiState.value)

        viewModel.submitQuery("second")

        // EN: Starting the second query should cancel the previous one because collectLatest is in charge.
        // ES: Al arrancar la segunda query, la anterior debe cancelarse porque collectLatest es quien manda.
        runCurrent()
        assertEquals(ArticleSearchUiState.Loading("second"), viewModel.uiState.value)

        advanceUntilIdle()

        assertEquals(
            ArticleSearchUiState.Success(query = "second", title = "Second result"),
            viewModel.uiState.value,
        )
        assertEquals(listOf("first"), repository.cancelledQueries)
    }
}

private class FakeArticleSearchRepository(
    private val delaysByQuery: Map<String, Long>,
    private val resultsByQuery: Map<String, String>,
) : ArticleSearchRepository {
    val cancelledQueries = mutableListOf<String>()

    override suspend fun search(query: String): String {
        return try {
            delay(delaysByQuery.getValue(query))
            resultsByQuery.getValue(query)
        } catch (_: CancellationException) {
            cancelledQueries += query
            throw CancellationException("Search cancelled for $query")
        }
    }
}
