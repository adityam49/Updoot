plugins {
    id("com.android.application")
    id("kotlin-android")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    compileSdk = 33
    defaultConfig {
        applicationId = "com.ducktapedapps.updoot"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        vectorDrawables.useSupportLibrary = true
    }
    buildTypes {
        getByName("debug") {
            buildConfigField("String", "CLIENT_ID", project.property("UPDOOT_CLIENT_ID") as String)
            buildConfigField("String", "REDIRECT_URI", project.property("UPDOOT_REDIRECT_URI") as String)
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField("String", "CLIENT_ID", project.property("UPDOOT_CLIENT_ID") as String)
            buildConfigField("String", "REDIRECT_URI", project.property("UPDOOT_REDIRECT_URI") as String)
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.4"
    }
    namespace = "com.ducktapedapps.updoot"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf(
            "-opt-in=kotlinx.coroutines.FlowPreview",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=androidx.compose.material.ExperimentalMaterialApi",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi"
        )
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(project(":navigation"))

    //android support libs
    implementation("androidx.appcompat:appcompat:1.4.2")
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    implementation("androidx.fragment:fragment-ktx:1.4.1")
    implementation("androidx.activity:activity-compose:1.5.0")

    //compose stuff
    val composeBom = platform("androidx.compose:compose-bom:2023.01.00")
    implementation(composeBom)
    implementation("androidx.compose.runtime:runtime")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation-layout")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")

    //android lifecycle
    val lifecycle_version = rootProject.extra.get("lifecycle_version") as String
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle_version")

    //kotlin-coroutines
    val kotlin_coroutines = rootProject.extra.get("kotlin_coroutines") as String
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlin_coroutines")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$kotlin_coroutines")

    //Retrofit
    val retrofit_version = rootProject.extra.get("retrofit_version") as String
    implementation("com.squareup.retrofit2:retrofit:$retrofit_version")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")

    //persistence
    val room_version = rootProject.extra.get("room_version") as String
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    kapt("androidx.room:room-compiler:$room_version")

    val dataStore_version = rootProject.extra.get("dataStore_version") as String
    implementation("androidx.datastore:datastore-preferences:$dataStore_version")

    //workManager for deferred work
    val work_manager_version = rootProject.extra.get("work_manager_version") as String
    implementation("androidx.work:work-runtime-ktx:$work_manager_version")

    //moshi for Json
    val moshi_version = rootProject.extra.get("moshi_version") as String
    implementation("com.squareup.moshi:moshi-kotlin:$moshi_version")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:$moshi_version")
    implementation("com.squareup.retrofit2:converter-moshi:$retrofit_version")

    //image loading
    //photoview
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    val glide_version = rootProject.extra.get("glide_version") as String
    //glide
    implementation("com.github.bumptech.glide:glide:$glide_version")
    kapt("com.github.bumptech.glide:compiler:$glide_version")
    //coil
    val coil_version = rootProject.extra.get("coil_version") as String
    implementation("io.coil-kt:coil:$coil_version")
    implementation("io.coil-kt:coil-compose:$coil_version")

    //video loading
    implementation("com.google.android.exoplayer:exoplayer:2.18.0")

    //hilt for di
    val hilt_version = rootProject.extra.get("hilt_version") as String
    implementation("com.google.dagger:hilt-android:$hilt_version")
    kapt("com.google.dagger:hilt-android-compiler:$hilt_version")

    val hilt_jetpack_version = rootProject.extra.get("hilt_jetpack_version") as String
    implementation("androidx.hilt:hilt-common:$hilt_jetpack_version")
    implementation("androidx.hilt:hilt-work:$hilt_jetpack_version")
    kapt("androidx.hilt:hilt-compiler:$hilt_jetpack_version")

    //web-page parser for extracting link metadata
    val jsoup_version = rootProject.extra.get("jsoup_version") as String
    implementation("org.jsoup:jsoup:$jsoup_version")

    //leak canary for detecting memory leaks
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.9.1")

    //navigation
    val compose_navigation = rootProject.extra.get("compose_navigation") as String
    implementation("androidx.navigation:navigation-compose:${compose_navigation}")
    implementation("androidx.hilt:hilt-navigation-compose:1.0.0")
    implementation("com.google.accompanist:accompanist-navigation-material:0.21.0-beta")

    implementation("com.google.firebase:firebase-crashlytics-ktx:18.2.12")
    implementation("com.google.firebase:firebase-analytics-ktx:21.1.0")


    //testing
    val mockk_version = rootProject.extra.get("mockk_version") as String
    val kotlin_version = rootProject.extra.get("kotlin_version") as String
    testImplementation("junit:junit:4.13.1")
    testImplementation("io.mockk:mockk:$mockk_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlin_version")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlin_coroutines")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlin_coroutines")
}

