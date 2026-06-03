---
name: android-retrofit
description: Type-safe HTTP with Retrofit — URL handling, request/response formats, header management, Hilt singleton setup, and error handling at the repository layer.
---

# Android Networking with Retrofit

## Key Takeaways

This guide provides modern best practices for implementing type-safe HTTP networking in Android using Retrofit (2025 standards).

**URL Handling**: The framework supports dynamic path segments via `{name}` placeholders paired with `@Path` annotations, individual query parameters using `@Query`, and bulk parameter sets through `@QueryMap`.

**Request Formats**: You can transmit data as JSON bodies using `@Body`, form-encoded data via `@FormUrlEncoded` with `@Field`, or multipart uploads combining `@Multipart` with `@Part` annotations.

**Header Management**: Headers can be applied statically with `@Headers`, dynamically through `@Header` parameters, or globally using OkHttp interceptors.

**Response Patterns**: Direct return types throw exceptions for non-2xx status codes, while wrapping responses in `Response<T>` enables manual status checking without exceptions.

**Dependency Injection**: The guide demonstrates configuring Retrofit as a singleton within a Hilt module, including JSON serialization settings and OkHttpClient customization with logging and timeout parameters.

**Error Handling**: Network exceptions should be caught at the repository layer using patterns like `runCatching` to maintain clean UI state separation.

The provided checklist emphasizes using suspend functions, choosing appropriate response wrapper types, avoiding manual URL construction, configuring client timeouts, and maintaining domain model separation from API DTOs.

## Braintraining Specifics

- Serialization: `kotlinx.serialization` via `retrofit2-kotlinx-serialization-converter`
- HTTP client: OkHttp with `HttpLoggingInterceptor`
- APIs: `GameApi`, `SkillApi` — injected into `NetworkDataSource` (singleton facade)
- DTOs (`NetworkGame`, `NetworkSkill`, `GameDto`, `SkillDto`) live in `:core:network` and are never exposed outside that module
- Mapping from DTO → domain model happens in the repository via `asExternalModel()` / `asEntity()`
