package com.example.coroutinetests.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coroutinetests.domain.SearchArticleUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class ArticleSearchViewModel(
    private val searchArticleUseCase: SearchArticleUseCase,
) : ViewModel() {

    private val queries = MutableStateFlow<String?>(null)

    private val _uiState = MutableStateFlow<ArticleSearchUiState>(ArticleSearchUiState.Idle)
    val uiState: StateFlow<ArticleSearchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            queries.filterNotNull().collectLatest { query ->
                _uiState.value = ArticleSearchUiState.Loading(query)
                val title = searchArticleUseCase(query)
                _uiState.value = ArticleSearchUiState.Success(query = query, title = title)
            }
        }
    }

    fun submitQuery(query: String) {
        queries.value = query
    }
}
