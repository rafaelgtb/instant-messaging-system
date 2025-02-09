dependencies {
    // Module dependencies
    implementation(project(":domain"))

    // To get the DI annotation
    implementation(libs.jakarta.inject.api)

    // To use Kotlin specific date and time functions
    api(libs.kotlinx.datetime)
}
