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
    // gradle/libs.versions.toml is auto-discovered by Gradle 8.1+.
    // Do NOT add versionCatalogs { from(...) } here — Gradle would
    // then call from() twice and throw:
    //   "you can only call the 'from' method a single time"
}

rootProject.name = "StreamTV"
include(":app")
