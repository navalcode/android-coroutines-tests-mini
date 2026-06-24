package com.example.coroutinetests.domain

import com.example.coroutinetests.data.ArticleRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class LoadArticleUseCase(
    private val repository: ArticleRepository,
    private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(): Article = withContext(ioDispatcher) {
        repository.fetchArticle()
    }
}
