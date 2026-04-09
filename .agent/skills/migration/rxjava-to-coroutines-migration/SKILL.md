---
name: rxjava-to-coroutines-migration
description: Type-by-type mapping from RxJava (Single, Observable, Subject) to Coroutines/Flow equivalents, threading migration, and step-by-step conversion workflow.
---

# RxJava to Kotlin Coroutines Migration Skill

This skill guides developers in converting RxJava-based asynchronous code to Kotlin Coroutines and Flow.

## Core Migration Mappings

**Single-value operations:**
- `Single<T>` becomes a `suspend fun ...(): T`
- `Maybe<T>` becomes `suspend fun ...(): T?`
- `Completable` becomes `suspend fun ...()`

**Stream operations:**
- `Observable<T>` and `Flowable<T>` both map to `Flow<T>`

**Reactive subjects:**
- `PublishSubject<T>` → `MutableSharedFlow<T>`
- `BehaviorSubject<T>` → `MutableStateFlow<T>`
- `ReplaySubject<T>` → `MutableSharedFlow<T>(replay = N)`

**Threading:**
Schedulers convert to Dispatchers: `Schedulers.io()` → `Dispatchers.IO`, `AndroidSchedulers.mainThread()` → `Dispatchers.Main`. The guide notes that "subscribeOn and observeOn are typically replaced by withContext(Dispatcher) or flowOn(Dispatcher)."

## Implementation Process

The recommended workflow involves:
1. Analyzing existing RxJava chains
2. Converting sources (suspend functions for single values, Flow for streams)
3. Translating operators to coroutine equivalents
4. Updating subscriptions with `launch` and `collect`
5. Replacing error handlers with try/catch blocks
6. Adjusting threading as needed

## Key Recommendation

The guide emphasizes: "Default to suspend functions instead of Flow unless you actually have a stream of multiple values over time."

## Braintraining Note

This project was started with Coroutines from day one — no RxJava exists. Use this skill only if integrating a third-party library that returns RxJava types.
