package com.braintraining.feature.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.braintraining.core.data.repository.GameRepository
import com.braintraining.core.data.repository.SkillRepository
import com.braintraining.core.model.Game
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    skillRepository: SkillRepository
): ViewModel() {
    private val _game = MutableStateFlow<Game?>(null)
    val game: StateFlow<Game?> = _game

    val skillWithGames = skillRepository.getSkillsWithGames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getGame(id: String) {
        viewModelScope.launch {
            gameRepository.getGame(id).collect { gameDetail ->
                _game.value = gameDetail
            }
        }
    }
}