package com.example.coroutinetests.data

import com.example.coroutinetests.domain.Article
import kotlinx.coroutines.delay

class StaticArticleRepository : ArticleRepository {
    override suspend fun fetchArticle(): Article {
        delay(1_200)
        return Article("Coroutines and Flow can be tested without pain")
    }
}
