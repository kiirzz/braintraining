# Braintraining — Android Development Guidelines

## Project Overview

**Braintraining** is an Android cognitive-training app built with a multi-module, offline-first architecture inspired by [Now in Android](https://github.com/android/nowinandroid). Users browse skill categories and play brain-training games.

## Technology Stack

| Concern | Choice |
|---|---|
| Language | Kotlin 2.1.10 |
| Min SDK | 24 |
| Compile/Target SDK | 36 |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose (string routes) |
| DI | Hilt 2.56 |
| Async | Kotlin Coroutines 1.9 + Flow |
| Local DB | Room |
| Network | Retrofit + OkHttp + kotlinx.serialization |
| Build | Gradle Kotlin DSL + Version Catalog (`gradle/libs.versions.toml`) |
| Code style | Spotless + ktlint |

## Architecture

The app follows a strict multi-module layered architecture. See `.agent/skills/architecture/SKILL.md` for the complete module map and dependency rules.

```
:app  →  :feature:*  /  :game:*  →  :core:data  →  :core:database  +  :core:network
                                                 →  :core:model
                                  :core:systemdesign
```

**Key principles:**
- Unidirectional Data Flow (UDF): UI observes ViewModel → ViewModel consumes Repository → Repository coordinates Room + Network
- Room is always the single source of truth. Network calls only populate local DB; UI never reads network directly.
- No domain/use-case layer currently — Repositories are injected directly into ViewModels.

## Asynchronous Programming

- Use **Kotlin Coroutines exclusively**. No RxJava.
- Inject `CoroutineDispatcher` via constructor — never hardcode `Dispatchers.IO` in source code.
- Repositories expose `Flow<T>` for reads and `suspend fun refresh*()` for network sync.
- ViewModels use `.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initial)`.
- Collect in UI with `collectAsStateWithLifecycle()`.

## Dependency Injection

- `@HiltAndroidApp` on `BrainTrainingApp`.
- `@HiltViewModel` on every ViewModel.
- One DI module per layer: `DatabaseModule`, `DaosModule`, `NetworkModule`, `DataModule`.
- Use `@Binds` (not `@Provides`) to bind interfaces to implementations.

## UI

- All new screens: Jetpack Compose. No new XML layouts.
- Screens are stateless `@Composable` functions; state lives in ViewModel.
- Navigation via string routes in `Dest` object → `NavGraph`.
- Design tokens from `:core:systemdesign` (Theme, Color, Type, Shape).

## Testing

- Unit tests: JUnit 4 + MockK + Turbine (for Flow).
- Integration tests: Hilt test rules + in-memory Room database.
- UI tests: Compose test rule.
- Test ViewModels by asserting `StateFlow` emissions, not implementation details.

## Code Style

- Spotless + ktlint enforced on all `.kt` and `.gradle.kts` files.
- Run `./gradlew spotlessApply` before committing.

## Skill Directory

| Topic | Path |
|---|---|
| Project Architecture | `.agent/skills/architecture/SKILL.md` |
| Android Architecture Patterns | `.agent/skills/architecture/android-architecture/SKILL.md` |
| Data Layer & Offline-First | `.agent/skills/architecture/android-data-layer/SKILL.md` |
| ViewModel & State | `.agent/skills/architecture/android-viewmodel/SKILL.md` |
| Gradle Build Logic | `.agent/skills/build_and_tooling/android-gradle-logic/SKILL.md` |
| Coroutines | `.agent/skills/concurrency_and_networking/android-coroutines/SKILL.md` |
| Retrofit Networking | `.agent/skills/concurrency_and_networking/android-retrofit/SKILL.md` |
| Concurrency Expert | `.agent/skills/concurrency_and_networking/kotlin-concurrency-expert/SKILL.md` |
| RxJava → Coroutines | `.agent/skills/migration/rxjava-to-coroutines-migration/SKILL.md` |
| XML → Compose | `.agent/skills/migration/xml-to-compose-migration/SKILL.md` |
| Compose Performance | `.agent/skills/performance/compose-performance-audit/SKILL.md` |
| Gradle Performance | `.agent/skills/performance/gradle-build-performance/SKILL.md` |
| Testing Strategies | `.agent/skills/testing_and_automation/android-testing/SKILL.md` |
| Accessibility | `.agent/skills/ui/android-accessibility/SKILL.md` |
| Coil Image Loading | `.agent/skills/ui/coil-compose/SKILL.md` |
| Compose Navigation | `.agent/skills/ui/compose-navigation/SKILL.md` |
| Compose UI Best Practices | `.agent/skills/ui/compose-ui/SKILL.md` |
