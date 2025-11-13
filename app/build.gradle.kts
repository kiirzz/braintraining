plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.spotless)
}

android {
    namespace = "com.braintraining"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.braintraining"
        minSdk = 24
        targetSdk = 36
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
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)

    // Hilt (DI)
    implementation(libs.hilt)
    ksp(libs.hilt.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Internal modules
    implementation(project(":feature:home"))
    implementation(project(":feature:games"))

    implementation(project(":core:data"))
    implementation(project(":core:model"))
    implementation(project(":core:systemdesign"))

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

subprojects {
    plugins.withId("kotlin") {
        apply(plugin = "com.diffplug.spotless")

        extensions.configure<com.diffplug.gradle.spotless.SpotlessExtension>("spotless") {
            kotlin {
                target("**/*.kt")
                ktlint()
                indentWithSpaces()
                endWithNewline()
            }

            java {
                target("**/*.java")
                googleJavaFormat()
                removeUnusedImports()
                trimTrailingWhitespace()
            }

            format("xml") {
                target("**/*.xml")
                indentWithSpaces(4)
                trimTrailingWhitespace()
                endWithNewline()
            }

            kotlinGradle {
                target("**/*.gradle.kts")
                ktlint()
                trimTrailingWhitespace()
                endWithNewline()
            }
        }
    }
}

