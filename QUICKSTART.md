# 🚀 Quick Start Guide - Synapse Multi-Module Project

## ✅ Build Verification

The project has been successfully migrated to a multi-module architecture and builds successfully!

```bash
BUILD SUCCESSFUL in 7s
132 actionable tasks: 16 executed, 116 up-to-date
```

## 📦 Modules Created (6 modules)

| Module | Purpose | Key Files |
|--------|---------|-----------|
| `:core:core-common` | Shared constants | `Constants.kt` |
| `:core:core-network` | Network setup | `NetworkModule.kt` |
| `:data:data-triggers` | Database layer | `TriggerEntity.kt`, `TriggerDao.kt`, `TriggerDatabase.kt`, `TriggersRepository.kt`, `Converters.kt` |
| `:feature:feature-triggers` | UI & ViewModel | `TriggerListScreen.kt`, `AddEditTriggerScreen.kt`, `TriggersViewModel.kt` |
| `:service:service-sms` | SMS handling | `SmsListenerService.kt`, `SmsReceiver.kt`, `BootCompletedReceiver.kt` |
| `:app` | Entry point | `MainActivity.kt`, `SynapseApp.kt` |

## 🔨 Build Commands

```bash
# Clean the project
./gradlew clean

# Build all modules
./gradlew build

# Build debug APK
./gradlew :app:assembleDebug

# Install on device
./gradlew :app:installDebug

# List all modules
./gradlew projects
```

## 📂 File Structure

```
synapse/
├── app/
│   └── src/main/java/com/aserdipanda/synapse/
│       ├── MainActivity.kt
│       └── SynapseApp.kt
│
├── core/
│   ├── core-common/
│   │   └── src/main/java/.../core/common/
│   │       └── Constants.kt
│   └── core-network/
│       └── src/main/java/.../core/network/
│           └── NetworkModule.kt
│
├── data/
│   └── data-triggers/
│       └── src/main/java/.../data/triggers/
│           ├── local/
│           │   ├── TriggerDao.kt
│           │   ├── TriggerDatabase.kt
│           │   ├── TriggerEntity.kt
│           │   └── Converters.kt
│           └── TriggersRepository.kt
│
├── feature/
│   └── feature-triggers/
│       └── src/main/java/.../feature/triggers/
│           ├── ui/
│           │   ├── TriggerListScreen.kt
│           │   └── AddEditTriggerScreen.kt
│           └── TriggersViewModel.kt
│
└── service/
    └── service-sms/
        └── src/main/java/.../service/sms/
            ├── SmsListenerService.kt
            ├── SmsReceiver.kt
            └── BootCompletedReceiver.kt
```

## 🔧 Key Configuration Files

- **`settings.gradle.kts`** - Registers all 6 modules
- **`gradle/libs.versions.toml`** - Version catalog with android-library plugin
- **`build.gradle.kts`** (root) - Applies plugins to all modules
- **`app/AndroidManifest.xml`** - Registers SynapseApp, services, and receivers

## 🎯 What's Working

✅ Multi-module structure created  
✅ Room database configured  
✅ Repository pattern implemented  
✅ Jetpack Compose UI screens  
✅ ViewModel with StateFlow  
✅ SMS service modularized  
✅ Network layer centralized  
✅ Constants extracted to core  
✅ **Build successful**  

## ⚠️ Minor Warnings (Non-blocking)

The build shows deprecation warnings for `LocalBroadcastManager`:
- This is expected - LocalBroadcastManager is deprecated
- Doesn't affect functionality
- Can be replaced with LiveData/Flow in future updates

## 🎨 Using the Trigger Feature

### 1. Access Repository in your code:

```kotlin
val app = application as SynapseApp
val repository = app.triggersRepository
```

### 2. Create ViewModel:

```kotlin
val viewModel = TriggersViewModel(repository)
```

### 3. Use the Compose screens:

```kotlin
// In your MainActivity or Navigation
TriggerListScreen(
    triggers = viewModel.triggers.collectAsState().value,
    isLoading = viewModel.isLoading.collectAsState().value,
    onAddTrigger = { /* Navigate to add screen */ },
    onEditTrigger = { trigger -> /* Navigate to edit */ },
    onDeleteTrigger = { trigger -> viewModel.deleteTrigger(trigger) },
    onToggleTrigger = { id, isActive -> viewModel.toggleTriggerStatus(id, isActive) }
)
```

## 📚 Documentation

- **`ARCHITECTURE.md`** - Detailed architecture documentation
- **`MIGRATION_SUMMARY.md`** - Complete migration summary
- **`QUICKSTART.md`** (this file) - Quick reference guide

## 🚧 Next Steps

1. **Add Hilt/Koin** for dependency injection
   - Remove manual repository creation
   - Use @Inject for ViewModels

2. **Implement Navigation**
   ```kotlin
   implementation("androidx.navigation:navigation-compose:2.7.7")
   ```

3. **Connect UI to MainActivity**
   - Replace current screen with TriggerListScreen
   - Add navigation graph

4. **Add Unit Tests**
   - Test repository logic
   - Test ViewModel state changes
   - Test UI components

5. **Enhance SMS Receiver**
   - Use triggers from database
   - Match patterns dynamically

## 🐛 Troubleshooting

### Gradle Sync Issues
```bash
# Stop all Gradle daemons
./gradlew --stop

# Clean and rebuild
./gradlew clean build
```

### Module Not Found
- Ensure module is listed in `settings.gradle.kts`
- Check `include(":module:name")` syntax
- Sync Gradle files

### Build Errors
- Check KSP version matches Kotlin version (2.0.21-1.0.28)
- Verify all dependencies are compatible
- Clear caches: `./gradlew clean`

## 📱 Running the App

1. **Connect Android device** or start emulator
2. **Run**:
   ```bash
   ./gradlew :app:installDebug
   ```
3. **Grant permissions**:
   - RECEIVE_SMS
   - POST_NOTIFICATIONS (Android 13+)
4. **Start SMS Listener** using the button in MainActivity

## 💡 Tips

- **Module navigation**: Use `Cmd + Click` (Mac) on module imports
- **Gradle tasks**: Run `./gradlew tasks` to see all available tasks
- **Sync Gradle**: After changes to build files, sync in Android Studio
- **Clean builds**: Use `./gradlew clean` when facing build issues

## 🎉 Success Indicators

✅ `./gradlew projects` shows all 6 modules  
✅ `./gradlew :app:assembleDebug` builds successfully  
✅ APK generated in `app/build/outputs/apk/debug/`  
✅ All Kotlin files compile without errors  

---

**Your multi-module architecture is ready to use!** 🚀

For detailed information, see `ARCHITECTURE.md` and `MIGRATION_SUMMARY.md`.
