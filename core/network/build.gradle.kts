plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.hamric.core.network"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 30

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        buildConfig = true
    }

}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.insert.koin.android)
    implementation(libs.squareup.retrofit2)
    implementation(libs.squareup.retrofit.moshi)
    implementation(libs.squareup.okhttp3)
    implementation(libs.squareup.okhttp3.logging.interceptor)
    implementation(libs.squareup.moshi)
    implementation(libs.squareup.moshi.kotlin)
    implementation(libs.github.chuckerteam.chucker)
    implementation(libs.kotlinx.coroutines.core)
    ksp(libs.squareup.moshi.kotlin.codegen)
}