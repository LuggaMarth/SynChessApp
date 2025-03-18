plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.synchess"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.synchess"
        minSdk = 28
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.javafx.base)
    implementation(libs.javafx.graphics)
    implementation(libs.javafx.controls)
    implementation(libs.javafx.fxml)


    implementation(libs.appcompat)
    implementation(fileTree("lib") { include("*.jar") })
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    implementation ("com.squareup.picasso:picasso:2.71828")
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}