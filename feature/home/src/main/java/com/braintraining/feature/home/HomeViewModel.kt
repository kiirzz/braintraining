package com.braintraining.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.braintraining.core.data.repository.GameRepository
import com.braintraining.core.model.Game
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    gameRepository: GameRepository,
) : ViewModel() {
    val games: StateFlow<List<Game>> = gameRepository.getGames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
