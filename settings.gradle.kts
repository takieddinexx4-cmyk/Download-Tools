pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        maven(url = "https://maven-central.storage-download.googleapis.com/maven2/")
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven(url = "https://maven-central.storage-download.googleapis.com/maven2/")
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "GlassGrab"

include(":app")
include(":core:common")
include(":core:domain")
include(":core:engine")
include(":core:data")
include(":core:design")
include(":feature:home")
include(":feature:formats")
include(":feature:downloads")
include(":feature:library")
include(":feature:player")
include(":feature:settings")
include(":benchmark")
