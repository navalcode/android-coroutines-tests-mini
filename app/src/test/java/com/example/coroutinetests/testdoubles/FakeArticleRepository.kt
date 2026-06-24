package com.example.coroutinetests.testdoubles

import com.example.coroutinetests.data.ArticleRepository
import com.example.coroutinetests.domain.Article
import kotlinx.coroutines.delay

class FakeArticleRepository(
    private val delayMs: Long = 0L,
    private val result: Result<Article> = Result.success(Article("Default test article")),
) : ArticleRepository {

    override suspend fun fetchArticle(): Article {
        delay(delayMs)
        return result.getOrThrow()
    }
}
