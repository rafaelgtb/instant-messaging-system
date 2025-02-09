plugins {
    // Kotlin plugins
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.kotlin.spring) apply false

    // Spring plugins
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.dependency.management) apply false

    // Linting plugin
    alias(libs.plugins.ktlint) apply false

    // Test logging plugin
    alias(libs.plugins.test.logger) apply false
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    group = "pt.isel"
    version = "0.0.1-SNAPSHOT"

    repositories { mavenCentral() }

    // kotlin { jvmToolchain(21) }
    configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> { jvmToolchain(21) }

    dependencies { "implementation"(kotlin("test")) }

    tasks.withType<Test> { useJUnitPlatform() }
}
