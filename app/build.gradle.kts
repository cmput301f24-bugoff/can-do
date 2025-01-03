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

    testOptions {
        animationsDisabled = true
        execution = "ANDROIDX_TEST_ORCHESTRATOR"

        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }

        execution = "ANDROIDX_TEST_ORCHESTRATOR"
    }
}

dependencies {
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation(libs.play.services.location)
    testImplementation("junit:junit:4.13.2")
    testImplementation(libs.core)
    // testImplementation(libs.testng) # I think this is the correct decision
    testImplementation(libs.mockito.core)
    implementation ("com.google.android.gms:play-services-maps:18.0.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.vintage:junit-vintage-engine:5.8.2")
    testImplementation("org.mockito:mockito-core:5.7.0")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation(libs.firebase.storage)
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")
    implementation(libs.zxing.android.embedded)
    implementation(libs.material.v190)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.firebase.firestore)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)

    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("org.mockito:mockito-junit-jupiter:5.3.1")

    dokkaPlugin(libs.kotlinAsJavaPlugin)

    androidTestImplementation("org.mockito:mockito-android:5.7.0")
    androidTestImplementation("org.mockito:mockito-core:5.7.0")
    androidTestImplementation("com.google.android.gms:play-services-tasks:18.0.2")

    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test:orchestrator:1.4.2")
    androidTestUtil("androidx.test:orchestrator:1.4.2")
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
