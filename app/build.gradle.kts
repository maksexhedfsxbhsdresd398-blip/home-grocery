plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.grocerynative"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.grocerynative"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug { }
    }

    buildFeatures { viewBinding = true }

    // Java 17 for Java sources
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// Kotlin 17 toolchain
kotlin {
    jvmToolchain(17)
}

dependencies {
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")

    // AndroidX / UI
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")

    // ✅ Needed for viewModels {} delegate
    implementation("androidx.activity:activity-ktx:1.9.2")
    // ✅ DialogFragment / Fragment utilities
    implementation("androidx.fragment:fragment-ktx:1.7.1")
    // ✅ You use CardView in your XML
    implementation("androidx.cardview:cardview:1.0.0")
}
