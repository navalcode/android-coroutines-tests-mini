package com.example.coroutinetests.presentation

sealed interface ArticleSearchUiState {
    data object Idle : ArticleSearchUiState
    data class Loading(val query: String) : ArticleSearchUiState
    data class Success(val query: String, val title: String) : ArticleSearchUiState
}
