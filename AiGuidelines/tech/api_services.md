# API Services Guidelines

Purpose: Keep remote APIs well-structured, testable, and decoupled from domain models.

- Transport: Ktor
  - Use Ktor Client for HTTP transport across platforms.
  - Centralize Ktor client creation (engine, JSON, logging, timeouts, retries) in a single provider/factory.
  - Configure content negotiation with Kotlinx Serialization; do not leak serialization annotations into domain models.

- Location
  - **REQUIRED**: Place ALL API service classes under composeApp/src/commonMain/kotlin/com/<org>/<app>/data/source/remote/apiservices.
  - Keep request/response DTOs under data/source/remote/request and data/source/remote/response respectively.
  - Request and Response classes **MUST** have Request and Response suffix in their class names
  - All request and response classes **MUST** use @Serializable and @SerialName annotations for JSON serialization
  - For platform-specific transport code, use expect/actual or platform source sets (androidMain, iosMain), keeping the interface common.

- Interfaces & Calls
  - Prefer small, cohesive interfaces grouped by bounded contexts (e.g., AuthService, MediaService).
  - Use suspending functions for network calls. Avoid callbacks.
  - Represent responses with DTOs tailored to transport, not domain.
  - **Return raw data types, not Result wrappers**. Let repositories handle Result wrapping and error coordination.

- Error Handling
  - Normalize low-level errors (timeouts, IO, HTTP status) into a consistent error model at the data boundary.
  - **Let exceptions propagate to repository layer**. Don't wrap in Result at API service level.

## Implementation Patterns

### API Service Interface
```kotlin
interface JobApiService {
    suspend fun getJobs(request: GetJobsRequest): GetJobsResponse
    suspend fun saveJob(request: SaveJobRequest): SaveJobResponse
}
```

### Response Classes with Domain Mapping
Response classes should include asDomain() methods for converting DTOs to domain models:

```kotlin
@Serializable
data class JobResponse(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    // ... other fields
) {
    fun asDomain(): Job {
        return Job(
            id = id,
            title = title,
            // ... map all fields
        )
    }
}
```

### Repository Integration
```kotlin
class JobRepository(
    private val jobApiService: JobApiService,
    private val backgroundExecutor: BackgroundExecutor = BackgroundExecutor.IO
) {
    suspend fun getJobs(page: Int, limit: Int): Result<List<Job>> =
        backgroundExecutor.execute {
            val request = GetJobsRequest(page = page, limit = limit)
            val response = jobApiService.getJobs(request)
            val jobs = response.jobs.map { it.asDomain() }
            Result.success(jobs)
        }
}
```


- Coroutines & Threading
  - Execute network IO on appropriate dispatchers. Keep API functions themselves suspend; delegate dispatcher control to the caller if needed.
  - Propagate coroutineContext to Ktor calls for cancellation support.

- Configuration
  - Centralize base URL(s), API keys, timeouts, and retry policy in a single configuration point.
  - Keep secrets out of source control; read from environment, local.properties, or secure keystores.

- Testing
  - Provide fakes or stubbed implementations for tests. Avoid hitting real network in unit tests.
  - Use contract tests for serialization/mapping where practical.
  - For Ktor, prefer MockEngine to simulate responses and errors.

- Alignment with Product Docs
  - Ensure endpoints, payloads, and status handling align with `AiGuidelines/project/prd.md`. When UX requires specific error messages or states, follow `AiGuidelines/project/user_flow.md`. Surface discrepancies explicitly.
