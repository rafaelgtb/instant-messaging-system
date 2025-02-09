dependencies {
    // Module dependencies
    implementation(project(":service"))

    // To use Spring MVC and the Servlet API
    implementation(libs.spring.webmvc)
    implementation(libs.jakarta.servlet.api)

    // To use SLF4J
    implementation(libs.slf4j.api)
}
