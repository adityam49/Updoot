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

    implementation(libs.bundles.android.core)

    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)

    implementation(libs.bundles.lifecycle)

    implementation(libs.bundles.coroutines)
    testImplementation(libs.bundles.coroutines.test)

    implementation(libs.bundles.room)
    kapt(libs.room.compiler)

    implementation(libs.bundles.retrofit)
    kapt(libs.moshi.codegen)

    implementation(libs.bundles.hilt)
    kapt(libs.hilt.compiler)
    kapt(libs.hilt.android.compiler)

    implementation(libs.bundles.coil)
    implementation(libs.exoPlayer)

    implementation(libs.dataStore)
    implementation(libs.workManager)
    implementation(libs.jsoup)
    debugImplementation(libs.leakCanary)
    implementation(libs.bundles.crashlytics)
    implementation(libs.bundles.navigation)

    testImplementation(libs.test.kotlin)
    testImplementation(libs.test.junit)
    testImplementation(libs.test.mockk)

    //todo remove
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    val glide_version = rootProject.extra.get("glide_version") as String
    //glide
    implementation("com.github.bumptech.glide:glide:$glide_version")
    kapt("com.github.bumptech.glide:compiler:$glide_version")
}

