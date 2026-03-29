plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ktor)

    application
}

group = "isel.pt.cbdcg"
version = "1.0.0"
application {
    mainClass.set("isel.pt.cbdcg.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")

}

dependencies {
    implementation("io.ktor:ktor-server-auth:${version}")
    implementation("io.ktor:ktor-server-auth-jwt:${version}")
    implementation("io.ktor:ktor-client-core:${version}")
    implementation("io.ktor:ktor-client-cio:${version}")
    implementation("io.ktor:ktor-client-content-negotiation:${version}")
    implementation(projects.shared)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.h2)
    implementation(libs.logback)
    implementation(libs.kotlinx.serializationJson)
    implementation(libs.ktor.serializationKotlinxJson)
    implementation(libs.ktor.serverContentNegotiation)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.serverStatusPages)
    implementation(libs.identity.jvm)
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}
