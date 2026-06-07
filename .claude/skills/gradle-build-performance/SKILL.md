---
name: gradle-build-performance
description: Diagnosing and optimising Android build times — configuration cache, build cache, KSP vs kapt, parallel execution, and CI/CD best practices.
---

# Gradle Build Performance Guide

This resource provides a comprehensive framework for diagnosing and improving Android/Gradle build times. It's designed for developers experiencing slow builds, investigating performance regressions, or optimizing CI/CD pipelines.

## Key Workflow

The guide recommends a systematic approach: establish baseline measurements, generate build scans for analysis, identify which phase is bottlenecked (initialization, configuration, or execution), apply single optimizations, measure improvements, and verify changes in build scan data.

## Primary Optimization Strategies

The document outlines 12 optimization patterns, including enabling configuration and build caching, parallel execution, and migrating from kapt to KSP annotation processing. It emphasizes: "Caches configuration phase across builds" and notes that KSP offers approximately twice the performance of kapt for Kotlin projects.

Other strategies involve increasing JVM heap allocation, pinning dependency versions instead of using dynamic ranges, optimizing repository order, and deferring I/O operations from configuration time to execution time.

## Troubleshooting Approach

The guide provides targeted solutions for common scenarios:
- **Slow configuration**: Replace eager task creation with lazy registration
- **Slow compilation**: Break large modules into smaller features or migrate annotation processors
- **Cache misses**: Standardize JDK versions and use proper task caching annotations

## CI/CD Integration

For continuous integration environments, recommendations include configuring remote build caches, implementing Gradle Enterprise/Develocity for analytics, and selectively skipping unnecessary tasks based on change scope.

## Braintraining Specifics

- This project already uses **KSP** (not kapt) for both Hilt and Room — a key performance advantage.
- Dependency versions are pinned in `gradle/libs.versions.toml` — no dynamic version ranges.
- `TYPESAFE_PROJECT_ACCESSORS` is enabled for faster project dependency resolution.
- Add `org.gradle.configuration-cache=true` and `org.gradle.caching=true` to `gradle.properties` to further improve build times.
