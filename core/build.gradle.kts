plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(libs.versions.javaToolchain.get().toIntOrNull()?: 25)
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
}