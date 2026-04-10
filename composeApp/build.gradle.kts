import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias (libs.plugins.google.services)
//    id("com.google.gms.google-services") version "4.4.3" apply false

}

val sharedMarketingVersion = rootProject.extra["appMarketingVersion"] as String
val sharedBuildNumber = (rootProject.extra["appBuildNumber"] as String).toInt()
val iosVersionConfigFile = rootProject.file("iosApp/Configuration/Version.xcconfig")

val syncIosVersionConfig by tasks.registering {
    outputs.file(iosVersionConfigFile)
    doLast {
        iosVersionConfigFile.writeText(
            """
            // Generated from /build.gradle.kts. Do not edit manually.
            APP_MARKETING_VERSION = $sharedMarketingVersion
            APP_BUILD_NUMBER = $sharedBuildNumber
            """.trimIndent() + "\n"
        )
    }
}

tasks.matching {
    it.name == "preBuild" || it.name == "embedAndSignAppleFrameworkForXcode"
}.configureEach {
    dependsOn(syncIosVersionConfig)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            linkerOpts("-framework", "AVFoundation", "-framework", "AudioToolbox")
        }

        iosTarget.compilations.getByName("main") {
            cinterops.create("SwiftProvider") {
                definitionFile.set(file(rootDir.absolutePath + "/composeApp/src/iosMain/c_interop/SwiftProvider.def"))
                includeDirs.allHeaders(rootDir.absolutePath + "/iosApp/iosApp/Bridge/")
            }
        }
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }
    
    sourceSets {
        
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.androidx.webkit)
            implementation(libs.android.naver)
            implementation(libs.android.kakao)
            implementation(project.dependencies.platform("com.google.firebase:firebase-bom:34.3.0"))
            implementation(libs.google.firebase.messaging)
            implementation(libs.google.firebase.auth)
            implementation(libs.androidx.work.runtime.ktx)





        }

        commonMain.dependencies {
            implementation(libs.kotlinx.datetime)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)

            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.jetbrains.compose.navigation)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.androidx.room.runtime)
            implementation(libs.sqlite.bundled)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            api(libs.koin.core)

            implementation(libs.bundles.ktor)
            implementation(libs.bundles.coil)
            implementation(libs.multiplatform.settings)



        }
        nativeMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        dependencies {
            ksp(libs.androidx.room.compiler)
        }
    }
}

android {
    namespace = "com.bium.youngssoo"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    signingConfigs {
        val keystorePropertiesFile = rootProject.file("local.properties")
        val keystoreProperties = Properties()
        if (keystorePropertiesFile.exists()) {
            keystoreProperties.load(FileInputStream(keystorePropertiesFile))
        }

        val storePW = keystoreProperties.getProperty("KEYSTORE_PASSWORD") ?: "bium1234"
        val keyAls = keystoreProperties.getProperty("KEY_ALIAS") ?: "bium"
        val keyPW = keystoreProperties.getProperty("KEY_PASSWORD") ?: "bium1234"

        create("release") {
            storeFile = file("bium_key.jks")
            storePassword = storePW
            keyAlias = keyAls
            keyPassword = keyPW
        }
        getByName("debug") {
            storeFile = file("bium_key.jks")
            storePassword = storePW
            keyAlias = keyAls
            keyPassword = keyPW
        }
    }

    defaultConfig {
        applicationId = "com.bium.youngssoo"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = sharedBuildNumber
        versionName = sharedMarketingVersion
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isDebuggable = true
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}
