// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    extra.apply{
        set("compose_navigation","2.5.0")
        set("accompanist_navigation","0.20.0")
        set("compose_version" , "1.1.1")
        set("kotlin_version","1.8.10")
        set("kotlin_coroutines","1.6.0")
        set("lifecycle_version" ,"2.5.0")
        set("retrofit_version","2.9.0")
        set("hilt_version","2.44")
        set("hilt_jetpack_version" ,"1.0.0")
        set("glide_version","4.12.0")
        set("moshi_version","1.14.0")
        set("room_version","2.4.2")
        set("work_manager_version","2.7.1")
        set("jsoup_version", "1.15.1")
        set("exoPlayer_version ", "2.18.0")
        set("dataStore_version" ,"1.0.0")
        set("mockk_version" ,"1.10.6")
        set("coil_version" ,"1.4.0")
    }
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10")
        classpath("com.android.tools.build:gradle:7.4.2")
        classpath("com.google.dagger:hilt-android-gradle-plugin:${rootProject.extra.get("hilt_version") as String}")
        classpath("com.google.gms:google-services:4.3.13")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.1")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://www.jitpack.io") }
    }
}

tasks {
    register("clean", Delete::class) {
        delete(rootProject.buildDir)
    }
}
