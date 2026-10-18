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
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AndroidAIAgents"
include(":app")
include(":agents-core")
include(":agents-ready")
include(":agents-gemini")
include(":agents-firebase")
include(":agents-adk")
include(":agents-tools")
include(":agents-multiagent")
include(":agents-comm")
include(":agents-rag")
include(":agents-voice")
include(":agents-vision")
include(":agents-compose")
include(":agents-a2ui")
include(":agents-persistence")
include(":agents-observability")
include(":agents-testing")
