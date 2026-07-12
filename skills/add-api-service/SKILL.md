---
name: add-api-service
description: Add a Ktor-backed network request end-to-end — request/response DTOs, an API service, a repository, and a ViewModel call. Use when the user wants to fetch or post data over HTTP / call any REST endpoint from the app.
---

# Add an API service (network request)

Generic Ktor call to **any** URL — this is not tied to the project's own backend. Follow the layering:
DTOs → API service → repository (`Result` wrapping) → ViewModel.

Paths below are under `shared/src/commonMain/kotlin/com/kotlinfoundation/koko/`.

## 1. DTOs — `data/source/remote/request/` and `data/source/remote/response/`

`*Request` / `*Response` suffixes are required. All DTOs `@Serializable` + `@SerialName`. Response DTOs
carry an `asDomain()` mapper. **Return raw types, never `Result`** — repositories do the wrapping.

```kotlin
// response/GetJobsResponse.kt
@Serializable
data class GetJobsResponse(
    @SerialName("jobs") val jobs: List<JobResponse>,
) {
    fun asDomain(): List<Job> = jobs.map { it.asDomain() }
}

@Serializable
data class JobResponse(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
) {
    fun asDomain(): Job = Job(id = id, title = title)
}
```

## 2. API service — `data/source/remote/apiservices/`

Take the shared `HttpClient` (base URL + JSON + logging are already configured in
`data/source/remote/HttpClientFactory.kt`). Return raw DTOs; let exceptions propagate.

```kotlin
class JobApiService(private val httpClient: HttpClient) {
    suspend fun getJobs(request: GetJobsRequest): GetJobsResponse =
        httpClient.post("/jobs") { setBody(request) }.body()
}
```
Register in `root/Di.kt` `dataModule` alongside the existing `factoryOf(::ApiService)`:
```kotlin
factoryOf(::JobApiService)
```

## 3. Repository — `data/repository/`

Concrete class (no interface). Wrap in `Result` via `backgroundExecutor.execute { }` — it already
catches, logs, and returns `Result.failure`, so **no redundant try-catch**.

```kotlin
class JobRepository(
    private val jobApiService: JobApiService,
    private val backgroundExecutor: BackgroundExecutor = BackgroundExecutor.IO,
) {
    suspend fun getJobs(page: Int, limit: Int): Result<List<Job>> =
        backgroundExecutor.execute {
            val response = jobApiService.getJobs(GetJobsRequest(page, limit))
            Result.success(response.asDomain())
        }
}
```
Register in `dataModule`: `single { JobRepository(get()) }`.

## 4. Call from a ViewModel

Inject the repository, call the suspend function inside `viewModelScope`, fold the `Result` into UiState.

## Testing

Use Ktor `MockEngine` to stub responses/errors — never hit the real network in unit tests
(`shared/src/commonTest/`). See `AiGuidelines/tech/api_services.md`.

Validate with the `run-quality-gates` skill.
