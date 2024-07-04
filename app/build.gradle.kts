plugins {
    id("someone.code.application")
    id("someone.code.compose")
}

android {
    namespace = "me.ysy.collapsing.scaffold"


    defaultConfig {
        applicationId = "me.ysy.collapsing.scaffold"

        versionCode = 1
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
        }
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
}

dependencies {

    implementation(projects.core)

    implementation(libs.core.ktx)

    implementation(libs.activity.core)
    implementation(libs.activity.compose)

    implementation(libs.compose.foundation)
    implementation(libs.compose.material)
    implementation(libs.compose.preview)
}