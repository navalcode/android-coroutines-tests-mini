package com.example.coroutinetests.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coroutinetests.domain.LoadArticleUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ArticleViewModel(
    private val loadArticleUseCase: LoadArticleUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ArticleUiState>(ArticleUiState.Idle)
    val uiState: StateFlow<ArticleUiState> = _uiState.asStateFlow()

    fun loadArticle() {
        viewModelScope.launch {
            _uiState.value = ArticleUiState.Loading

            _uiState.value = try {
                ArticleUiState.Success(loadArticleUseCase().title)
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (exception: Exception) {
                ArticleUiState.Error(exception.message ?: "Unknown error")
            }
        }
    }
}
