---
name: android-data-layer
description: Repository pattern with Room as single source of truth, Retrofit for remote data, and sync strategies (stale-while-revalidate, outbox pattern).
---

# Android Data Layer & Offline-First Architecture

This guidance document outlines implementing a data layer using the Repository pattern with Room (local storage) and Retrofit (remote API calls) for offline-first synchronization.

## Key Components

**Repository Pattern**: Serves as the "Single Source of Truth (SSOT)" by deciding whether to return cached or fresh data. The example shows a NewsRepository exposing local database data via Flow while providing a `refreshNews()` function for synchronization.

**Local Storage**: Room databases function as "Primary cache and offline storage" with observable data returned through Flow. Entity and DAO classes manage structured data access.

**Remote Data**: Retrofit handles backend communication using suspend functions for asynchronous operations. The guidance emphasizes wrapping network calls with error handling for common failures like network unavailability or 404 responses.

**Synchronization Strategies**:
- Read operations use "Stale-While-Revalidate" (display local data immediately, refresh in background)
- Write operations employ the "Outbox Pattern" (save locally as unsynced, use WorkManager to push to server)

**Dependency Injection**: Hilt modules bind repository interfaces to implementations for clean architectural separation.

This approach prioritizes user experience by enabling offline functionality while maintaining data consistency through thoughtful synchronization patterns.

## Braintraining Specifics

In this project:
- `GameRepository` and `SkillRepository` interfaces live in `:core:data`
- `DefaultGameRepository` / `DefaultSkillRepository` are the implementations
- `NetworkDataSource` is the single facade over all Retrofit API interfaces
- Entity ↔ model mapping uses `asExternalModel()` / `asEntity()` extension functions defined in `:core:database` entity files
- `DataModule` uses `@Binds` to wire interfaces to implementations
