package com.example.coroutinetests.presentation

sealed interface ArticleUiState {
    data object Idle : ArticleUiState
    data object Loading : ArticleUiState
    data class Success(val title: String) : ArticleUiState
    data class Error(val message: String) : ArticleUiState
}
