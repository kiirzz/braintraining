---
name: android-viewmodel
description: StateFlow/SharedFlow patterns, lifecycle-safe collection, thread-safe state updates, and WhileSubscribed(5000) stateIn convention.
---

# Android ViewModel & State Management Guide

This document outlines best practices for Android ViewModels with focus on state management patterns.

## Key Concepts

**UI State Management**: The guide recommends using `StateFlow<UiState>` to represent persistent UI states like Loading, Success, or Error conditions. As specified, "Expose as a read-only `StateFlow` backing a private `MutableStateFlow`" to maintain encapsulation while allowing safe observation.

**Transient Events**: For one-time occurrences (toasts, navigation), implement `SharedFlow<UiEvent>` with `replay = 0`. This configuration prevents events from being re-triggered after configuration changes, which is critical for preventing duplicate notifications.

**UI Collection Patterns**:
- Compose apps should use `collectAsStateWithLifecycle()` for StateFlow
- XML-based Views should use `repeatOnLifecycle(Lifecycle.State.STARTED)` within coroutines

**Thread Safety**: State updates should employ `.update { oldState -> ... }` rather than direct assignment to ensure thread-safe modifications.

**Scope**: All ViewModel-initiated coroutines must use `viewModelScope`, with complex operations delegated to UseCases or Repositories for better separation of concerns.

## Braintraining Pattern

```kotlin
@HiltViewModel
class GameViewModel @Inject constructor(
    skillRepository: SkillRepository
) : ViewModel() {

    val skillWithGames = skillRepository.getSkillsWithGames()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
}
```

- Always use `WhileSubscribed(5000)` — keeps the flow active for 5 s after the last subscriber to survive configuration changes.
- Inject repository **interfaces**, never concrete implementations or DAOs.
- For one-off UI state (loading, error), use a sealed `UiState` class backed by `MutableStateFlow`.
