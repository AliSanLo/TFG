plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.example.tfg"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.tfg"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

    dependencies {
        // Core AndroidX
        implementation(libs.androidx.core.ktx) {
            exclude(group = "com.android.support", module = "support-compat")
        }
        implementation(libs.androidx.appcompat) {
            exclude(group = "com.android.support", module = "support-compat")
        }
        implementation(libs.material) {
            exclude(group = "com.android.support", module = "support-compat")
        }

        // AndroidX Activity & ConstraintLayout
        implementation(libs.androidx.activity) {
            exclude(group = "com.android.support", module = "support-compat")
        }
        implementation(libs.androidx.constraintlayout) {
            exclude(group = "com.android.support", module = "support-compat")
        }

        // Material CalendarView
        implementation("com.prolificinteractive:material-calendarview:1.4.3") {
            exclude(group = "com.android.support", module = "support-compat")
        }

        // Testing
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
    }


// Para resolver conflictos entre los módulos de soporte antiguos y las dependencias de AndroidX
configurations.all {
    resolutionStrategy {
        force("androidx.core:core:1.13.1")
        force("androidx.appcompat:appcompat:1.7.0")
        force("com.google.android.material:material:1.9.0")
        // Puedes forzar otras dependencias de androidx aquí si es necesario
    }
}
