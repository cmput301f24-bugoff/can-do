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

    buildFeatures {
        dataBinding = true
    }
}

dependencies {
    implementation(libs.zxing.android.embedded)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.firebase.firestore)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)

    dokkaPlugin(libs.kotlinAsJavaPlugin)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register<org.jetbrains.dokka.gradle.DokkaTask>("generateJavadoc") {
    outputDirectory.set(layout.buildDirectory.dir("docs/javadoc"))

    dokkaSourceSets {
        create("androidMain") {
            displayName.set("JavaMain")
            sourceRoots.from(file("src/main/java"))  // Point to Java files in the app module
            platform.set(org.jetbrains.dokka.Platform.jvm)  // Set platform to JVM for Java

            languageVersion.set("21")
            apiVersion.set("21")

            // Include visibility settings to document private/protected members
            documentedVisibilities.set(
                setOf(
                    org.jetbrains.dokka.DokkaConfiguration.Visibility.PUBLIC,
                    org.jetbrains.dokka.DokkaConfiguration.Visibility.PROTECTED,
                    org.jetbrains.dokka.DokkaConfiguration.Visibility.PACKAGE,
                    org.jetbrains.dokka.DokkaConfiguration.Visibility.PRIVATE
                )
            )
        }
    }
}