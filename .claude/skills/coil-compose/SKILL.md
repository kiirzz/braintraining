---
name: coil-compose
description: AsyncImage best practices, singleton ImageLoader setup, SubcomposeAsyncImage trade-offs, and how to add Coil to the Braintraining project.
---

# Coil for Jetpack Compose

**Primary Tool**: Use `AsyncImage` for most image loading scenarios in Compose. This composable automatically handles size resolution and integrates seamlessly with standard Image parameters.

**Key Quote**: "Use `AsyncImage` for most use cases. It handles size resolution automatically and supports standard `Image` parameters."

**Alternative Options**:
- `rememberAsyncImagePainter` - only when you specifically need a Painter object rather than a composable
- `SubcomposeAsyncImage` - for custom loading/error state UI, but avoid in scrollable lists due to performance concerns

**Essential Best Practices**:
1. Maintain a single `ImageLoader` instance app-wide to share caching
2. Enable `crossfade(true)` for smoother visual transitions
3. Always include meaningful `contentDescription` values for accessibility
4. Set `contentScale` appropriately to prevent unnecessary large image loads
5. Avoid subcomposition in performance-sensitive areas like `LazyColumn`

The documentation emphasizes that Coil is the recommended choice for Compose because it "executes image requests on a background thread automatically" and integrates efficiently with the framework.

## Adding Coil to Braintraining

Coil is not yet in this project. To add it:

1. Add to `gradle/libs.versions.toml`:
```toml
[versions]
coil = "2.7.0"

[libraries]
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }
```

2. Add dependency to the module that needs image loading:
```kotlin
implementation(libs.coil.compose)
```

3. Provide a singleton `ImageLoader` via Hilt in `:core:network` or `:app`.
