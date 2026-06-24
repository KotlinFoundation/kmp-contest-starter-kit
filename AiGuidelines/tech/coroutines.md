# Coroutines & Concurrency Guidelines

Purpose: Ensure coroutine usage is testable, predictable, and aligned with platform lifecycles.

- Scopes
  - backgroundScope: Use a dedicated CoroutineScope with SupervisorJob() and Dispatchers.IO for repository/data work.
    - Inject the IO dispatcher (CoroutineDispatcher) for testability. In tests, use a TestDispatcher.
    - Example construction (DI): backgroundScope = CoroutineScope(SupervisorJob() + ioDispatcher)
  - ApplicationScope: For jobs that must outlive screens/features (e.g., warm caches, analytics upload, periodic sync), launch in an Application-level scope.
    - Provide this scope via DI. Tie its Job to app process lifetime.

- Dispatchers
  - Use Dispatchers.IO (injected) for blocking IO or network-bound work.
  - Confine CPU-heavy work to Dispatchers.Default (injected) when needed.
  - Avoid hardcoding Dispatchers in code; depend on abstractions (e.g., a DispatchersProvider) to improve testability.

- Repositories
  - Expose suspend functions and Flows. Perform IO using backgroundScope.
  - For long-running operations that should continue across screens, delegate to ApplicationScope.
  - Use withContext(ioDispatcher) around discrete IO when a new scope is not required.

- Structured Concurrency
  - Prefer structured concurrency; avoid GlobalScope.
  - Use SupervisorJob for scopes handling independent child coroutines so one failure doesn't cancel siblings.

- Cancellation & Timeouts
  - Make network/database calls cancellable. Propagate coroutineContext to Ktor/SQL drivers.
  - Use timeouts judiciously (withTimeout) and map cancellations/timeouts to domain errors as needed.

- Testing
  - Inject dispatchers and scopes; in unit tests, use StandardTestDispatcher/UnconfinedTestDispatcher and TestScope.
  - Avoid real delays in tests; use TestCoroutineScheduler to advance time.
