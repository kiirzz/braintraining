package com.braintraining.feature.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.braintraining.core.data.repository.GameRepository
import com.braintraining.core.data.repository.SkillRepository
import com.braintraining.core.model.Game
import com.braintraining.core.model.Skill
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
    private val skillRepository: SkillRepository
): ViewModel() {
    private val _game = MutableStateFlow<Game?>(null)
    private val _skill = MutableStateFlow<Skill?>(null)
    val game: StateFlow<Game?> = _game
    val skill: StateFlow<Skill?> = _skill


    val skillWithGames = skillRepository.getSkillsWithGames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getGame(id: String) {
        viewModelScope.launch {
            gameRepository.getGame(id).collect { gameDetail ->
                _game.value = gameDetail
            }
        }
    }

    fun getSkill(id: String) {
        viewModelScope.launch {
            skillRepository.getSkill(id).collect { skillDetail ->
                _skill.value = skillDetail
            }
        }
    }
}