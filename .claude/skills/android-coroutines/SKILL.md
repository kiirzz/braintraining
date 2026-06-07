---
name: android-coroutines
description: Production-grade Coroutines patterns — dispatcher injection, main-safety, lifecycle collection, structured concurrency, error handling, and Flow best practices.
---

# Android Coroutines Expert Skill Summary

This resource establishes production-grade patterns for Kotlin Coroutines on Android, emphasizing structured concurrency and lifecycle safety.

## Key Responsibilities
The skill covers async logic implementation, reactive streams (Flow/StateFlow), lifecycle integration, error handling, cancellability, and testing approaches.

## Critical Rules

**Dispatcher Management**: "NEVER hardcode Dispatchers...ALWAYS inject a CoroutineDispatcher via the constructor." This enables testability.

**Main-Safety**: Data/Domain layer functions must be main-safe, using `withContext(dispatcher)` to shift execution off the UI thread.

**Lifecycle Collection**: Replace deprecated patterns with `repeatOnLifecycle(Lifecycle.State.STARTED)` for safe flow collection in Activities/Fragments.

**State Encapsulation**: Never expose mutable flows publicly; use read-only `StateFlow` or `Flow` instead.

**No GlobalScope**: "NEVER use GlobalScope. It breaks structured concurrency and leads to leaks."

**Exception Handling**: Avoid catching `CancellationException` generically without rethrowing; use `CoroutineExceptionHandler` only for top-level launches.

**Cancellability**: Implement `ensureActive()` or `yield()` in loops to enable cooperative cancellation.

**Callback Conversion**: Use `callbackFlow` with `awaitClose` to safely convert callback APIs.

The resource includes practical patterns for repositories, parallel execution via `coroutineScope`, and testing with `runTest`.

## Braintraining Usage

- Repositories expose `Flow<T>` (from Room) for reads — no manual dispatcher needed, Room is main-safe.
- `suspend fun refresh*()` in repositories should use `withContext(Dispatchers.IO)` if doing non-Room work.
- ViewModels collect with `.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initial)`.
- Compose UI collects with `collectAsStateWithLifecycle()`.
