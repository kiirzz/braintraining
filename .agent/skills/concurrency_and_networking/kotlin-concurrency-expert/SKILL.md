---
name: kotlin-concurrency-expert
description: Three-phase review process for diagnosing and fixing coroutine issues — scope selection, dispatcher injection, cancellation safety, and exception handling.
---

# Kotlin Concurrency Expert

This guide provides a structured approach to reviewing and fixing Kotlin Coroutines issues in Android applications.

## Key Workflow Steps

The expert follows a three-phase approach:

1. **Triage** - Identify the specific symptom (ANR, memory leak, race condition), check library versions, and determine the current scope context and dispatcher strategy.

2. **Apply Minimal Fixes** - Preserve existing behavior while ensuring structured concurrency safety through targeted edits like migrating from `GlobalScope` to lifecycle-bound scopes or replacing deprecated `launchWhenStarted` with `repeatOnLifecycle`.

3. **Enforce Critical Rules** - Adhere to best practices around dispatcher injection for testability, lifecycle-aware collection patterns, state encapsulation, proper exception handling, and cooperative cancellation.

## Core Principles

**Scope Selection**: Use `viewModelScope` for ViewModel operations, `lifecycleScope` with `repeatOnLifecycle` for UI collection, and injected `applicationScope` for app-wide work. Never use `GlobalScope`.

**Dispatcher Injection**: "Inject dispatcher via constructor for testability" rather than hardcoding `Dispatchers.IO` or similar.

**Exception Safety**: Always rethrow `CancellationException` to respect coroutine cancellation semantics.

**Cooperative Cancellation**: Add `ensureActive()` or `yield()` in tight loops to enable responsive cancellation.

This approach balances safety with minimal code disruption.
