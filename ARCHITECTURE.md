# Synapse - Multi-Module Android Architecture

## 📁 Project Structure

```
synapse/
├── app/                          # Application Module (Entry Point)
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   └── java/com/aserdipanda/synapse/
│   │       ├── MainActivity.kt
│   │       └── SynapseApp.kt
│   └── build.gradle.kts
│
├── core/                         # Core Modules
│   ├── core-common/              # Common utilities and constants
│   │   └── src/main/java/com/aserdipanda/synapse/core/common/
│   │       └── Constants.kt
│   │
│   ├── core-network/             # Network configuration
│   │   └── src/main/java/com/aserdipanda/synapse/core/network/
│   │       └── NetworkModule.kt
│   │
│   └── core-ui/                  # Theme and shared Compose components
│       └── src/main/java/com/aserdipanda/core/ui/
│           ├── theme/            # Colors, type and SynapseAppTheme
│           └── components/       # SynapseLogo, RuleSentence
│
├── data/                         # Data Modules
│   └── data-triggers/            # Triggers data layer
│       └── src/main/java/com/aserdipanda/synapse/data/triggers/
│           ├── local/
│           │   ├── TriggerDao.kt
│           │   ├── TriggerDatabase.kt
│           │   ├── TriggerEntity.kt
│           │   └── Converters.kt
│           └── TriggersRepository.kt
│
├── feature/                      # Feature Modules
│   ├── feature-onboarding/       # Welcome and permission screens
│   │   └── src/main/java/com/aserdipanda/synapse/feature/onboarding/
│   │       ├── ui/
│   │       │   ├── WelcomeScreen.kt
│   │       │   ├── PermissionsScreen.kt
│   │       │   ├── SmsDeniedScreen.kt
│   │       │   └── SmsBlockedScreen.kt
│   │       ├── OnboardingViewModel.kt
│   │       └── SmsAccess.kt
│   │
│   └── feature-triggers/         # Triggers UI and business logic
│       └── src/main/java/com/aserdipanda/synapse/feature/triggers/
│           ├── ui/
│           │   ├── TriggerListScreen.kt
│           │   └── AddEditTriggerScreen.kt
│           └── TriggersViewModel.kt
│
└── service/                      # Service Modules
    └── service-sms/              # SMS handling services
        └── src/main/java/com/aserdipanda/synapse/service/sms/
            ├── SmsListenerService.kt
            ├── SmsReceiver.kt
            └── BootCompletedReceiver.kt
```

## 🏗️ Module Architecture

### `:app` - Application Module
**Purpose**: Main entry point that assembles all modules together.

**Responsibilities**:
- App initialization via `SynapseApp.kt`
- Main navigation in `MainActivity.kt`
- Theme configuration

**Dependencies**: All feature, service, data, and core modules

---

### `:core:core-common` - Common Core Module
**Purpose**: Shared utilities, constants, and base classes.

**Contains**:
- `Constants.kt` - App-wide constants
- Utility functions
- Base classes

**Dependencies**: None (leaf module)

---

### `:core:core-network` - Network Core Module
**Purpose**: Network client configuration and setup.

**Contains**:
- `NetworkModule.kt` - OkHttp client provider
- Network interceptors (if needed)
- API configuration

**Dependencies**: `core-common`

---

### `:core:core-ui` - UI Core Module
**Purpose**: The Synapse theme, and the Compose components that features share.

**Contains**:
- `theme/` - Color schemes, type scale and `SynapseAppTheme`
- `components/` - `SynapseLogo`, and `RuleSentence` for reading a trigger's rules as a sentence

**Dependencies**: None (leaf module)

---

### `:data:data-triggers` - Triggers Data Module
**Purpose**: Single source of truth for trigger-related data.

**Contains**:
- Room database (`TriggerDatabase`)
- DAOs (`TriggerDao`)
- Entities (`TriggerEntity`)
- Repository (`TriggersRepository`)
- Type converters (`Converters`)

**Dependencies**: `core-common`

**Technologies**:
- Room Database
- Kotlin Coroutines & Flow
- Type Converters for List<String>

---

### `:feature:feature-onboarding` - Onboarding Feature Module
**Purpose**: First launch and SMS access: the welcome screen and the permission screens.

**Contains**:
- `WelcomeScreen.kt` - What Synapse does, shown on first launch only
- `PermissionsScreen.kt` - Explains SMS and notification access before Android asks
- `SmsDeniedScreen.kt` - Asks again after one denial
- `SmsBlockedScreen.kt` - Leads to the app's settings once Android won't ask again
- `OnboardingViewModel.kt` - Saved onboarding and permission state

**Dependencies**:
- `core-ui`
- `data-triggers`

---

### `:feature:feature-triggers` - Triggers Feature Module
**Purpose**: Self-contained user experience for triggers management.

**Contains**:
- Jetpack Compose screens
  - `TriggerListScreen.kt` - List of triggers
  - `AddEditTriggerScreen.kt` - Add/Edit trigger form
- `TriggersViewModel.kt` - UI state management

**Dependencies**: 
- `core-common`
- `data-triggers`

**Technologies**:
- Jetpack Compose
- Material3
- ViewModel & StateFlow

---

### `:service:service-sms` - SMS Service Module
**Purpose**: Background SMS processing and listening.

**Contains**:
- `SmsListenerService.kt` - Foreground service for SMS listening
- `SmsReceiver.kt` - Broadcast receiver for SMS
- `BootCompletedReceiver.kt` - Auto-start on device boot

**Dependencies**: 
- `core-common`
- `core-network`
- `data-triggers`

**Technologies**:
- Android Services
- Broadcast Receivers
- OkHttp for API calls

---

## 🔄 Dependency Graph

```
┌─────────────────┐
│      :app       │ (Application Module)
└────────┬────────┘
         │
         ├──────────────────┬──────────────────┬──────────────────┐
         │                  │                  │                  │
    ┌────▼─────┐      ┌────▼────────┐   ┌────▼────────┐   ┌────▼────────┐
    │ :feature │      │  :service   │   │   :data     │   │   :core     │
    │ -triggers│      │  -sms       │   │  -triggers  │   │  -common    │
    └────┬─────┘      └──────┬──────┘   └──────┬──────┘   │  -network   │
         │                   │                  │          └─────────────┘
         └───────────────────┴──────────────────┘
                             │
                    ┌────────┴────────┐
                    │  :core-common   │
                    │  :core-network  │
                    └─────────────────┘
```

## 🚀 Benefits of This Architecture

### 1. **Separation of Concerns**
- Each module has a single, well-defined responsibility
- Easy to locate and modify specific functionality

### 2. **Scalability**
- Add new features without affecting existing code
- Modules can be developed independently

### 3. **Reusability**
- Core modules can be shared across features
- Service modules can be reused in multiple places

### 4. **Testability**
- Each module can be tested in isolation
- Mock dependencies easily

### 5. **Build Performance**
- Gradle can build modules in parallel
- Changes in one module don't require rebuilding everything

### 6. **Team Collaboration**
- Different teams can work on different modules
- Reduces merge conflicts

## 📝 Module Guidelines

### Creating a New Feature Module

1. **Create module structure**:
   ```
   feature/feature-<name>/
   ├── build.gradle.kts
   └── src/main/
       ├── AndroidManifest.xml
       └── java/com/aserdipanda/synapse/feature/<name>/
           ├── ui/
           └── ViewModel.kt
   ```

2. **Add dependencies**:
   ```kotlin
   dependencies {
       implementation(project(":core:core-common"))
       implementation(project(":data:data-<name>"))
       // Compose & ViewModel libraries
   }
   ```

3. **Register in `settings.gradle.kts`**:
   ```kotlin
   include(":feature:feature-<name>")
   ```

### Creating a New Data Module

1. **Follow clean architecture principles**
2. **Expose repository only** - hide DAOs and Entities
3. **Use Kotlin Flow** for reactive data streams
4. **Include Room for local storage**

### Creating a New Service Module

1. **Keep services focused** on one responsibility
2. **Use dependency injection** for better testability
3. **Leverage core modules** for shared functionality

## 🛠️ Technologies Used

- **Language**: Kotlin
- **Build System**: Gradle (Kotlin DSL)
- **UI Framework**: Jetpack Compose + Material3
- **Database**: Room
- **Async**: Kotlin Coroutines + Flow
- **Network**: OkHttp
- **Architecture**: Multi-module + MVVM

## 📦 Building the Project

```bash
# Clean build
./gradlew clean

# Build all modules
./gradlew build

# Build specific module
./gradlew :app:build
./gradlew :feature:feature-triggers:build

# Run the app
./gradlew :app:installDebug
```

## 🎯 Next Steps

1. **Add Hilt/Koin** for dependency injection
2. **Implement navigation** with Jetpack Navigation Compose
3. **Add unit tests** for each module
4. **Set up CI/CD** pipeline
5. **Add more features** as separate modules

## 📚 References

- [Android Multi-Module Architecture](https://developer.android.com/topic/modularization)
- [Guide to app architecture](https://developer.android.com/topic/architecture)
- [Now in Android App](https://github.com/android/nowinandroid) - Google's multi-module sample

---

**Last Updated**: November 2026  
**Version**: 1.0.0
