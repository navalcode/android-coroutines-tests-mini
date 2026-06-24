package com.example.coroutinetests.domain

import com.example.coroutinetests.data.SavedArticlesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveSavedArticlesUseCaseTest {

    // EN: Verifies that a Flow can emit the current value first and later emit updates in order.
    // ES: Verifica que un Flow puede emitir primero el valor actual y después emitir actualizaciones en orden.
    @Test
    fun `emits current articles and later updates`() = runTest {
        val repository = FakeSavedArticlesRepository(
            initialArticles = emptyList(),
        )
        val useCase = ObserveSavedArticlesUseCase(repository)
        val emissions = mutableListOf<List<Article>>()

        val collectionJob = launch {
            useCase().take(2).toList(emissions)
        }

        // EN: Start the collector so the first emission can be observed before pushing the update.
        // ES: Arranca el colector para poder observar la primera emisión antes de enviar la actualización.
        runCurrent()

        repository.updateArticles(
            listOf(
                Article("Flow for article lists"),
                Article("Testing multiple emissions"),
            ),
        )

        advanceUntilIdle()
        collectionJob.join()

        assertEquals(
            listOf(
                emptyList(),
                listOf(
                    Article("Flow for article lists"),
                    Article("Testing multiple emissions"),
                ),
            ),
            emissions,
        )
    }
}

private class FakeSavedArticlesRepository(
    initialArticles: List<Article>,
) : SavedArticlesRepository {
    private val articles = MutableStateFlow(initialArticles)

    override fun observeSavedArticles(): Flow<List<Article>> = articles

    fun updateArticles(newArticles: List<Article>) {
        articles.value = newArticles
    }
}
