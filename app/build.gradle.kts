plugins {
    alias(libs.plugins.android.application)

    id("com.google.gms.google-services")

    id("org.jetbrains.dokka") version "2.0.0-Beta"
}

android {
    namespace = "com.bugoff.can_do"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.bugoff.can_do"
        minSdk = 28
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.firebase.firestore)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register("generateJavadoc", org.jetbrains.dokka.gradle.DokkaTask::class) {
    outputDirectory.set(layout.buildDirectory.dir("docs/javadoc"))
}
