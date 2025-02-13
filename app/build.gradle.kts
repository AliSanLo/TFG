plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.tfgdeverdad"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.tfgdeverdad"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        //añadido para poder usar el Calendar View debido a que el minSdkVersion es anterior a las 26
        multiDexEnabled = true

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
        // Enable support for the new language APIs (traducir)
           isCoreLibraryDesugaringEnabled = true


        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

implementation(libs.places)

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")

// The view calendar library for Android
    implementation("com.kizitonwose.calendar:view:2.6.0")
// The compose calendar library for Android
    implementation("com.kizitonwose.calendar:compose:2.6.0")





    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
