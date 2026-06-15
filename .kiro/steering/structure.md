# Project Structure

## Root Directory Layout
```
fs-mobile-app/
├── app/                          # Main application module
├── .gradle/                      # Gradle build cache
├── .idea/                        # Android Studio configuration
├── .kiro/                        # Kiro AI assistant configuration
├── build.gradle                  # Root build configuration
└── *.md                          # Documentation files
```

## Application Module Structure (`app/src/main/`)

### Core Package Organization (`com.dfd.delfin`)
```
com.dfd.delfin/
├── KotlinApp.kt                  # Application class with Dagger setup
├── api/                          # Network layer
│   ├── repository/               # Repository pattern implementations
│   ├── request/                  # API request models
│   ├── response/                 # API response models
│   └── service/                  # Retrofit service interfaces
├── injection/                    # Dagger dependency injection
│   ├── component/                # Dagger components
│   └── module/                   # Dagger modules
├── ui/                           # UI layer (Activities, Fragments, ViewModels)
│   ├── home/                     # Home screen and main features
│   ├── auth/                     # Authentication flows
│   ├── bids/                     # Bid management
│   ├── contracts/                # Contract management
│   ├── trips/                    # Trip tracking
│   ├── profile/                  # User profile
│   ├── kyc/                      # KYC verification flows
│   ├── trucks/                   # Fleet management
│   ├── wallet/                   # Wallet and payments
│   └── [feature]/                # Feature-specific UI modules
├── utils/                        # Utility classes
│   ├── prefs/                    # SharedPreferences wrappers
│   └── [utility]/                # Helper utilities
├── fcm/                          # Firebase Cloud Messaging
├── SyncOfferData/                # Background sync workers
└── [domain]/                     # Domain-specific packages
```

## Architecture Patterns

### MVVM Structure
Each feature typically follows this structure:
```
feature/
├── FeatureActivity.kt            # Activity with data binding
├── FeatureFragment.kt            # Fragment with data binding
├── FeatureViewModel.kt           # ViewModel with business logic
├── adapter/                      # RecyclerView adapters
│   └── FeatureAdapter.kt
└── model/                        # Feature-specific models
    └── FeatureModel.kt
```

### Repository Pattern
```
api/repository/
├── BaseRepository.kt             # Base repository with common logic
├── AuthenticationRepository.kt   # Auth-related API calls
├── BidsRepository.kt             # Bid management APIs
├── TransactionsRepository.kt     # Transaction/load APIs
├── TruckRepository.kt            # Fleet management APIs
└── [Domain]Repository.kt         # Domain-specific repositories
```

### Service Layer
```
api/service/
├── AuthenticationService.kt      # Auth endpoints
├── BidsService.kt                # Bid endpoints
├── TransactionService.kt         # Transaction endpoints
├── SpotBiddingService.kt         # Marketplace endpoints
└── [Domain]Service.kt            # Domain-specific services
```

## Key Directories

### Resources (`app/src/main/res/`)
```
res/
├── layout/                       # XML layouts
├── drawable/                     # Images and vector drawables
├── mipmap/                       # App icons
├── values/                       # Strings, colors, styles, dimensions
├── xml/                          # XML configurations (file paths, etc.)
└── [resource-type]/              # Other resource types
```

### Assets & Configuration
```
app/
├── google-services.json          # Firebase configuration
├── proguard-rules.pro            # ProGuard rules
└── keystore/                     # Release signing keystore
```

## Naming Conventions

### Package Naming
- Use lowercase with no underscores
- Group by feature or layer (ui, api, utils)
- Feature packages contain all related components

### File Naming
- **Activities**: `[Feature]Activity.kt` (e.g., `HomeActivity.kt`)
- **Fragments**: `[Feature]Fragment.kt` (e.g., `HomeLoadsFragment.kt`)
- **ViewModels**: `[Feature]ViewModel.kt` (e.g., `HomeLoadsViewModel.kt`)
- **Repositories**: `[Domain]Repository.kt` (e.g., `BidsRepository.kt`)
- **Services**: `[Domain]Service.kt` (e.g., `TransactionService.kt`)
- **Adapters**: `[Feature]Adapter.kt` or `[Feature]VH.kt` for ViewHolders
- **Models**: `[Entity]Model.kt` or `[Entity]Request/Response.kt`
- **Layouts**: `activity_[feature].xml`, `fragment_[feature].xml`, `item_[type].xml`

### Class Naming
- Activities: PascalCase ending with `Activity`
- Fragments: PascalCase ending with `Fragment`
- ViewModels: PascalCase ending with `ViewModel`
- Repositories: PascalCase ending with `Repository`
- Adapters: PascalCase ending with `Adapter` or `VH` (ViewHolder)

## Module Dependencies

### Core Dependencies Flow
```
UI Layer (Activities/Fragments)
    ↓ (observes)
ViewModel Layer
    ↓ (calls)
Repository Layer
    ↓ (uses)
Service Layer (Retrofit)
    ↓ (makes)
Network Calls
```

### Dependency Injection Flow
```
KotlinApp
    ↓ (creates)
DaggerAppComponent
    ↓ (provides)
Modules (NetworkModule, ViewModelModule, etc.)
    ↓ (injects into)
Activities/Fragments/ViewModels
```

## Configuration Files

### Build Configuration
- `build.gradle` (root): Project-level dependencies and repositories
- `app/build.gradle`: App module configuration, dependencies, flavors
- `gradle.properties`: Gradle configuration properties
- `settings.gradle`: Project settings and module inclusion

### Android Configuration
- `AndroidManifest.xml`: App permissions, activities, services
- `proguard-rules.pro`: Code obfuscation rules
- `google-services.json`: Firebase configuration

## Testing Structure
```
app/src/
├── androidTest/                  # Instrumented tests
│   └── java/com/example/harish/baseproject/
│       └── ExampleInstrumentedTest.java
└── test/                         # Unit tests (if present)
```

## Documentation Files
- `API_DOCUMENTATION.md`: Comprehensive API endpoint documentation
- `TESTING_GUIDE.md`: Testing procedures and guidelines
- `*_README.md`: Feature-specific documentation
- `*_DOCUMENTATION.md`: Technical documentation for specific features

## Important Notes

### Code Organization
- Features are organized by domain (bids, trips, contracts, etc.)
- Each feature contains its own Activity, Fragment, ViewModel, and adapters
- Shared utilities and base classes are in the `utils` package
- Network layer is centralized in the `api` package

### Data Flow
- UI observes LiveData from ViewModels
- ViewModels call Repository methods
- Repositories use Service interfaces (Retrofit)
- RxJava handles asynchronous operations
- Room database for local persistence

### Dependency Injection
- Dagger 2 manages all dependencies
- Components defined in `injection/component/`
- Modules defined in `injection/module/`
- Activities and Fragments use `@Inject` for dependencies
