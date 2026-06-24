package com.example.coroutinetests.data

import com.example.coroutinetests.domain.Article
import kotlinx.coroutines.flow.Flow

interface SavedArticlesRepository {
    fun observeSavedArticles(): Flow<List<Article>>
}
