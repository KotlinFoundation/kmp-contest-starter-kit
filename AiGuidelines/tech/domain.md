# Domain Layer Guidelines

Purpose: Make domain models and logic stable, pure, platform-agnostic, and easy to test.

- Location
  - Place domain models and interfaces under composeApp/src/commonMain (e.g., package com.<org>.<app>.domain).
  - Keep domain free of platform-specific types (no Android/iOS classes, no Ktor, no SQL types).

- Models
  - Prefer immutable data classes with val properties.
  - Provide sensible default values for optional fields to simplify construction and testing. Do not add defaults that hide required invariants.
    - Example: data class User(id: UserId, name: String = "", avatarUrl: String? = null)
  - Keep models "pure domain": no serialization annotations, no persistence concerns, no UI-specific fields.
  - Provide pure, side-effect-free helpers/derivations via functions or extension functions.

- Domain Exceptions (business rules)
  - Domain layer is allowed to throw the following when appropriate and explicitly modeled by PRD/user flows:
    - UnauthenticatedException — thrown when an operation requires a signed-in user.
    - PurchaseRequiredException — thrown when an operation requires an active purchase/subscription.
  - Define them in a common domain/exceptions package

- APIs and Boundaries
  - MVP-first minimalism: avoid overengineering. Do NOT add pass-through use cases that only call a single repository method. Call repositories directly from the presentation layer when no domain policy/orchestration is needed.
  - Introduce a use case only when it adds value: aggregates multiple repositories, enforces business rules, coordinates transactions, applies throttling/debouncing, validation, or cross-cutting concerns.
  - Repository interfaces (Project policy): Do not define repository interfaces in the domain. Prefer concrete repositories in the data layer to reduce indirection. Only introduce an interface if there is a concrete, current need (e.g., two live implementations to swap at runtime) and after explicit approval in the issue/PR; otherwise, keep it concrete.
  - Implementations (network/database) live in data layers and depend on domain, not vice versa.
  - Expose asynchronous data as suspend functions and/or Kotlin Flow/StateFlow.

- Mapping
  - Domain models are the source of truth inside the app. Map from DTOs/entities at the edges (data layer) into domain models.
  - Keep mapping logic close to the boundary (e.g., data.remote -> domain, data.local -> domain) and unit-test it.

- Error Handling
  - Convert transport/storage errors into meaningful domain errors at the boundary.
  - Use sealed results for recoverable cases; reserve exceptions for truly exceptional or control-flow dictated cases as above.

- Testing
  - Domain should be unit-testable without Android/iOS. Use simple fakes for repository interfaces.
  - Cover invariants and exception cases (Unauthenticated, PurchaseRequired) with tests.

- Alignment with Product Docs
  - When modeling entities, constraints, and states, prefer the canonical definitions from `AiGuidelines/project/prd.md` and `AiGuidelines/project/user_flow.md`. If conflicts arise, follow PRD for data rules and user_flow for sequence/UX; call out discrepancies.
