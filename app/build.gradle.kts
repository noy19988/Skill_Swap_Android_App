plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    id("kotlin-kapt")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.skill_swap_app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.skill_swap_app"
        minSdk = 29
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.glide)
    annotationProcessor(libs.compiler)
    implementation(libs.picasso)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)

    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.recyclerview)
    implementation(libs.play.services.maps)

    // Firebase BOM
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    kapt(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // Retrofit for networking
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // OkHttp for logging HTTP requests
    implementation(libs.logging.interceptor)
    implementation(libs.okhttp)

    // ✅ Cloudinary - עדכון לתלות ישירה
    implementation("com.cloudinary:cloudinary-android:2.0.0") // נסה גרסה זו
    // ניתן לנסות גם גרסאות ישנות יותר אם עדיין לא עובד:
    // implementation("com.cloudinary:cloudinary-android:1.31.0")

    // ViewModel and LiveData
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // Coroutine for background tasks
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.cardview)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
