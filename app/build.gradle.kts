plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")

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
    implementation("com.google.android.libraries.places:places:4.1.0")  // O la última versión disponible

        // ...

        // Import the Firebase BoM
        implementation(platform("com.google.firebase:firebase-bom:33.9.0"))

        // When using the BoM, you don't specify versions in Firebase library dependencies

        // Add the dependency for the Firebase SDK for Google Analytics
        implementation("com.google.firebase:firebase-analytics")

        // TODO: Add the dependencies for any other Firebase products you want to use
        // See https://firebase.google.com/docs/android/setup#available-libraries
        // For example, add the dependencies for Firebase Authentication and Cloud Firestore
        implementation("com.google.firebase:firebase-auth")
        implementation("com.google.firebase:firebase-firestore")


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
