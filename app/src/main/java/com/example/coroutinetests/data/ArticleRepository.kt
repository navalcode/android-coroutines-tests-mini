package com.example.coroutinetests.data

import com.example.coroutinetests.domain.Article

interface ArticleRepository {
    suspend fun fetchArticle(): Article
}
