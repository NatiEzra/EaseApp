import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-kapt")
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}

val room_version = "2.6.1"
val secretsPropertiesFile = rootProject.file("secrets.properties")
val secretsProperties = Properties()
secretsProperties.load(FileInputStream(secretsPropertiesFile))

android {
    namespace = "com.example.ease"
    compileSdk = 35
    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.ease"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "GOOGLE_CLIENT_ID", secretsProperties["GOOGLE_CLIENT_ID"] as String)
        buildConfigField("String", "Articles_Api", "\"${project.properties["Articles_Api"] ?: ""}\"")
        buildConfigField("String", "GOOGLE_MAPS_API_KEY", "\"${project.properties["GOOGLE_MAPS_API_KEY"] ?: ""}\"")
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    implementation(libs.play.services.auth)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.fragment.ktx.v276)
    implementation(libs.androidx.navigation.ui.ktx.v276)
    implementation(libs.androidx.navigation.fragment.ktx.v260)

    implementation(libs.androidx.room.common)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.ui.test.android)
    kapt(libs.androidx.room.compiler)

    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation("com.google.code.gson:gson:2.8.9")
    implementation(libs.okhttp)

    implementation(libs.picasso)
    implementation(libs.wasabeef.picasso.transformations)
    implementation(libs.glide)

    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.cardview)

    implementation("io.socket:socket.io-client:2.1.0")

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation("io.socket:socket.io-client:2.1.0")
    implementation ("com.squareup.okhttp3:okhttp:4.10.0")
    implementation (libs.androidx.activity.ktx)
    implementation (libs.moshi)
    implementation (libs.moshi.kotlin)
    implementation (libs.moshi.v1140)
    implementation (libs.moshi.kotlin.v1140)
    implementation (libs.moshi.adapters)
    implementation (libs.converter.moshi)



}