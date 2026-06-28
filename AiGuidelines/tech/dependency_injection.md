# Dependency Injection Guidelines

Purpose: Establish consistent dependency injection patterns using Koin for Compose Multiplatform projects.

## Framework Choice
- Use Koin for dependency injection across all platforms
- Leverage Koin's multiplatform support and Compose integration
- Prefer declarative module definitions over imperative configuration

## Location and Structure
- DI modules: `composeApp/src/commonMain/kotlin/com/<org>/<app>/di/`
- Platform-specific modules: `composeApp/src/androidMain/kotlin/.../di/` and `composeApp/src/iosMain/kotlin/.../di/`
- Module organization by layer: `DataModule.kt`, `DomainModule.kt`, `PresentationModule.kt`

## Module Organization

### Layer-Based Modules
Organize modules by architectural layers:

```kotlin
// DataModule.kt
val dataModule = module {
    // Repositories
    singleOf(::UserRepository)
    singleOf(::SubscriptionRepository)
    singleOf(::AiInfluencerRepository)

    // Data sources
    singleOf(::UserPreferences)
    singleOf(::AuthService)

    // API services
    singleOf(::JobApiService)
    singleOf(::MediaApiService)

    // Database
    singleOf(::DatabaseProvider)
}

// PresentationModule.kt
val presentationModule = module {
    // ViewModels with factory scope for screen lifecycle
    factoryOf(::HomeViewModel)
    factoryOf(::PaywallViewModel)
    factoryOf(::OnboardingViewModel)
}

// UtilModule.kt
val utilModule = module {
    // Utilities and cross-cutting concerns
    singleOf(::ApplicationScope)
    singleOf(::BackgroundExecutor)

    // Multiple logger implementations
    singleOf(::ConsoleLogger) bind Logger::class
    singleOf(::FirebaseLogger) bind Logger::class
    // AppLogger uses getAll<Logger>() to get all implementations
}
```

### Platform-Specific Modules
Handle platform differences with separate modules:

```kotlin
// androidMain/di/PlatformModule.kt
val androidModule = module {
    singleOf(::AndroidLogger) bind Logger::class
    singleOf(::AndroidFileManager) bind FileManager::class
    singleOf(::GoogleAuthProvider) bind AuthProvider::class
}

// iosMain/di/PlatformModule.kt
val iosModule = module {
    singleOf(::IOSLogger) bind Logger::class
    singleOf(::IOSFileManager) bind FileManager::class
    singleOf(::AppleAuthProvider) bind AuthProvider::class
}
```

## Scoping Strategies

### Singleton vs Factory
Choose appropriate scoping based on usage:

```kotlin
// Singleton for stateful services and repositories
singleOf(::UserRepository)
singleOf(::NetworkClient)
singleOf(::DatabaseInstance)

// Factory for stateless services and UI components
factoryOf(::DataValidator)
factoryOf(::HomeViewModel)
factoryOf(::PaymentProcessor)
```

### Scope Guidelines
- **Single**: Repositories, network clients, databases, application-wide services
- **Factory**: ViewModels, validators, processors, screen-scoped services
- **Scoped**: Use sparingly, only for specific lifecycle requirements

## Interface Binding

### Multiple Implementations
Use interface binding for polymorphic dependencies:

```kotlin
// Multiple logger implementations
singleOf(::ConsoleLogger) bind Logger::class
singleOf(::FileLogger) bind Logger::class
singleOf(::RemoteLogger) bind Logger::class

// Consumer uses getAll() to get all implementations
object AppLogger : Logger, KoinComponent {
    private val loggers = getKoin().getAll<Logger>()
    // Implementation delegates to all loggers
}
```

### Single Implementation Binding
Only bind interfaces when truly needed:

```kotlin
// Only if you need to swap implementations
singleOf(::ProductionAuthService) bind AuthService::class

// Prefer direct injection for single implementations
singleOf(::UserRepository) // No interface binding needed
```

## Initialization Patterns

### Application Initialization
Initialize Koin at application startup:

```kotlin
// Module definitions live in root/Di.kt; AppInitializer loads them at startup.
fun initializeKoin() {
    startKoin {
        modules(
            dataModule,
            presentationModule,
            utilModule,
            platformModule
        )
    }
}
```


## Integration Patterns



### Repository Dependencies
Inject dependencies into repositories properly:

```kotlin
class UserRepository(
    private val authService: AuthService,
    private val userPreferences: UserPreferences,
    private val subscriptionRepository: SubscriptionRepository,
    private val backgroundExecutor: BackgroundExecutor = BackgroundExecutor.IO,
    private val applicationScope: ApplicationScope
) {
    // Implementation
}

// Module definition
val dataModule = module {
    singleOf(::UserRepository)
    // Dependencies will be automatically resolved
}
```

## Testing with Koin

### Test Module Override
Override dependencies for testing:

```kotlin
@BeforeTest
fun setupKoin() {
    startKoin {
        modules(
            testModule // Override production modules
        )
    }
}

val testModule = module {
    singleOf(::MockUserRepository) bind UserRepository::class
    singleOf(::MockAuthService) bind AuthService::class
    singleOf(::TestApplicationScope) bind ApplicationScope::class
}
```

### Koin Test Extensions
Use Koin test utilities:

```kotlin
class UserRepositoryTest : KoinTest {
    private val userRepository: UserRepository by inject()
    private val mockAuthService: MockAuthService by inject()

    @Test
    fun testUserFlow() {
        mockAuthService.mockUser = testUser
        val result = userRepository.getCurrentUser()
        // Assertions
    }
}
```

## Platform-Specific Dependencies

### Expect/Actual Pattern
Combine with expect/actual for platform abstraction:

```kotlin
// commonMain
expect class PlatformService

// androidMain
actual class PlatformService {
    // Android implementation
}

// iosMain
actual class PlatformService {
    // iOS implementation
}

// Module
val platformModule = module {
    singleOf(::PlatformService)
}
```

## Best Practices

### Module Organization
- Keep modules focused and cohesive
- Separate platform-specific dependencies
- Use clear naming conventions for modules

### Dependency Declaration
- Prefer constructor injection over property injection
- Use the minimum required scope (factory over singleton when possible)
- Avoid complex object graphs that are hard to test

### Documentation
- Document complex dependency relationships
- Explain unusual scoping decisions
- Provide examples for platform-specific usage

## Alignment with Architecture
- Repositories and services use singleton scope for state consistency
- ViewModels use factory scope for proper lifecycle management
- Platform abstractions follow expect/actual patterns
- Cross-cutting concerns (logging, analytics) use multiple binding patterns
