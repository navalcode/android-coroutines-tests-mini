package com.example.coroutinetests.data

interface ArticleSearchRepository {
    suspend fun search(query: String): String
}
