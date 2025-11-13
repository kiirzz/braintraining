package com.braintraining.feature.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.braintraining.core.data.repository.SkillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    skillRepository: SkillRepository
): ViewModel() {
    val skillWithGames = skillRepository.getSkillsWithGames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}