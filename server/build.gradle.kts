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
    implementation("io.ktor:ktor-server-auth:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-auth-jwt:${libs.versions.ktor.get()}")
    implementation(libs.ktor.clientCore)
    implementation(libs.ktor.clientCio)
    implementation(libs.ktor.clientContentNegotiation)
    implementation(libs.ktor.serverWebsockets)
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
    implementation("io.ktor:ktor-server-core:3.3.3")
    implementation("io.ktor:ktor-server-websockets:3.3.3")
    implementation("io.ktor:ktor-server-core:3.3.3")
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}
