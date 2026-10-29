package com.example.coroutinetests

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

        val loadButton = findViewById<Button>(R.id.loadButton)

        ViewCompat.setAccessibilityHeading(findViewById(R.id.titleLabel), true)
        ViewCompat.setScreenReaderFocusable(findViewById(R.id.stateRegion), true)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.contentRoot)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        loadButton.setOnClickListener {
            if (loadButton.isEnabled) {
                renderState(ArticleUiState.Loading)
                viewModel.loadArticle()
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderState(state)
                }
            }
        }
    }

    private fun renderState(state: ArticleUiState) {
        val loading = state == ArticleUiState.Loading
        findViewById<View>(R.id.loadingIndicator).visibility = if (loading) View.VISIBLE else View.INVISIBLE
        findViewById<TextView>(R.id.stateName).setText(when (state) {
            ArticleUiState.Idle -> R.string.idle_state
            ArticleUiState.Loading -> R.string.loading_state
            is ArticleUiState.Success -> R.string.success_state
            is ArticleUiState.Error -> R.string.error_state
        })
        findViewById<TextView>(R.id.stateLabel).text = when (state) {
            ArticleUiState.Idle -> getString(R.string.idle_message)
            ArticleUiState.Loading -> getString(R.string.loading_message)
            is ArticleUiState.Success -> getString(R.string.success_message, state.title)
            is ArticleUiState.Error -> getString(R.string.error_message, state.message)
        }
        findViewById<Button>(R.id.loadButton).apply {
            isEnabled = !loading
            setText(if (state is ArticleUiState.Success || state is ArticleUiState.Error) R.string.reload_button else R.string.load_button)
        }
    }
}
