---
name: android-gradle-logic
description: Centralising build config via convention plugins and Version Catalogs (libs.versions.toml); covers Spotless, KSP, and Braintraining custom plugins.
---

# Android Gradle Build Logic & Convention Plugins

## Overview
This guide addresses the challenge of maintaining Android projects by eliminating redundant build configuration code. Rather than duplicating settings across multiple `build.gradle.kts` files, the approach centralizes common logic into reusable convention plugins.

## Core Concept
The framework uses two primary tools:

1. **Convention Plugins**: Encapsulate shared build logic (Compose configuration, Kotlin settings, dependency injection setup) into plugins applied across modules
2. **Version Catalogs**: Centralize dependency and plugin declarations in `libs.versions.toml`

## Implementation Structure

A typical project layout includes:
- A `build-logic` composite build containing convention plugin definitions
- A `gradle` directory housing the version catalog
- Module-level build files that reference these shared configurations

## Key Setup Steps

**Plugin Management**: The root `settings.gradle.kts` must reference the build-logic directory and configure repositories for plugin resolution.

**Version Catalog**: Define plugin versions and references in a centralized TOML file, enabling consistent dependency management across the project.

**Plugin Creation**: Custom plugins inherit from Gradle's `Plugin<Project>` interface and configure project extensions like `ApplicationExtension` for Android-specific settings.

**Plugin Registration**: Each convention plugin requires registration in its module's build file with a unique ID and implementation class reference.

## Practical Benefit
By applying these plugins via `alias(libs.plugins.custom-plugin-name)`, individual modules maintain minimal configuration, improving maintainability and reducing errors from inconsistent settings.

## Braintraining Specifics

This project uses the following custom convention plugins (defined in `build-logic/`):

| Plugin ID | Purpose |
|---|---|
| `braintraining.android.application` | Standard app module config (compileSdk, minSdk, Kotlin, Java 11) |
| `braintraining.android.room` | Adds Room + KSP annotation processing |
| `braintraining.hilt` | Adds Hilt + KSP compiler |

**Version catalog location**: `gradle/libs.versions.toml`

**Code style**: Spotless is configured in the root `app/build.gradle.kts` `subprojects` block — runs ktlint on `.kt`, Google Java Format on `.java`, and ktlint on `.gradle.kts`.

Run `./gradlew spotlessApply` to auto-fix formatting before committing.
