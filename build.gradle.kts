import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
	kotlin("jvm") version "2.2.21"
	kotlin("plugin.spring") version "2.2.21"
	id("org.springframework.boot") version "4.0.1"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.openapi.generator") version "7.10.0"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("tools.jackson.module:jackson-module-kotlin")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.1")

	// Required for the generated DTOs (validation and swagger annotations)
	implementation("jakarta.validation:jakarta.validation-api")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("io.swagger.core.v3:swagger-annotations:2.2.21")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-test-autoconfigure")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// 1. Configure the Generator Task
tasks.openApiGenerate {
	generatorName.set("kotlin-spring") // Optimized for Kotlin/Spring Boot
	inputSpec.set("$projectDir/src/main/resources/static/openapi.yaml")
	outputDir.set("$buildDir/generated/openapi")
	apiPackage.set("com.example.api")
	modelPackage.set("com.example.api.dto")

	typeMappings.set(mapOf(
		"date-time" to "OffsetDateTime",
		"double" to "BigDecimal",
	))

	importMappings.set(mapOf(
		"OffsetDateTime" to "java.time.OffsetDateTime",
		"BigDecimal" to "java.math.BigDecimal",
	))

	configOptions.set(mapOf(
		"dateLibrary" to "java8",
		"interfaceOnly" to "true",
		"useSpringBoot3" to "true",
		"useTags" to "true",
		"serializationLibrary" to "jackson",
		"enumPropertyNaming" to "UPPERCASE",
		"collectionType" to "list",
		"useBeanValidation" to "true"
	))
}

// 2. Add generated code to SourceSets
sourceSets {
	main {
		kotlin {
			srcDir("$buildDir/generated/openapi/src/main/kotlin")
		}
	}
}

// 3. Ensure code is generated before Kotlin compilation
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
	dependsOn(tasks.openApiGenerate)
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}