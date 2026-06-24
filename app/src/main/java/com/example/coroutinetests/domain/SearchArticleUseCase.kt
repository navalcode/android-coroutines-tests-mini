package com.example.coroutinetests.domain

import com.example.coroutinetests.data.ArticleSearchRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class SearchArticleUseCase(
    private val repository: ArticleSearchRepository,
    private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(query: String): String = withContext(ioDispatcher) {
        repository.search(query)
    }
}
