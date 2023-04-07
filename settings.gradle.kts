@file:Suppress("UnstableApiUsage")

enableFeaturePreview("VERSION_CATALOGS")
include("app")
include("navigation")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {

            version("lifecycle-version", "2.6.0")
            version("coroutines-version", "1.6.0")
            version("room-version", "2.4.2")
            version("retrofit-version", "2.9.0")
            version("moshi-version", "1.14.0")
            version("hilt-version", "2.44")
            version("hilt-jetpack-version", "1.0.0")
            version("coil-version", "2.3.0")
            version("kotlinVersion", "1.8.10")
            version("chuckerVersion","3.5.2")
            version("accompanistVersion","0.30.0")
            //android core libs
            library("appcompat", "androidx.appcompat:appcompat:1.4.2")
            library("core-ktx", "androidx.core:core-ktx:1.9.0")
            library("activity-compose", "androidx.activity:activity-compose:1.5.0")
            bundle(
                "android-core", listOf("appcompat", "core-ktx", "activity-compose")
            )

            library("compose-bom", "androidx.compose:compose-bom:2023.01.00")
            library("compose-runtime", "androidx.compose.runtime", "runtime").withoutVersion()
            library("compose-ui", "androidx.compose.ui", "ui").withoutVersion()
            library("compose-ui-tooling", "androidx.compose.ui", "ui-tooling").withoutVersion()
            library(
                "compose-foundation", "androidx.compose.foundation", "foundation"
            ).withoutVersion()
            library(
                "compose-foundation-layout", "androidx.compose.foundation", "foundation-layout"
            ).withoutVersion()
//            library("compose-material", "androidx.compose.material", "material").withoutVersion()
            library(
                "compose-constraintlayout",
                "androidx.constraintlayout:constraintlayout-compose:1.0.1"
            )
            library("compose-Material3","androidx.compose.material3:material3:1.0.1")
            library("compose-flowRow","com.google.accompanist","accompanist-flowlayout").versionRef("accompanistVersion")
            bundle(
                "compose", listOf(
                    "compose-runtime",
                    "compose-ui",
                    "compose-ui-tooling",
                    "compose-foundation",
                    "compose-foundation-layout",
                    "compose-constraintlayout",
                    "compose-Material3",
                    "compose-flowRow"
                )
            )

            library(
                "lifecycle-compose",
                "androidx.lifecycle",
                "lifecycle-runtime-compose"
            ).versionRef("lifecycle-version")
            library(
                "livedata-viewmodel-ktx", "androidx.lifecycle", "lifecycle-viewmodel-ktx"
            ).versionRef("lifecycle-version")
            library(
                "livedata-viewmodel-compose", "androidx.lifecycle", "lifecycle-viewmodel-compose"
            ).versionRef("lifecycle-version")
            bundle(
                "lifecycle", listOf(
                    "lifecycle-compose",
                    "livedata-viewmodel-ktx",
                    "livedata-viewmodel-compose",
                )
            )

            library(
                "coroutines-core", "org.jetbrains.kotlinx", "kotlinx-coroutines-core"
            ).versionRef("coroutines-version")
            library(
                "coroutines-android", "org.jetbrains.kotlinx", "kotlinx-coroutines-android"
            ).versionRef("coroutines-version")
            library(
                "coroutines-test", "org.jetbrains.kotlinx", "kotlinx-coroutines-test"
            ).versionRef("coroutines-version")
            library(
                "coroutines-test-core", "org.jetbrains.kotlinx", "kotlinx-coroutines-core"
            ).versionRef("coroutines-version")
            bundle(
                "coroutines-test", listOf(
                    "coroutines-test", "coroutines-test-core"
                )
            )
            bundle(
                "coroutines", listOf("coroutines-android", "coroutines-core")
            )

            library("room-runtime", "androidx.room", "room-runtime").versionRef("room-version")
            library("room-ktx", "androidx.room", "room-ktx").versionRef("room-version")
            library("room-compiler", "androidx.room", "room-compiler").versionRef("room-version")
            bundle(
                "room", listOf(
                    "room-runtime", "room-ktx"
                )
            )

            library("retrofit", "com.squareup.retrofit2", "retrofit").versionRef("retrofit-version")
            library("http-logger", "com.squareup.okhttp3", "logging-interceptor").version("4.9.0")
            library(
                "retrofit-moshi-coverter", "com.squareup.retrofit2", "converter-moshi"
            ).versionRef("retrofit-version")
            library("moshi", "com.squareup.moshi", "moshi-kotlin").versionRef("moshi-version")
            library(
                "moshi-codegen", "com.squareup.moshi", "moshi-kotlin-codegen"
            ).versionRef("moshi-version")
            bundle(
                "retrofit", listOf(
                    "retrofit", "http-logger", "retrofit-moshi-coverter", "moshi"
                )
            )


            library("hilt-android", "com.google.dagger", "hilt-android").versionRef("hilt-version")
            library(
                "hilt-android-compiler", "com.google.dagger", "hilt-android-compiler"
            ).versionRef("hilt-version")
            library(
                "hilt-common", "androidx.hilt", "hilt-common"
            ).versionRef("hilt-jetpack-version")
            library("hilt-work", "androidx.hilt", "hilt-work").versionRef("hilt-jetpack-version")
            library(
                "hilt-compiler", "androidx.hilt", "hilt-compiler"
            ).versionRef("hilt-jetpack-version")
            bundle(
                "hilt", listOf(
                    "hilt-android", "hilt-common", "hilt-work"
                )
            )

            library("coil", "io.coil-kt", "coil").versionRef("coil-version")
            library("coil-compose", "io.coil-kt", "coil-compose").versionRef("coil-version")
            bundle("coil", listOf("coil", "coil-compose"))

            library("exoPlayer", "com.google.android.exoplayer:exoplayer:2.18.0")

            library("dataStore", "androidx.datastore:datastore-preferences:1.0.0")


            library("workManager", "androidx.work:work-runtime-ktx:2.7.1")

            library("jsoup", "org.jsoup:jsoup:1.15.1")

            library("leakCanary", "com.squareup.leakcanary:leakcanary-android:2.9.1")

            library("crashlyticsKtx", "com.google.firebase:firebase-crashlytics-ktx:18.2.12")
            library("analyticsKtx", "com.google.firebase:firebase-analytics-ktx:21.1.0")
            bundle("crashlytics", listOf("analyticsKtx", "crashlyticsKtx"))
            library("composeNavigation", "androidx.navigation:navigation-compose:2.5.0")
            library("hiltComposeNavigation", "androidx.hilt:hilt-navigation-compose:1.0.0")
            library(
                "accompanistNavigationMaterial",
                "com.google.accompanist","accompanist-navigation-material"
            ).versionRef("accompanistVersion")
            bundle(
                "navigation", listOf(
                    "composeNavigation", "hiltComposeNavigation", "accompanistNavigationMaterial"
                )
            )

            library("test-mockk", "io.mockk:mockk:1.10.6")
            library(
                "test-kotlin",
                "org.jetbrains.kotlin",
                "kotlin-test"
            ).versionRef("kotlinVersion")
            library("test-junit", "junit:junit:4.13.1")
            library("timber", "com.jakewharton.timber:timber:5.0.1")

            library("chucker","com.github.chuckerteam.chucker","library").versionRef("chuckerVersion")
            library("chuckerNoOp","com.github.chuckerteam.chucker","library-no-op").versionRef("chuckerVersion")
        }
    }
}
