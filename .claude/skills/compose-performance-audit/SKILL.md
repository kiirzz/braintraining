---
name: compose-performance-audit
description: Workflow for identifying and fixing Compose runtime performance issues — recomposition storms, unstable parameters, lazy list keys, and profiling guidance.
---

# Compose Performance Audit

## Overview

This workflow guides auditing Jetpack Compose runtime performance through code review, profiling, and targeted remediation.

## Workflow Decision Tree

The audit follows three paths:
- **Code-First Review**: When code is available
- **Symptom-Based**: When only problems are described
- **Profiling-Guided**: When code review is inconclusive

## Key Focus Areas

The audit examines:
- Recomposition storms from unstable parameters
- Unstable keys in lazy lists causing churn
- Heavy computation during composition phases
- Missing `remember` blocks causing recreation
- Large images without constraints
- Deep nesting and layout complexity

## Common Performance Issues & Solutions

**Unstable lambdas**: Wrap callbacks in `remember(dependencies)` rather than creating new instances per recomposition.

**Expensive composition work**: Use `remember(keys)` to cache sorted lists, filtered data, and other computed values.

**Missing lazy list keys**: Always provide stable, unique IDs via the `key` parameter instead of relying on indices.

**Unstable data classes**: Mark truly immutable classes with `@Immutable` and use `ImmutableList` from kotlinx.

**State reads during composition**: Defer reads to layout/draw phases using `Modifier.offset { }` patterns.

## Profiling Guidance

Users should collect:
- Layout Inspector recomposition counts
- Perfetto traces for frame timing
- Release builds with R8 optimization enabled

## Verification

Compare metrics before/after fixes using Layout Inspector counts, Macrobenchmark results, and real-device testing on release builds.
