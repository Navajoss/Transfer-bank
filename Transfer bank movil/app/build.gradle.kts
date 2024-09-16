plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    namespace = "com.example.fileuploaderapp" // Asegúrate de usar el namespace de tu app
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.fileuploaderapp" // Asegúrate de que coincida con tu app
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        kotlinOptions {
            jvmTarget = "17"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    // Dependencias de Android y Kotlin
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-ktx:1.6.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")

    // Retrofit para manejar las solicitudes HTTP
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp, usado internamente por Retrofit para manejar las peticiones
    implementation("com.squareup.okhttp3:okhttp:4.9.1")

    // Testing (opcional)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Dependency for Network Service Discovery (NSD)
    implementation("androidx.legacy:legacy-support-v4:1.0.0") // Required for NSDManager

    // Coroutine for background operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0")
    // Otras dependencias
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")

}
