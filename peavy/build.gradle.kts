plugins {
    id("com.android.library")
    id("maven-publish")
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "peavy"
    compileSdk = 34

    version = "0.9.15"

    defaultConfig {
        minSdk = 21
        buildConfigField("String", "VERSION", "\"${version}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    publishing {
        singleVariant("release")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildToolsVersion = "34.0.0"
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.github.peavy-log"
            artifactId = "android"
            version = project.version.toString()
            afterEvaluate {
                from(components["release"])
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.okhttp)
    compileOnly(libs.timber)
    compileOnly(libs.okhttp)

    coreLibraryDesugaring(libs.desugar)
}