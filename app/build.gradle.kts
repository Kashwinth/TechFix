import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

val mapsApiKey = providers.gradleProperty("MAPS_API_KEY")
    .orElse(providers.environmentVariable("MAPS_API_KEY"))
    .orElse(providers.provider {
        val localProperties = rootProject.file("local.properties")
        if (!localProperties.exists()) return@provider ""
        Properties().apply {
            localProperties.inputStream().use { input -> load(input) }
        }.getProperty("MAPS_API_KEY", "")
    })

android {
    namespace = "com.example.techfix"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.techfix"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey.get()
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.play.services.maps)
    implementation(libs.room.runtime)
    implementation(libs.room.guava)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.viewmodel)
    annotationProcessor("androidx.room:room-compiler:2.7.1")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}