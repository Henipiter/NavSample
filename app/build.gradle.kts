plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-parcelize")
    id("de.mannodermaus.android-junit5") version "1.9.3.0"
    id("kotlin-kapt")
}

android {
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
    namespace = "com.example.navsample"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.navsample"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        val apiToken: String = project.findProperty("GEMINI_API_KEY") as String? ?: ""
        buildConfigField("String", "GEMINI_API_KEY", apiToken)

    }
    buildFeatures {
        buildConfig = true
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

    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
//            applicationIdSuffix = ".dev"
//            versionNameSuffix = "-dev"
            buildConfigField("Boolean", "DEVELOPER", "true")
        }
        create("prod") {
            dimension = "environment"
            buildConfigField("Boolean", "DEVELOPER", "false")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.google.android.flexbox:flexbox:3.0.0")

    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")

    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:rules:1.6.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.2")
    androidTestImplementation("org.junit.jupiter:junit-jupiter:5.11.2")
    val room_version = "2.6.1"
    implementation("androidx.room:room-ktx:$room_version")
    implementation("androidx.room:room-runtime:$room_version")
    kapt("androidx.room:room-compiler:$room_version")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    implementation("androidx.navigation:navigation-fragment-ktx:2.8.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.5")
    implementation("com.github.duanhong169:colorpicker:1.1.6")

    implementation("androidx.camera:camera-core:1.4.1")
    implementation("androidx.camera:camera-camera2:1.4.1")
    implementation("androidx.camera:camera-view:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    implementation("com.google.mlkit:vision-common:17.3.0")
    implementation("com.google.mlkit:text-recognition:16.0.1")
    implementation("com.github.chrisbanes:PhotoView:2.3.0")

    implementation("com.vanniktech:android-image-cropper:4.6.0")

    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    testImplementation("org.mockito:mockito-junit-jupiter:5.14.2")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.2")
    testImplementation("org.mockito:mockito-core:5.14.2")

    testImplementation("org.junit.jupiter:junit-jupiter-params:5.11.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")

    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.11.2")
    androidTestImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("org.mockito:mockito-core:5.14.2")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("org.mockito:mockito-android:3.6.0")


}