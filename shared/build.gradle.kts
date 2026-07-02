import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    jvm()

    jvmToolchain(21)

    js {
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serializationJson)
        }
        jvmMain.dependencies {
            implementation(libs.reflections)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
    sourceSets.jvmMain.dependencies {
        implementation(kotlin("reflect"))
    }
}
