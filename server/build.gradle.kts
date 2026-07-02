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

    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.json)

    implementation(libs.h2)
    implementation(libs.reflections)
    implementation("org.reflections:reflections:0.10.2")
    implementation("org.postgresql:postgresql:42.7.8")
    implementation("io.ktor:ktor-server-auth:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-auth-jwt:${libs.versions.ktor.get()}")

    implementation(libs.ktor.serverAuth)
    implementation(libs.ktor.serverAuthJwt)
    implementation(libs.ktor.serverCors)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.serverWebsockets)
    implementation(libs.ktor.serverContentNegotiation)
    implementation(libs.ktor.serverStatusPages)

    implementation(libs.ktor.clientCore)
    implementation(libs.ktor.clientCio)
    implementation(libs.ktor.clientContentNegotiation)
    implementation(libs.ktor.serverCachingHeaders)
    implementation(libs.ktor.serverConditionalHeaders)

    implementation(libs.ktor.serializationKotlinxJson)
    implementation(libs.kotlinx.serializationJson)
    implementation(libs.logback)
    implementation(libs.identity.jvm)
    implementation(projects.shared)

    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}
