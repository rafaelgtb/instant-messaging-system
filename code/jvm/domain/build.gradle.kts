dependencies {
    // To use Named annotation for Dependency Injection
    implementation(libs.jakarta.inject.api)

    // To get password encode
    api(libs.spring.security.core)

    // To use Kotlin specific date and time functions
    implementation(libs.kotlinx.datetime)
}
