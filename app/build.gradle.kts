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
        targetSdk = 35
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
        // Habilitar soporte para las nuevas APIs del lenguaje
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

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

// The view calendar library for Android
    implementation("com.kizitonwose.calendar:view:2.6.2")
// The compose calendar library for Android
    implementation("com.kizitonwose.calendar:compose:2.6.2")
    implementation("com.google.android.libraries.places:places:4.1.0")  // O la última versión disponible
    implementation ("com.google.android.gms:play-services-auth:20.7.0")
    implementation ("com.google.firebase:firebase-auth-ktx:22.3.1")


    // ...

        // Import the Firebase BoM
        implementation(platform("com.google.firebase:firebase-bom:33.10.0"))

        // When using the BoM, you don't specify versions in Firebase library dependencies

        // Add the dependency for the Firebase SDK for Google Analytics
        implementation("com.google.firebase:firebase-analytics:22.3.0")


        // TODO: Add the dependencies for any other Firebase products you want to use
        // See https://firebase.google.com/docs/android/setup#available-libraries
        // For example, add the dependencies for Firebase Authentication and Cloud Firestore
        implementation("com.google.firebase:firebase-auth:23.2.0")
        implementation("com.google.firebase:firebase-firestore:25.1.2")


        implementation ("com.firebaseui:firebase-ui-auth:8.0.2")



    // Dependencias estándar de AndroidX y otras

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    // Dependencias para pruebas

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    //libreria de firebase  para el inicio de sesion
    implementation ("com.google.firebase:firebase-auth:23.2.0")
    //librerias que nos permiten manejar la base de datos
    implementation ("com.google.firebase:firebase-database:21.0.0")
    implementation ("com.google.firebase:firebase-firestore:25.1.3")



}













