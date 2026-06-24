package com.example.coroutinetests.domain

import com.example.coroutinetests.data.SavedArticlesRepository
import kotlinx.coroutines.flow.Flow

class ObserveSavedArticlesUseCase(
    private val repository: SavedArticlesRepository,
) {
    operator fun invoke(): Flow<List<Article>> = repository.observeSavedArticles()
}
