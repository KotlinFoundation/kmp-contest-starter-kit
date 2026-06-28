# Presentation Layer Guidelines

Purpose: Establish consistent patterns for UI architecture, state management, and component design in Compose Multiplatform projects.

## Location and Structure
- All presentation code under `composeApp/src/commonMain/kotlin/com/<org>/<app>/presentation/`
- Organize by feature: `presentation/screens/<feature>/` (e.g., `home/`, `paywall/`, `onboarding/`) — each folder contains only `*Screen.kt`, `*UiState.kt`, `*ViewModel.kt`
- Shared components: `presentation/components/` with sub-packages for related components
- Navigation: `presentation/navigation/` holds `Routes.kt` (sealed `ScreenRoute` hierarchy), `Navigator`, `NavigationState`, and `AppNavigation` — feature folders do **not** contain a `*ScreenRoute.kt`

## Screen Architecture Pattern

### ViewModel Pattern
Use a ViewModel for complex screens with business logic:

```kotlin
class HomeViewModel(
    private val repository: Repository,
    private val applicationScope: ApplicationScope
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        repository.dataFlow,
        // other flows
    ) { data, ... ->
        HomeUiState(
            // map to UI state
        )
    }.stateIn(applicationScope, SharingStarted.Eagerly, HomeUiState.Loading)

    fun onUiEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.Action -> handleAction(event)
        }
    }
}
```

### Screen Composables
Follow function overloading pattern for flexibility:

```kotlin
// Main entry point with ViewModel
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel,
    onNavigate: (destination) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        modifier = modifier,
        uiState = uiState,
        onUiEvent = viewModel::onUiEvent,
        onNavigate = onNavigate
    )
}

// Pure composable for testing and previews
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onUiEvent: (HomeUiEvent) -> Unit,
    onNavigate: (destination) -> Unit
) {
    // UI implementation
}
```

## State Management

### UI State Classes
- Use immutable data classes for UI state
- Provide meaningful defaults for easy construction
- Group related state in nested data classes when appropriate

```kotlin
data class HomeUiState(
    val isLoading: Boolean = false,
    val items: List<ItemUiState> = emptyList(),
    val error: String? = null,
    val selectedFilter: FilterType = FilterType.All
) {
    val hasItems: Boolean get() = items.isNotEmpty()
    val isEmpty: Boolean get() = !isLoading && items.isEmpty()
}
```

### UI Events
Use sealed classes/interfaces for user interactions:

```kotlin
sealed interface HomeUiEvent {
    data object Refresh : HomeUiEvent
    data class ItemClicked(val id: String) : HomeUiEvent
    data class FilterChanged(val filter: FilterType) : HomeUiEvent
}
```

## Component Design

### Reusable Components
- Keep components small and focused on single responsibility
- Use `modifier: Modifier = Modifier` parameter for all custom composables
- Provide meaningful parameter names, avoid generic names like `onClick`

```kotlin
@Composable
fun VideoCardItem(
    title: String,
    thumbnailUrl: String?,
    onPlayClick: () -> Unit,
    onMoreOptionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Implementation
}
```

### Component Organization
- Group related components in dedicated packages:
  - `presentation/components/video/` - video-related components
  - `presentation/components/premium/` - premium/subscription components
  - `presentation/components/ads/` - advertisement components

### Design System Integration
- Use design system components from `designsystem` module when available
- Follow design system naming conventions (e.g., `AppButton`, `AppCard`)
- Keep presentation components generic and parameterized

## Screen Wrapper Patterns

### Common Screen Structure
Use consistent screen wrapper pattern:

```kotlin
@Composable
fun FeatureScreen(...) {
    ScreenWithToolbar(
        title = stringResource(Res.string.feature_title),
        onNavigationClick = onBackClick
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = paddingValues
        ) {
            // Screen content
        }
    }
}
```


### User Feedback
- Use proper loading indicators for async operations
- Show meaningful error messages with retry options
- Provide haptic feedback for important actions


## Performance Considerations

### State Management
- Use `remember` for expensive calculations
- Implement proper `key` parameters for LazyColumn/LazyRow items
- Avoid unnecessary recomposition with stable parameters


## Alignment with Product Docs
- Map UI states to user flows from `AiGuidelines/project/user_flow.md`
- Implement copy and content from `AiGuidelines/project/prd.md`
- Follow UX specifications from `AiGuidelines/project/ui_ux.md`
- Handle premium states according to `AiGuidelines/project/paywall.md`
