package com.example.coroutinetests.domain

import com.example.coroutinetests.rules.MainDispatcherRule
import com.example.coroutinetests.testdoubles.FakeArticleRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoadArticleUseCaseTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // EN: Verifies that the use case waits for the repository work and eventually returns the article.
    // ES: Verifica que el caso de uso espera el trabajo del repositorio y acaba devolviendo el artículo.
    @Test
    fun `returns the article from the repository`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeArticleRepository(
            delayMs = 1_000,
            result = Result.success(Article("Testing coroutines with calm")),
        )
        val useCase = LoadArticleUseCase(
            repository = repository,
            ioDispatcher = mainDispatcherRule.testDispatcher,
        )

        val pendingResult = async { useCase() }

        // EN: Execute what is ready now, but do not advance virtual time yet.
        // ES: Ejecuta lo que ya está listo, pero sin avanzar todavía el tiempo virtual.
        runCurrent()

        assertFalse(pendingResult.isCompleted)

        // EN: Advance virtual time until no pending coroutine work remains.
        // ES: Avanza el tiempo virtual hasta que no quede trabajo pendiente de coroutines.
        advanceUntilIdle()

        assertEquals("Testing coroutines with calm", pendingResult.await().title)
    }

    // EN: Verifies that repository failures are propagated instead of being swallowed.
    // ES: Verifica que los fallos del repositorio se propagan y no se silencian.
    @Test
    fun `rethrows the repository error`() = runTest(mainDispatcherRule.testDispatcher) {
        val useCase = LoadArticleUseCase(
            repository = FakeArticleRepository(
                result = Result.failure(IllegalStateException("No article available")),
            ),
            ioDispatcher = mainDispatcherRule.testDispatcher,
        )

        val error = assertFailsWith<IllegalStateException> {
            useCase()
        }

        assertEquals("No article available", error.message)
    }
}
