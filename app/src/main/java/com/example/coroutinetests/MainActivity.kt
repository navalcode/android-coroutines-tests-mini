package com.example.coroutinetests

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.coroutinetests.data.StaticArticleRepository
import com.example.coroutinetests.domain.LoadArticleUseCase
import com.example.coroutinetests.presentation.ArticleUiState
import com.example.coroutinetests.presentation.ArticleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: ArticleViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ArticleViewModel(
                    loadArticleUseCase = LoadArticleUseCase(
                        repository = StaticArticleRepository(),
                        ioDispatcher = Dispatchers.IO,
                    ),
                ) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val stateLabel = findViewById<TextView>(R.id.stateLabel)
        val loadButton = findViewById<Button>(R.id.loadButton)

        loadButton.setOnClickListener {
            viewModel.loadArticle()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    stateLabel.text = when (state) {
                        ArticleUiState.Idle -> getString(R.string.idle_message)
                        ArticleUiState.Loading -> getString(R.string.loading_message)
                        is ArticleUiState.Success -> getString(R.string.success_message, state.title)
                        is ArticleUiState.Error -> getString(R.string.error_message, state.message)
                    }
                }
            }
        }
    }
}
