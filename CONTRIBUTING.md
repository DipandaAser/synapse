# Contributing to Synapse

Thank you for your interest in contributing to Synapse!

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Project Structure](#project-structure)
- [Making Changes](#making-changes)
- [Pull Request Process](#pull-request-process)
- [Coding Guidelines](#coding-guidelines)
- [Testing](#testing)
- [Module Guidelines](#module-guidelines)

## Code of Conduct

Please be respectful and constructive in all interactions. We're here to build something great together!

## Getting Started

1. **Fork the repository** to your GitHub account
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/synapse.git
   cd synapse
   ```
3. **Add upstream remote**:
   ```bash
   git remote add upstream https://github.com/DipandaAser/synapse.git
   ```

## Development Setup

### Prerequisites

- Android Studio (latest stable version)
- JDK 11 or higher
- Android SDK (API 24+)
- Git

### Initial Setup

1. Open the project in Android Studio
2. Sync Gradle files
3. Build the project:
   ```bash
   ./gradlew build
   ```
4. Run on device/emulator:
   ```bash
   ./gradlew :app:installDebug
   ```

## Project Structure

Synapse follows a **multi-module architecture**:

```
synapse/
├── app/                    # Application entry point
├── core/
│   ├── core-common/        # Shared utilities & constants
│   └── core-network/       # Network configuration
├── data/
│   └── data-triggers/      # Data layer (Room DB)
├── feature/
│   └── feature-triggers/   # UI & ViewModels
└── service/
    └── service-sms/        # Background services
```

**See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed documentation.**

## Making Changes

### 1. Create a Feature Branch

```bash
git checkout -b feature/your-feature-name
# or
git checkout -b fix/bug-description
```

### Branch Naming Convention

- `feature/` - New features
- `fix/` - Bug fixes
- `refactor/` - Code refactoring
- `docs/` - Documentation updates
- `test/` - Test additions

### 2. Make Your Changes

- Follow the [coding guidelines](#coding-guidelines)
- Write clear commit messages
- Add tests for new features
- Update documentation as needed

### 3. Keep Your Branch Updated

```bash
git fetch upstream
git rebase upstream/main
```

### 4. Test Your Changes

```bash
# Build all modules
./gradlew build

# Run tests
./gradlew test

# Run on device
./gradlew :app:installDebug
```

## Pull Request Process

### Before Submitting

- [ ] Code builds successfully
- [ ] All tests pass
- [ ] No new lint warnings
- [ ] Documentation updated
- [ ] Self-reviewed your code
- [ ] Added tests for new features

### Submitting

1. **Push your branch**:
   ```bash
   git push origin feature/your-feature-name
   ```

2. **Create Pull Request** on GitHub
   - Use the PR template
   - Fill in all relevant sections
   - Link related issues
   - Add screenshots/videos if applicable

3. **Address Review Feedback**
   - Be responsive to comments
   - Make requested changes
   - Push updates to the same branch

### PR Title Format

```
[TYPE] Brief description

Examples:
[FEATURE] Add SMS pattern matching
[FIX] Resolve database migration crash
[REFACTOR] Improve repository pattern
[DOCS] Update architecture documentation
```

## Coding Guidelines

### Kotlin Style

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable names
- Prefer immutability (`val` over `var`)
- Use data classes for models
- Leverage Kotlin features (scope functions, extension functions)

### Code Organization

```kotlin
// Order: Properties -> Init blocks -> Functions
class Example {
    // Constants
    companion object {
        private const val TAG = "Example"
    }
    
    // Properties
    private val repository: Repository
    private var state = State.IDLE
    
    // Init blocks
    init {
        // Initialization
    }
    
    // Public functions
    fun publicFunction() { }
    
    // Private functions
    private fun privateHelper() { }
}
```

### Comments

- Use KDoc for public APIs
- Comment complex logic
- Avoid obvious comments
- Keep comments up-to-date

```kotlin
/**
 * Fetches active triggers from the database.
 *
 * @return List of active triggers or empty list if none found
 */
suspend fun getActiveTriggers(): List<TriggerEntity>
```

### Android Best Practices

- **Lifecycle awareness**: Use lifecycle-aware components
- **Coroutines**: Use coroutines for async operations
- **Resource management**: Close resources properly
- **Memory leaks**: Avoid context leaks
- **Permissions**: Check permissions before use

## Testing

### Writing Tests

```kotlin
// Unit test example
@Test
fun `test trigger insertion`() = runTest {
    val trigger = TriggerEntity(name = "Test")
    val id = repository.insertTrigger(trigger)
    assertThat(id).isGreaterThan(0)
}
```

### Running Tests

```bash
# All tests
./gradlew test

# Specific module
./gradlew :data:data-triggers:test

# Instrumented tests
./gradlew connectedAndroidTest
```

## Module Guidelines

### Creating a New Module

1. **Create module structure**:
   ```bash
   mkdir -p feature/feature-name/src/main/java/com/aserdipanda/synapse/feature/name
   ```

2. **Add `build.gradle.kts`**:
   ```kotlin
   plugins {
       alias(libs.plugins.android.library)
       alias(libs.plugins.kotlin.android)
   }
   // ... configuration
   ```

3. **Register in `settings.gradle.kts`**:
   ```kotlin
   include(":feature:feature-name")
   ```

4. **Add `.gitignore`**:
   ```
   /build
   ```

### Module Dependencies

- Modules should depend on **abstractions**, not implementations
- Avoid circular dependencies
- Keep dependency graph simple

**Good**:
```
:feature:feature-x -> :data:data-x -> :core:core-common
```

**Bad**:
```
:feature:feature-x <-> :data:data-x  (circular)
```

## Code Review

### As a Reviewer

- Be constructive and respectful
- Focus on code quality and maintainability
- Test the changes locally if possible
- Approve when ready or request changes with clear feedback

### As a Contributor

- Address all comments
- Ask questions if feedback is unclear
- Don't take criticism personally
- Thank reviewers for their time

## Commit Messages

### Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- **feat**: New feature
- **fix**: Bug fix
- **refactor**: Code refactoring
- **docs**: Documentation
- **test**: Tests
- **chore**: Maintenance

### Examples

```
feat(triggers): add SMS pattern matching

Implemented regex-based pattern matching for SMS triggers.
Users can now define custom patterns to match incoming messages.

Closes #123

---

fix(database): resolve migration crash on Android 12

Fixed crash when migrating from version 1 to 2 by adding
missing column migration script.

Fixes #456
```

## Reporting Bugs

- Use the bug report template
- Include steps to reproduce
- Provide logcat output
- Specify Android version and device

## Suggesting Features

- Use the feature request template
- Explain the problem you're solving
- Describe the proposed solution
- Consider implementation complexity

## Documentation

When adding features:
- Update README.md
- Update ARCHITECTURE.md if structure changes
- Add KDoc comments for public APIs
- Update QUICKSTART.md if setup changes

## Good First Issues

Look for issues labeled `good-first-issue` for beginner-friendly tasks.

## Questions?

- Check [ARCHITECTURE.md](ARCHITECTURE.md)
- Check [QUICKSTART.md](QUICKSTART.md)
- Open a discussion on GitHub
- Ask in your PR

## Thank You!

Your contributions make Synapse better for everyone. Thank you for taking the time to contribute!

---

**Happy coding!**
