plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.librarymanagementsystem"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.librarymanagementsystem"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

// Android 35 SDK force version conflict fix aur Duplicate class fix ek sath
configurations.all {
    exclude(group = "androidx.ink", module = "ink-geometry-jvm")

    resolutionStrategy.eachDependency {
        if (requested.group == "androidx.core" && requested.name.startsWith("core")) {
            useVersion("1.13.1") // Yeh version SDK 34 par smooth chalta hai aur error nahi dega
        }
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:34.17.0"))
    implementation("com.google.firebase:firebase-auth")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Volley Library Network/API Requests ke liye
    implementation("com.android.volley:volley:1.2.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}