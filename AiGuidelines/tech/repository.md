# Repository Guidelines

## Overview
Repository classes handle data access and business logic coordination between different data sources (API, local storage, cache). This document outlines preferred patterns for repository implementation in any project.

## Prefer Concrete Classes Over Interfaces

### When NOT to use interfaces
- **Single implementation**: If there's only one implementation of a repository, avoid creating an interface
- **No testing variations**: If you don't need to swap implementations for testing
- **Simple CRUD operations**: For straightforward data access patterns
- **Local-only operations**: When operations don't require server interaction

### When TO use interfaces
- **Multiple implementations**: When you have different implementations (e.g., RemoteRepository, LocalRepository)
- **Testing requirements**: When you need to inject mock implementations for testing
- **Complex abstraction**: When the interface represents a complex business concept

## Local vs Server Operations

### Local Database Operations
Operations like marking jobs as "seen" or "applied", and saving jobs should use local database storage:

```kotlin
class JobRepository(
    private val apiService: JobApiService,
    private val savedJobDao: SavedJobDao,
    private val seenJobDao: SeenJobDao,
    private val backgroundExecutor: BackgroundExecutor = BackgroundExecutor.IO
) {

    suspend fun markJobAsSeen(jobId: String) =
        backgroundExecutor.execute {
            val seenJobEntity = SeenJobEntity(
                jobId = jobId,
                seenAt = Clock.System.now().toEpochMilliseconds(),
                isApplied = false
            )
            seenJobDao.upsert(seenJobEntity)
            Result.success(Unit)
        }

    suspend fun saveJob(job: Job): Result<Unit> =
        backgroundExecutor.execute {
            // Save to server first
            val request = SaveJobRequest(jobId = job.id)
            jobApiService.saveJob(request)

            // Then save to local database
            val savedJobEntity = SavedJobEntity(
                jobId = job.id,
                title = job.title,
                // ... map all fields
                savedAt = Clock.System.now().toEpochMilliseconds()
            )
            savedJobDao.upsert(savedJobEntity)
            Result.success(Unit)
        }
}
```

### Server Operations
Only send server requests for operations that need persistence:

```kotlin
suspend fun getJobs(page: Int, limit: Int): Result<List<Job>> {
    return apiService.getJobs(page, limit)
}

suspend fun saveJob(job: Job): Result<Unit> {
    return apiService.saveJob(job.id)
}
```

## Dependency Injection

### Concrete Class Injection
When using concrete classes, inject directly without interface binding:

```kotlin
// In Koin module
singleOf(::JobRepository)

// In consumer class
class HomeViewModel(
    private val jobRepository: JobRepository
) : ViewModel()
```

### Interface Injection (when needed)
Only when multiple implementations exist:

```kotlin
// In Koin module
singleOf(::RemoteJobRepository) bind JobRepository::class

// In consumer class
class HomeViewModel(
    private val jobRepository: JobRepository
) : ViewModel()
```

## BackgroundExecutor Usage

Repositories should use BackgroundExecutor for operations that need to run on background threads.

### Exception Handling with BackgroundExecutor

**IMPORTANT**: BackgroundExecutor already handles exceptions internally. When using `backgroundExecutor.execute()`, **do NOT wrap the code in additional try-catch blocks** unless you need specific exception handling logic beyond what BackgroundExecutor provides.

BackgroundExecutor's `execute()` method:
- Automatically catches all exceptions (except CancellationException)
- Logs errors with AppLogger
- Returns `Result.failure(exception)` for caught exceptions
- Returns the Result from your function if no exception occurs

### Correct Usage (Recommended)

```kotlin
class JobRepository(
    private val jobApiService: JobApiService,
    private val backgroundExecutor: BackgroundExecutor = BackgroundExecutor.IO
) {

    // CORRECT - No redundant try-catch needed
    suspend fun getJobs(page: Int, limit: Int): Result<List<Job>> =
        backgroundExecutor.execute {
            val response = jobApiService.getJobs(page, limit)
            response.handleAsResult { data ->
                Result.success(data?.asDomainModels() ?: emptyList())
            }
        }

    // CORRECT - Simple operations
    suspend fun saveJob(job: Job): Result<Unit> =
        backgroundExecutor.execute {
            jobApiService.saveJob(job.id)
            Result.success(Unit)
        }

    // Local operations - direct execution
    fun markJobAsSeen(jobId: String) {
        seenJobIds.add(jobId)
    }
}
```

### Incorrect Usage (Anti-Pattern)

```kotlin
// INCORRECT - Redundant try-catch block
suspend fun getJobs(page: Int, limit: Int): Result<List<Job>> =
    backgroundExecutor.execute {
        try {
            val response = jobApiService.getJobs(page, limit)
            response.handleAsResult { data ->
                Result.success(data?.asDomainModels() ?: emptyList())
            }
        } catch (e: Exception) {
            AppLogger.e("Error fetching jobs: ${e.message}")
            Result.failure(e)
        }
    }
```

### When to Use try-catch with BackgroundExecutor

Only use try-catch inside `backgroundExecutor.execute()` when you need:
- **Custom error handling logic** beyond logging and returning Result.failure
- **Different error types** to be handled differently
- **Resource cleanup** that must happen in the catch block

Example of justified try-catch usage:
```kotlin
suspend fun complexOperation(): Result<Data> = backgroundExecutor.execute {
    var resource: Resource? = null
    try {
        resource = acquireResource()
        val result = performComplexOperation(resource)
        Result.success(result)
    } catch (e: SpecificException) {
        // Custom handling for specific exception type
        handleSpecificError(e)
        Result.failure(CustomException("Custom error message"))
    } finally {
        resource?.release() // Required cleanup
    }
}
```

## API Service vs Repository Responsibilities

### API Services
- Return raw data types (not Result wrappers)
- Handle network communication
- Can throw exceptions for network errors

### Repositories
- Wrap API service calls with Result types
- Use BackgroundExecutor for background operations
- Handle caching and local state management
- Coordinate between different data sources

## Best Practices

1. **Keep it simple**: Don't over-abstract. Prefer concrete implementations until you need the flexibility
2. **Local state management**: Use in-memory collections for user interaction tracking
3. **Clear separation**: Distinguish between local operations (synchronous) and server operations (async with BackgroundExecutor)
4. **Error handling**: Handle network errors gracefully, but don't make local operations fail
5. **Caching strategy**: Cache server data locally, but keep user interactions separate
6. **Background execution**: Use BackgroundExecutor for all API calls and heavy operations

## Anti-Patterns

**Unnecessary interfaces**
```kotlin
interface JobRepository { /* only one implementation */ }
class JobRepositoryImpl : JobRepository { /* unnecessary abstraction */ }
```

**Server calls for local operations**
```kotlin
suspend fun markJobAsSeen(jobId: String): Result<Unit> {
    return apiService.markJobAsSeen(jobId) // Unnecessary server call
}
```

**Complex local operations**
```kotlin
suspend fun markJobAsSeen(jobId: String) {
    // Don't make simple local operations async
}
```

**Concrete implementation with clear separation**
```kotlin
class JobRepository(private val apiService: JobApiService) {
    // Server operations - async
    suspend fun getJobs(): Result<List<Job>> = apiService.getJobs()

    // Local operations - sync
    fun markJobAsSeen(jobId: String) { seenJobIds.add(jobId) }
}
```
