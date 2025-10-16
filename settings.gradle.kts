pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Synapse"
include(":app")

// Core modules
include(":core:core-common")
include(":core:core-network")

// Data modules
include(":data:data-triggers")

// Feature modules
include(":feature:feature-triggers")

// Service modules
include(":service:service-sms")
