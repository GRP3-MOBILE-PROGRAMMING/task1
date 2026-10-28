plugins {
    alias(libs.plugins.android.application)
    checkstyle
}

// ===== Checkstyle: style gate for CI (see config/checkstyle/checkstyle.xml) =====
checkstyle {
    toolVersion = "10.21.4"
    configFile = rootProject.file("config/checkstyle/checkstyle.xml")
    configDirectory = rootProject.file("config/checkstyle")
    isIgnoreFailures = false // violations fail the task
    maxErrors = 0            // any error fails the build
    maxWarnings = 0          // raise temporarily during a cleanup phase if needed
    isShowViolations = true
}

val checkstyleTask = tasks.register<Checkstyle>("checkstyle") {
    group = "verification"
    description = "Runs Checkstyle on the UNEB e-Results Java sources."
    source("src/main/java")
    include("**/*.java")
    exclude("**/R.java", "**/BuildConfig.java")
    classpath = files()
    reports {
        xml.required = true  // read by Jenkins Warnings NG
        html.required = true // human-readable report
    }
}

// Run the style check before every build and as part of `gradlew check`
tasks.named("preBuild") { dependsOn(checkstyleTask) }
tasks.named("check") { dependsOn(checkstyleTask) }

android {
    namespace = "com.example.buildgradle"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.buildgradle"
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
}


dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("androidx.lifecycle:lifecycle-viewmodel:2.9.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.9.0")
    implementation("commons-net:commons-net:3.11.1")
}