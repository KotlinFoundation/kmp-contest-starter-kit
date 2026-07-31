# Utility Organization Guidelines

Purpose: Establish consistent patterns for organizing utility classes, extensions, and cross-cutting concerns in Compose Multiplatform projects.

## Location and Structure
- Base utilities: `composeApp/src/commonMain/kotlin/com/<org>/<app>/util/`
- Platform-specific utilities: `composeApp/src/androidMain/kotlin/.../util/` and `composeApp/src/iosMain/kotlin/.../util/`
- Organize by functional domain, not by technical type

## Package Organization

### Functional Domain Structure
Organize utilities by what they do, not what they are:

```
util/
├── analytics/          # Analytics and tracking utilities
├── extensions/         # Extension functions
├── file/              # File operations and management
├── inappreview/       # In-app review functionality
├── logging/           # Logging infrastructure
└── Platform.kt        # Platform-specific abstractions
```

### Cross-Cutting Concerns
Group related functionality together:

```kotlin
// util/logging/
├── Logger.kt           # Logger interface and AppLogger
├── LogLevel.kt         # Log level definitions
└── LogFormatter.kt     # Log formatting utilities

// util/analytics/
├── AnalyticsEvent.kt   # Event definitions
├── AnalyticsTracker.kt # Tracking interface
└── EventLogger.kt      # Analytics implementation

// util/extensions/
├── StringExtensions.kt
├── FlowExtensions.kt
└── ComposeExtensions.kt
```

## Utility Design Patterns

### Singleton Pattern for Stateful Utilities
Use object declarations for utilities that maintain state:

```kotlin
object AppLogger : Logger, KoinComponent {
    private val loggers = getKoin().getAll<Logger>()

    override fun d(message: String, throwable: Throwable?, tag: String?) {
        loggers.forEach { it.d(message, throwable, tag) }
    }
}

object AnalyticsManager {
    private val trackers = mutableListOf<AnalyticsTracker>()

    fun initialize(vararg trackers: AnalyticsTracker) {
        this.trackers.clear()
        this.trackers.addAll(trackers)
    }
}
```

### Extension Functions for Behavioral Extensions
Group extension functions by the type they extend:

```kotlin
// StringExtensions.kt
fun String.isValidEmail(): Boolean =
    android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.truncate(maxLength: Int): String =
    if (length <= maxLength) this else take(maxLength - 3) + "..."

// FlowExtensions.kt
fun <T> Flow<T>.throttleLatest(periodMillis: Long): Flow<T> =
    conflate().onEach { delay(periodMillis) }

// ComposeExtensions.kt
fun Modifier.fillWidthOfParent(percentage: Float = 1f): Modifier =
    this.fillMaxWidth(percentage)
```

### Utility Classes for Stateless Operations
Use regular classes for complex operations that don't need global state:

```kotlin
class DataValidator {
    fun validateEmail(email: String): ValidationResult {
        // Validation logic
    }

    fun validatePassword(password: String): ValidationResult {
        // Validation logic
    }
}

class FileUtils {
    suspend fun saveToCache(data: ByteArray, filename: String): Result<String> {
        // File operations
    }

    suspend fun readFromCache(filename: String): Result<ByteArray> {
        // File operations
    }
}
```

## Platform Abstraction

### Expect/Actual Pattern
Use expect/actual for platform-specific utilities:

```kotlin
// commonMain/util/Platform.kt
expect object Platform {
    val name: String
    val version: String
}

expect class FileManager {
    suspend fun writeFile(filename: String, content: ByteArray): Result<String>
    suspend fun readFile(filename: String): Result<ByteArray>
    fun deleteFile(filename: String): Boolean
}

// androidMain/util/Platform.kt
actual object Platform {
    actual val name: String = "Android"
    actual val version: String = Build.VERSION.RELEASE
}

actual class FileManager(private val context: Context) {
    actual suspend fun writeFile(filename: String, content: ByteArray): Result<String> {
        // Android implementation
    }
}
```

### Platform-Specific Utilities
Place platform-specific utilities in appropriate source sets:

```kotlin
// androidMain/util/AndroidUtils.kt
object AndroidUtils {
    fun getVersionCode(context: Context): Long {
        // Android-specific implementation
    }

    fun openAppSettings(context: Context) {
        // Android-specific implementation
    }
}

// iosMain/util/IOSUtils.kt
object IOSUtils {
    fun getVersionString(): String {
        // iOS-specific implementation
    }

    fun openAppSettings() {
        // iOS-specific implementation
    }
}
```

## Dependency Integration

### Injectable Utilities
Design utilities to work with dependency injection:

```kotlin
class BackgroundExecutor(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun <T> execute(block: suspend () -> Result<T>): Result<T> {
        return withContext(dispatcher) {
            try {
                block()
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

// DI Module
val utilModule = module {
    singleOf(::BackgroundExecutor)
    singleOf(::ApplicationScope)
    singleOf(::DataValidator)
}
```

### Self-Contained Utilities
Some utilities should be self-contained and not require DI:

```kotlin
object DateTimeUtils {
    fun formatRelativeTime(instant: Instant): String {
        // Pure function, no dependencies
    }

    fun isToday(instant: Instant): Boolean {
        // Pure function, no dependencies
    }
}

object CryptoUtils {
    fun generateHash(input: String): String {
        // Pure function, no dependencies
    }
}
```

## Error Handling in Utilities

### Consistent Error Patterns
Use consistent error handling across utilities:

```kotlin
class NetworkUtils {
    suspend fun checkConnection(): Result<Boolean> {
        return try {
            // Network check logic
            Result.success(true)
        } catch (e: Exception) {
            AppLogger.e("Network check failed", e)
            Result.failure(NetworkException("Connection check failed", e))
        }
    }
}

class FileUtils {
    suspend fun saveFile(filename: String, data: ByteArray): Result<String> {
        return try {
            // File save logic
            Result.success(savedPath)
        } catch (e: IOException) {
            AppLogger.e("File save failed", e)
            Result.failure(FileOperationException("Failed to save file", e))
        }
    }
}
```

### Graceful Degradation
Design utilities to handle failures gracefully:

```kotlin
object AnalyticsManager {
    fun trackEvent(event: AnalyticsEvent) {
        try {
            trackers.forEach { it.track(event) }
        } catch (e: Exception) {
            AppLogger.e("Analytics tracking failed", e)
            // Continue execution, don't crash the app
        }
    }
}
```

## Performance Considerations

### Lazy Initialization
Use lazy initialization for expensive utilities:

```kotlin
object CacheManager {
    private val cache by lazy {
        LruCache<String, Any>(maxSize = 100)
    }

    fun get(key: String): Any? = cache.get(key)
    fun put(key: String, value: Any) = cache.put(key, value)
}

class ImageLoader {
    private val httpClient by lazy {
        HttpClient {
            // Expensive initialization
        }
    }
}
```

### Memory Management
Consider memory impact of utility classes:

```kotlin
// Prefer stateless utilities
object StringUtils {
    fun formatCurrency(amount: Double): String {
        // No state, memory efficient
    }
}

// Be careful with caching utilities
class ResourceCache {
    private val cache = ConcurrentHashMap<String, ByteArray>()

    fun clearCache() {
        cache.clear() // Provide cleanup methods
    }
}
```

## Testing Utilities

### Testable Design
Design utilities to be easily testable:

```kotlin
class DataProcessor(
    private val validator: DataValidator = DataValidator(),
    private val logger: Logger = AppLogger
) {
    fun processData(input: String): ProcessResult {
        logger.d("Processing data: ${input.take(10)}...")
        if (!validator.isValid(input)) {
            return ProcessResult.Invalid
        }
        // Processing logic
    }
}

// Easy to test with mocks
@Test
fun testDataProcessor() {
    val mockValidator = mockk<DataValidator>()
    val mockLogger = mockk<Logger>()
    val processor = DataProcessor(mockValidator, mockLogger)
    // Test implementation
}
```

### Test Utilities
Create utilities specifically for testing:

```kotlin
// commonTest/util/TestUtils.kt
object TestUtils {
    fun createTestUser(id: String = "test"): User = User(
        id = id,
        email = "test@example.com",
        displayName = "Test User"
    )

    fun createTestAiInfluencer(id: Long = 1L): AiInfluencerModel =
        AiInfluencerModel(
            id = id,
            prompt = "Test prompt",
            createdAt = Clock.System.now(),
            updatedAt = Clock.System.now()
        )
}
```

## Documentation and Naming

### Clear Naming Conventions
Use descriptive names that indicate purpose:

```kotlin
// Good
object NetworkConnectivityChecker
class EmailValidator
fun String.toSlug(): String

// Avoid generic names
object Utils
class Helper
fun String.convert(): String
```

### Comprehensive Documentation
Document utility classes thoroughly:

```kotlin
/**
 * Manages application-wide logging with support for multiple log destinations.
 *
 * Automatically discovers all Logger implementations through Koin DI and
 * delegates log calls to all registered loggers.
 *
 * Usage:
 * ```kotlin
 * AppLogger.d("Debug message")
 * AppLogger.e("Error message", exception)
 * ```
 */
object AppLogger : Logger, KoinComponent {
    // Implementation
}
```

## Integration with Architecture

### Layer Independence
Keep utilities independent of specific architectural layers:

```kotlin
// Good - can be used by any layer
object DateTimeFormatter {
    fun formatTimestamp(instant: Instant): String
}

// Avoid - couples utility to specific layer
object RepositoryUtils {
    fun mapToUser(userEntity: UserEntity): User // Too coupled
}
```

### Cross-Layer Utilities
Design utilities that serve multiple layers appropriately:

```kotlin
// Serves both data and presentation layers
object ErrorMessageMapper {
    fun mapToUserMessage(exception: Throwable): String {
        return when (exception) {
            is NetworkException -> "Check your internet connection"
            is AuthException -> "Please sign in again"
            else -> "Something went wrong"
        }
    }
}
```
