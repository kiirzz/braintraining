---
name: compose-navigation
description: Current string-route NavGraph setup in Braintraining, adding new destinations, bar visibility callbacks, and a migration path to type-safe routes.
---

# Compose Navigation

This documentation covers implementing navigation in Jetpack Compose using the Navigation Compose library.

## Key Setup Requirements

The project uses Navigation Compose 2.8.3 with string-based routes defined in the `Dest` object.

## Current Navigation Pattern (String Routes)

```kotlin
object Dest {
    const val Home = "home"
    const val Games = "games"
    const val Stats = "stats"
    const val Setting = "setting"
    const val GameDetailWithId = "game_detail/{gameId}"
}
```

Arguments are passed in the route path and retrieved via `it.arguments?.getString("gameId")`.

Bar visibility (top bar, bottom bar, back button) is controlled by callbacks passed from `MainActivity` into `NavGraph`:
```kotlin
fun NavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues,
    setShowBackButton: (Boolean) -> Unit,
    setShowTopBar: (Boolean) -> Unit,
    setShowBottomBar: (Boolean) -> Unit,
)
```

## Adding a New Destination

1. Add a constant to `Dest`
2. Add a `composable(Dest.NewScreen) { ... }` block in `NavGraph.kt`
3. Set bar visibility callbacks appropriately
4. Add a bottom nav item to `BottomNavbar.kt` if needed

## Type-Safe Routes (Future Migration)

Navigation Compose 2.8+ supports `@Serializable` objects/data classes as type-safe routes. Consider migrating when adding complex argument types:

```kotlin
@Serializable data class GameDetail(val gameId: String)
// Then: navController.navigate(GameDetail(id))
```

## Core Navigation Operations

**Navigate forward**: `navController.navigate(Dest.Games)`

**Navigate with argument**: `navController.navigate("game_detail/$gameId")`

**Navigate up**: `navController.navigateUp()`

**Navigate clearing back stack**:
```kotlin
navController.navigate(Dest.Home) {
    popUpTo(0) { inclusive = true }
}
```
