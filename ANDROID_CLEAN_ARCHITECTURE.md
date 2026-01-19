# Android Clean Architecture (Reference Only)

## ⚠️ IMPORTANT: This is NOT Our Architecture

**This document is for reference only.** We are **NOT using** Android's standard Clean Architecture (3-layer: UI/Domain/Data).

**👉 For our actual architecture, see [PRAGMATIC_SERVICES_ARCHITECTURE.md](./PRAGMATIC_SERVICES_ARCHITECTURE.md)**

---

## Overview

This document describes the standard Android Clean Architecture pattern for reference purposes. While this is the official Android recommendation, we've chosen a simpler services-based approach that better fits our use case of heavy edge device processing and real-time streaming.

This architecture follows official Android guidelines and emphasizes **separation of concerns**, **unidirectional data flow**, and **testability**.

## Architecture Layers

Android's recommended architecture consists of three primary layers:

```
┌─────────────────────────────────────────────────────────────┐
│  UI LAYER                                                   │
│  - Screens & Composables                                   │
│  - ViewModels (State Holders)                              │
│  - UI State Models                                         │
│  - Navigation                                              │
└───────────────────────┬─────────────────────────────────────┘
                        │ calls use cases
┌───────────────────────▼─────────────────────────────────────┐
│  DOMAIN LAYER                                               │
│  - Use Cases (Business Logic)                              │
│  - Domain Models (Entities)                                │
│  - Repository Interfaces (Contracts)                       │
└───────────────────────┬─────────────────────────────────────┘
                        │ implements interfaces
┌───────────────────────▼─────────────────────────────────────┐
│  DATA LAYER                                                 │
│  - Repository Implementations                              │
│  - Data Sources (Remote/Local)                            │
│  - Data Services (Parsers, Filters, etc.)                 │
│  - DTOs & Mappers                                         │
└─────────────────────────────────────────────────────────────┘
```

## Detailed Layer Breakdown

### 1. UI Layer

**Purpose**: Display data and capture user interactions.

**Responsibilities**:
- Render UI components (Jetpack Compose)
- Observe and display UI state from ViewModels
- Capture user events and pass them to ViewModels
- Handle navigation between screens
- **No business logic**

**Key Components**:

```kotlin
// ViewModel - State holder and UI logic coordinator
class AcquisitionViewModel @Inject constructor(
    private val processOcrStreamUseCase: ProcessOcrStreamUseCase,
    private val validateResultUseCase: ValidateAcquisitionResultUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AcquisitionUiState())
    val uiState: StateFlow<AcquisitionUiState> = _uiState.asStateFlow()
    
    fun startOcrProcessing() {
        viewModelScope.launch {
            processOcrStreamUseCase()
                .collect { result ->
                    _uiState.update { it.copy(acquisitionResult = result) }
                }
        }
    }
}

// UI State - Immutable state model
data class AcquisitionUiState(
    val acquisitionResult: AcquisitionResult? = null,
    val isProcessing: Boolean = false,
    val error: String? = null
)

// Screen - Pure UI rendering
@Composable
fun AcquisitionScreen(
    uiState: AcquisitionUiState,
    onEvent: (AcquisitionEvent) -> Unit
) {
    // Declarative UI rendering based on state
}
```

**Directory Structure**:
```
ui/
├── acquisition/
│   ├── AcquisitionRoute.kt          # Navigation entry point
│   ├── AcquisitionScreen.kt         # Composable UI
│   ├── AcquisitionViewModel.kt      # State holder
│   └── AcquisitionUiState.kt        # UI state model
├── components/                      # Reusable UI components
│   ├── BarcodeBottomSheet.kt
│   ├── CameraCornersOverlay.kt
│   └── ...
├── navigation/
│   └── AcquisitionNavigation.kt     # Navigation graph
└── mappers/
    └── AcquisitionResultToUiMapper.kt # Domain → UI mappers
```

---

### 2. Domain Layer

**Purpose**: Contains business logic and rules independent of any framework.

**Responsibilities**:
- Define business operations (use cases)
- Model business entities (domain models)
- Define contracts for data access (repository interfaces)
- **Pure Kotlin** - no Android dependencies

**Key Components**:

```kotlin
// Use Case - Single responsibility business operation
class ProcessOcrStreamUseCase @Inject constructor(
    private val ocrRepository: OcrRepository,
    private val acquisitionRepository: AcquisitionRepository
) {
    operator fun invoke(): Flow<AcquisitionResult> {
        return ocrRepository.getOcrStream()
            .flatMapConcat { ocrResult ->
                acquisitionRepository.processOcrResult(ocrResult)
            }
    }
}

// Repository Interface - Data access contract
interface AcquisitionRepository {
    fun processOcrResult(ocrResult: OcrResult): Flow<AcquisitionResult>
    suspend fun parseExpDate(candidate: AcquisitionCandidate): ParsedCandidate
    fun aggregateResults(vararg flows: Flow<AcquisitionResult>): Flow<AcquisitionResult>
}

// Domain Model - Business entity
data class AcquisitionResult(
    val rawOcr: String,
    val parsedExpDate: String? = null,
    val parsedFabDate: String? = null,
    val confidence: Double,
    // ... other properties
)
```

**Directory Structure**:
```
domain/
├── model/                           # Domain models (entities)
│   ├── AcquisitionCandidate.kt
│   ├── AcquisitionResult.kt
│   ├── ConfidenceResult.kt
│   └── ParsedCandidate.kt
├── usecase/                         # Use cases (business operations)
│   ├── ProcessOcrStreamUseCase.kt
│   ├── AggregateResultsUseCase.kt
│   ├── ParseExpDateUseCase.kt
│   ├── ParseFabDateUseCase.kt
│   ├── ParseBatchNumberUseCase.kt
│   ├── FilterAcquisitionCandidatesUseCase.kt
│   ├── CheckConfidenceUseCase.kt
│   └── ValidateAcquisitionResultUseCase.kt
├── repository/                      # Repository contracts
│   ├── AcquisitionRepository.kt
│   └── OcrRepository.kt
└── util/
    └── Result.kt                    # Result wrapper (Success/Error)
```

---

### 3. Data Layer

**Purpose**: Manage data operations and implement repository contracts.

**Responsibilities**:
- Implement repository interfaces from domain layer
- Manage data sources (camera, ML Kit, database)
- Handle data transformation (DTOs ↔ Domain models)
- Contain data services (parsers, filters, confidence checkers)
- Provide caching and data consistency

**Key Components**:

```kotlin
// Repository Implementation
class AcquisitionRepositoryImpl @Inject constructor(
    private val expDateParser: RegexExpDateParser,
    private val fabDateParser: RegexFabricationDateParser,
    private val filter: AcquisitionFilter,
    private val confidenceFlowConnector: ConfidenceFlowConnector,
    private val confidenceConfig: ConfidenceConfig
) : AcquisitionRepository {
    
    override fun processOcrResult(ocrResult: OcrResult): Flow<AcquisitionResult> {
        return flow {
            // Filter candidates
            val candidates = filter.filter(ocrResult)
            candidates.forEach { emit(it) }
        }.flatMapConcat { candidate ->
            // Parse based on type
            when (candidate.typeHint) {
                CandidateType.EXPIRY_DATE -> expDateParser.parse(candidate)
                else -> flowOf()
            }
        }.let { parsedFlow ->
            // Apply confidence checking
            confidenceFlowConnector.run {
                parsedFlow.toConfidenceResults(
                    type = CandidateType.EXPIRY_DATE,
                    params = confidenceConfig
                )
            }
        }
    }
}

// Data Source
class OcrRemoteDataSource @Inject constructor(
    private val rtCameraSdk: RtCameraSdk
) {
    fun startOcrStream(): Flow<OcrResultDto> = callbackFlow {
        rtCameraSdk.startCamera { result ->
            trySend(result)
        }
        awaitClose { rtCameraSdk.stopCamera() }
    }
}

// Mapper (DTO → Domain)
class OcrResultMapper {
    fun toDomain(dto: OcrResultDto): OcrResult {
        return OcrResult(
            text = dto.recognizedText,
            confidence = dto.mlConfidence,
            // ... map other fields
        )
    }
}
```

**Directory Structure**:
```
data/
├── repository/                      # Repository implementations
│   ├── AcquisitionRepositoryImpl.kt
│   └── OcrRepositoryImpl.kt
├── source/                          # Data sources
│   ├── local/
│   │   └── AcquisitionLocalDataSource.kt  # Future: Room database
│   └── remote/
│       └── OcrRemoteDataSource.kt   # Camera/ML Kit source
├── model/                           # DTOs (Data Transfer Objects)
│   ├── OcrResultDto.kt
│   └── CameraFrameDto.kt
├── mapper/                          # Data mappers (DTO ↔ Domain)
│   ├── OcrResultMapper.kt
│   └── DataToDomainMappers.kt
├── service/                         # Data services
│   ├── confidence/
│   │   ├── ConfidenceChecker.kt
│   │   ├── SlidingConfidenceChecker.kt
│   │   ├── StringDistance.kt
│   │   ├── ConfidenceFlowConnector.kt
│   │   └── ConfidenceConfig.kt
│   ├── parsing/
│   │   ├── AcquisitionParser.kt
│   │   ├── RegexExpDateParser.kt
│   │   ├── RegexFabricationDateParser.kt
│   │   └── RegexBatchNumberParser.kt
│   ├── filtering/
│   │   ├── AcquisitionFilter.kt
│   │   └── RegexAcquisitionFilter.kt
│   └── util/
│       ├── DateParsingUtils.kt
│       └── DatePatterns.kt
└── di/                              # Dependency Injection
    ├── DataModule.kt                # Repository & data source bindings
    ├── ServiceModule.kt             # Parser, filter, confidence bindings
    └── CameraModule.kt              # Camera SDK setup
```

---

## Data Flow Pattern

### Unidirectional Data Flow

```
User Action → ViewModel → Use Case → Repository → Data Source
                ↓            ↓            ↓            ↓
             Event        Domain       Domain       DTO
                          Logic       Model      

Data Source → Repository → Use Case → ViewModel → UI Update
     ↓             ↓           ↓          ↓           ↓
   DTO         Domain      Domain      UI State   Recompose
              Model       Model
```

### Example Flow: OCR Processing

```
1. User opens OCR screen
   └─► OcrScreen rendered

2. ViewModel starts OCR
   └─► startOcrProcessing() called

3. ViewModel invokes use case
   └─► ProcessOcrStreamUseCase.invoke()

4. Use case calls repository
   └─► acquisitionRepository.processOcrResult(ocrResult)

5. Repository orchestrates data services
   ├─► Filter candidates
   ├─► Parse dates
   └─► Check confidence

6. Results flow back up
   └─► Repository → Use Case → ViewModel → UI State

7. UI observes state and recomposes
   └─► Screen displays acquisition result
```

---

## Dependency Rules

### The Dependency Rule

> **Source code dependencies can only point inward.**

```
UI Layer ────────────────────► Domain Layer
                                      ▲
                                      │
Data Layer ────────────────────────────┘
```

- **UI Layer** depends on Domain Layer (use cases, models)
- **Data Layer** depends on Domain Layer (implements interfaces)
- **Domain Layer** depends on nothing (pure Kotlin)

### Dependency Inversion Principle

The **Domain Layer** defines interfaces (contracts). The **Data Layer** implements them.

```kotlin
// Domain Layer - defines contract
interface AcquisitionRepository {
    fun processOcrResult(ocrResult: OcrResult): Flow<AcquisitionResult>
}

// Data Layer - implements contract
class AcquisitionRepositoryImpl @Inject constructor(
    private val parser: AcquisitionParser,
    private val filter: AcquisitionFilter
) : AcquisitionRepository {
    override fun processOcrResult(ocrResult: OcrResult): Flow<AcquisitionResult> {
        // Implementation details
    }
}

// DI Module - wires implementation
@Module
@InstallIn(SingletonComponent::class)
interface DataModule {
    @Binds
    fun bindAcquisitionRepository(
        impl: AcquisitionRepositoryImpl
    ): AcquisitionRepository
}
```

---

## Key Principles

### 1. Separation of Concerns

Each layer has a **single, well-defined responsibility**:
- **UI**: Display data and handle user interactions
- **Domain**: Business logic and rules
- **Data**: Data operations and transformations

### 2. Single Source of Truth

The **Repository** is the single source of truth for data:
- UI never accesses data sources directly
- All data flows through the repository
- Enables caching, offline support, and consistency

### 3. Unidirectional Data Flow

Data flows in one direction (Data → UI), events flow up (UI → Domain → Data):
- Simplifies debugging
- Prevents state inconsistencies
- Makes data flow predictable

### 4. Testability

Each layer can be tested independently:
- **UI**: Test ViewModels with fake use cases
- **Domain**: Test use cases with fake repositories
- **Data**: Test repositories with fake data sources

```kotlin
// ViewModel test with fake use case
class AcquisitionViewModelTest {
    @Test
    fun `startOcrProcessing emits acquisition result`() = runTest {
        val fakeUseCase = FakeProcessOcrStreamUseCase()
        val viewModel = AcquisitionViewModel(fakeUseCase)
        
        viewModel.startOcrProcessing()
        
        val state = viewModel.uiState.first()
        assertThat(state.acquisitionResult).isNotNull()
    }
}

// Use case test with fake repository
class ProcessOcrStreamUseCaseTest {
    @Test
    fun `invoke processes OCR results through repository`() = runTest {
        val fakeRepository = FakeAcquisitionRepository()
        val useCase = ProcessOcrStreamUseCase(fakeRepository)
        
        val results = useCase().toList()
        
        assertThat(results).isNotEmpty()
    }
}
```

---

## Use Cases vs Application Services

### Use Cases (Android Recommended)

- **Single Responsibility**: Each use case does ONE thing
- **Reusable**: Can be composed in different ViewModels
- **Testable**: Easy to test in isolation
- **Discoverable**: Clear naming shows intent

```kotlin
// Good: Focused use cases
class ParseExpDateUseCase @Inject constructor(
    private val repository: AcquisitionRepository
) {
    suspend operator fun invoke(candidate: AcquisitionCandidate): Result<ParsedCandidate> {
        return repository.parseExpDate(candidate)
    }
}

class ValidateAcquisitionResultUseCase @Inject constructor() {
    operator fun invoke(result: AcquisitionResult): ValidationResult {
        // Validation logic
    }
}

// ViewModel composes multiple use cases
class AcquisitionViewModel @Inject constructor(
    private val parseExpDate: ParseExpDateUseCase,
    private val validateResult: ValidateAcquisitionResultUseCase
) : ViewModel()
```

### Application Services (Traditional)

- **Multiple Responsibilities**: Services can do many things
- **Heavier**: Often contain orchestration logic
- **Less Composable**: Harder to reuse parts

```kotlin
// Traditional: Heavier service class
class AcquisitionService @Inject constructor(
    private val repository: AcquisitionRepository,
    private val validator: Validator
) {
    fun processAcquisition(candidate: AcquisitionCandidate): Flow<AcquisitionResult> {
        // Multiple responsibilities in one service
    }
}
```

**Recommendation**: Use **Use Cases** for Android apps.

---

## Repository Pattern

### Why Use Repositories?

The **Repository pattern** abstracts data sources and provides a clean API for data access:

✅ **Centralized data access** - Single point for all data operations  
✅ **Abstraction** - UI doesn't know if data comes from network, database, or cache  
✅ **Testability** - Easy to mock for testing  
✅ **Caching** - Can implement caching strategies transparently  
✅ **Offline support** - Can fall back to local data when network unavailable

### Repository Implementation Pattern

```kotlin
// 1. Define interface in domain layer
interface AcquisitionRepository {
    fun getAcquisitionStream(): Flow<AcquisitionResult>
    suspend fun saveResult(result: AcquisitionResult)
}

// 2. Implement in data layer
class AcquisitionRepositoryImpl @Inject constructor(
    private val remoteDataSource: OcrRemoteDataSource,
    private val localDataSource: AcquisitionLocalDataSource,
    private val mapper: OcrResultMapper
) : AcquisitionRepository {
    
    override fun getAcquisitionStream(): Flow<AcquisitionResult> {
        return remoteDataSource.getOcrStream()
            .map { dto -> mapper.toDomain(dto) }
            .onEach { result -> 
                // Cache to local database
                localDataSource.saveResult(result)
            }
            .catch { error ->
                // Fall back to cached data on error
                emitAll(localDataSource.getCachedResults())
            }
    }
    
    override suspend fun saveResult(result: AcquisitionResult) {
        localDataSource.saveResult(result)
    }
}

// 3. Bind in DI module
@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {
    @Binds
    fun bindAcquisitionRepository(
        impl: AcquisitionRepositoryImpl
    ): AcquisitionRepository
}
```

---

## ViewModels as State Holders

### ViewModel Responsibilities

ViewModels in Android architecture are **state holders** that:
- Hold and manage UI state
- Survive configuration changes (screen rotation)
- Coordinate with use cases
- Expose state via `StateFlow` or `LiveData`
- Handle UI logic (not business logic)

```kotlin
class AcquisitionViewModel @Inject constructor(
    private val processOcrStream: ProcessOcrStreamUseCase,
    private val validateResult: ValidateAcquisitionResultUseCase
) : ViewModel() {
    
    // Private mutable state
    private val _uiState = MutableStateFlow(AcquisitionUiState())
    
    // Public read-only state
    val uiState: StateFlow<AcquisitionUiState> = _uiState.asStateFlow()
    
    // Handle user events
    fun onStartOcr() {
        _uiState.update { it.copy(isProcessing = true) }
        
        viewModelScope.launch {
            processOcrStream()
                .catch { error ->
                    _uiState.update { 
                        it.copy(
                            isProcessing = false,
                            error = error.message
                        )
                    }
                }
                .collect { result ->
                    val validation = validateResult(result)
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            acquisitionResult = result,
                            validationStatus = validation
                        )
                    }
                }
        }
    }
    
    fun onStopOcr() {
        _uiState.update { it.copy(isProcessing = false) }
    }
}
```

### State Management Patterns

```kotlin
// Sealed interface for UI state variants
sealed interface AcquisitionUiState {
    object Loading : AcquisitionUiState
    
    data class Success(
        val result: AcquisitionResult,
        val validation: ValidationStatus
    ) : AcquisitionUiState
    
    data class Error(
        val message: String
    ) : AcquisitionUiState
}

// Or data class with multiple properties
data class AcquisitionUiState(
    val isLoading: Boolean = false,
    val result: AcquisitionResult? = null,
    val error: String? = null,
    val validationStatus: ValidationStatus = ValidationStatus.PENDING
)
```

---

## Migration Guide

### Current Structure → Android Architecture

| Current Location | New Location | Layer | Notes |
|-----------------|--------------|-------|-------|
| `domain/AcquisitionPipeline.kt` | `domain/usecase/ProcessOcrStreamUseCase.kt` | Domain | Convert to use case |
| `domain/ResultAggregator.kt` | `domain/usecase/AggregateResultsUseCase.kt` | Domain | Convert to use case |
| `domain/Pipeline.kt` | `domain/repository/AcquisitionRepository.kt` | Domain | Repository interface |
| `data/AcquisitionCandidate.kt` | `domain/model/AcquisitionCandidate.kt` | Domain | Domain model |
| `data/AcquisitionResult.kt` | `domain/model/AcquisitionResult.kt` | Domain | Domain model |
| `data/ConfidenceCheckerResult.kt` | `domain/model/ConfidenceResult.kt` | Domain | Domain value object |
| `data/ConfidenceCheckerParams.kt` | `data/service/confidence/ConfidenceConfig.kt` | Data | Data layer config |
| `domain/consensus/*` | `data/service/confidence/*` | Data | Data services |
| `domain/parsers/*Parser.kt` | `data/service/parsing/*` | Data | Data services |
| `domain/parsers/ParsedCandidate.kt` | `domain/model/ParsedCandidate.kt` | Domain | Domain value object |
| `domain/filters/*` | `data/service/filtering/*` | Data | Data services |
| `di/AcquisitionModule.kt` | `data/di/DataModule.kt` | Data | DI configuration |
| `ui/screens/*/ViewModel.kt` | Keep in `ui/*/` | UI | ViewModels stay with screens |
| `ui/screens/*/UiState.kt` | `ui/*/` or `ui/models/` | UI | UI state models |
| `utils/*` | `data/service/util/*` | Data | Data utilities |

### Migration Steps

1. **Create new package structure** (start with empty folders)
2. **Move domain models first** (entities and value objects)
3. **Move repository interfaces** to `domain/repository/`
4. **Create use cases** from existing pipeline/aggregator logic
5. **Move data services** (parsers, filters, confidence) to data layer
6. **Create repository implementations** in data layer
7. **Update ViewModels** to use use cases instead of services
8. **Update DI modules** with new bindings
9. **Update imports** across the codebase
10. **Run tests** to ensure nothing broke
11. **Update documentation** to reflect new structure

---

## Benefits of Android Architecture

### For Development

✅ **Industry Standard**: Familiar to Android developers  
✅ **Official Support**: Documented by Google, integrated with Jetpack  
✅ **Clear Structure**: Easy to locate classes by responsibility  
✅ **Composability**: Use cases are easily combined in ViewModels  
✅ **Reactive**: Built for Kotlin Flow and coroutines  

### For Testing

✅ **Isolated Layers**: Test each layer independently  
✅ **Easy Mocking**: Repository interfaces are easy to fake  
✅ **Fast Tests**: Domain layer has no Android dependencies  
✅ **Behavior Testing**: Focus on what code does, not how  

### For Maintenance

✅ **Separation of Concerns**: Changes are localized to one layer  
✅ **Dependency Inversion**: Easy to swap implementations  
✅ **Scalability**: Add new features without touching existing code  
✅ **Onboarding**: New developers understand structure quickly  

---

## Common Patterns

### Loading States

```kotlin
sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val exception: Throwable) : UiState<Nothing>
}

// Usage in ViewModel
class MyViewModel @Inject constructor(
    private val useCase: MyUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<MyData>>(UiState.Loading)
    val state: StateFlow<UiState<MyData>> = _state.asStateFlow()
    
    init {
        viewModelScope.launch {
            _state.value = UiState.Loading
            useCase()
                .onSuccess { data -> _state.value = UiState.Success(data) }
                .onFailure { error -> _state.value = UiState.Error(error) }
        }
    }
}
```

### Result Wrapper

```kotlin
sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val exception: Throwable) : Result<Nothing>
}

// Extension functions
fun <T> Result<T>.getOrNull(): T? = when (this) {
    is Result.Success -> data
    is Result.Error -> null
}

suspend fun <T> Result<T>.onSuccess(action: suspend (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

suspend fun <T> Result<T>.onFailure(action: suspend (Throwable) -> Unit): Result<T> {
    if (this is Result.Error) action(exception)
    return this
}
```

### Mapping Between Layers

```kotlin
// Data → Domain
class OcrResultMapper {
    fun toDomain(dto: OcrResultDto): OcrResult {
        return OcrResult(
            text = dto.recognizedText,
            confidence = dto.mlConfidence,
            timestamp = dto.timestamp
        )
    }
}

// Domain → UI
fun AcquisitionResult.toUiModel(): AcquisitionUiModel {
    return AcquisitionUiModel(
        displayText = parsedExpDate ?: "No date found",
        confidenceLevel = when {
            confidence >= 0.8 -> ConfidenceLevel.HIGH
            confidence >= 0.5 -> ConfidenceLevel.MEDIUM
            else -> ConfidenceLevel.LOW
        },
        isValid = validationStatus == ValidationStatus.VALID
    )
}
```

---

## References

- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Guide to App Architecture](https://developer.android.com/topic/architecture/intro)
- [Data Layer](https://developer.android.com/topic/architecture/data-layer)
- [Domain Layer](https://developer.android.com/topic/architecture/domain-layer)
- [UI Layer](https://developer.android.com/topic/architecture/ui-layer)
- [Android Modularization](https://developer.android.com/topic/modularization)
- [Kotlin Flow](https://kotlinlang.org/docs/flow.html)
- [Dependency Injection with Hilt](https://developer.android.com/training/dependency-injection/hilt-android)

---

## Conclusion

The Android recommended layered architecture provides a solid foundation for building maintainable, testable, and scalable Android applications. By following these patterns and principles, you ensure that your codebase:

- **Aligns with industry standards** and Android best practices
- **Separates concerns** effectively across layers
- **Supports reactive programming** with Kotlin Flow
- **Enables comprehensive testing** at all layers
- **Scales gracefully** as features are added
- **Onboards new developers** quickly with familiar patterns

For the `feature-acquisition` module specifically, this architecture enables clean separation between OCR processing (data layer), business logic (domain layer), and user interface (UI layer), while maintaining excellent testability and maintainability.

