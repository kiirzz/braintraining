---
name: android-testing
description: Testing pyramid for modern Android — JUnit 4 unit tests, Hilt integration tests, Turbine for Flow, Roborazzi screenshot tests, and Braintraining conventions.
---

# Android Testing Strategies

This resource outlines a comprehensive testing approach for modern Android applications, structured around three key tiers:

## Core Testing Levels

The framework emphasizes a **testing pyramid** with unit tests at the foundation (fast, logic-focused), integration tests in the middle (testing interactions), and UI/screenshot tests at the top (visual verification).

## Key Testing Tools

The guidance covers several essential libraries:
- **JUnit 4** for test structure
- **Kotlin Coroutines Test** (`runTest`) for asynchronous operations
- **Hilt** (`HiltAndroidRule`) for dependency injection in tests
- **Roborazzi** for screenshot testing without emulators

## Notable Approach: Roborazzi

As stated in the document, "Roborazzi" is highlighted because it "runs on the JVM (fast) without needing an emulator." This represents a significant advantage for CI/CD pipelines and local development workflows.

## Hilt Integration Testing

The resource demonstrates using `HiltAndroidRule` to inject dependencies during testing, enabling realistic database and service testing scenarios.

## Execution

Tests are run via Gradle commands:
- Unit tests: `./gradlew test`
- Instrumented tests: `./gradlew connectedAndroidTest`
- Screenshot record: `./gradlew recordRoborazziDebug`
- Screenshot verify: `./gradlew verifyRoborazziDebug`

## Braintraining Test Conventions

- **Unit tests**: `src/test/` — test ViewModels with `runTest` + fake repositories
- **Instrumented tests**: `src/androidTest/` — Room DAO tests with in-memory database
- ViewModel tests should assert `StateFlow` values using Turbine's `test { }` builder
- Mock with MockK; avoid Mockito (not Kotlin-idiomatic)
- Never test private implementation details — test observable state only
