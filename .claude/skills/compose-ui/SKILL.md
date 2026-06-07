---
name: compose-ui
description: State hoisting, modifier usage, remember/derivedStateOf, theming with MaterialTheme, performance tips, and Braintraining-specific Compose conventions.
---

# Jetpack Compose Best Practices

## Key Guidelines

**State Management**: "Make Composables stateless whenever possible by moving state to the caller." This approach decouples UI from state storage, improving testability and preview capabilities.

**Function Signature Pattern**: Components should accept state as parameters flowing downward and callbacks for events flowing upward, with a standard modifier parameter:
```kotlin
fun MyComponent(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
)
```

**Modifier Usage**: Always include `modifier: Modifier = Modifier` as the first optional parameter and apply it to the root layout element. Note that modifier order matters — `padding().clickable()` differs from `clickable().padding()`.

**Performance Techniques**:
- Use `remember { ... }` for caching expensive computations
- Apply `derivedStateOf { ... }` when state changes frequently but UI only needs threshold-based updates
- Prefer stable lambda references (method references or remembered lambdas) to avoid unnecessary child recompositions

**Theming**: Utilize `MaterialTheme.colorScheme` and `MaterialTheme.typography` rather than hardcoded values. All design tokens come from `:core:systemdesign` (Theme, Color, Type, Shape).

**Testing & Previews**: Create preview functions for all public Composables with light/dark mode variants and static dummy data.

## Braintraining Conventions

- Import theme via `:core:systemdesign` — never define colors or typography locally in feature modules.
- Game screens (`game:*`) should call `setShowTopBar(false)` and `setShowBottomBar(false)` via the NavGraph callbacks to go full-screen.
- Use `LazyColumn` / `LazyRow` for any list that could exceed ~10 items.
- Avoid `SubcomposeAsyncImage` inside lazy lists (use `AsyncImage` instead).
- All public Composables must have at least one `@Preview`.
