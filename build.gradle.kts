import java.math.BigDecimal

plugins {
    java
    jacoco
}

group = "dev.bottega"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(26)
    }
}

dependencies {
    val lombok = "org.projectlombok:lombok:1.18.48"
    // Lombok: @Builder on records (value objects). Runs as annotation processor for main + test sources.
    compileOnly(lombok)
    annotationProcessor(lombok)
    testCompileOnly(lombok)
    testAnnotationProcessor(lombok)

    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    // AssertJ: used by the migrated intermediate workshops (assertThat / Assertions).
    testImplementation("org.assertj:assertj-core:3.20.2")
    // JMH: `collections/Bench.java` uses @Benchmark. Compile-time annotation only (benchmarks are not run).
    testImplementation("org.openjdk.jmh:jmh-core:1.32")
    // Generate the JMH benchmark classes at compile time and provide the runtime generator
    // so `./gradlew jmh` can actually execute `collections/Bench`.
    testAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess:1.32")
    testRuntimeOnly("org.openjdk.jmh:jmh-generator-asm:1.32")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile> {
    // jdk.httpserver is a standard module not in java.se; add it so com.sun.net.httpserver resolves.
    // -restricted: FFM-style restricted APIs are intentional. -preview: preview features are intentional.
    options.compilerArgs.addAll(listOf("-Xlint:all,-restricted,-preview", "-parameters", "--add-modules=jdk.httpserver"))
    // Preview features + incubating Vector API.
    options.release.set(26)
    options.compilerArgs.addAll(listOf("--enable-preview", "--add-modules=jdk.incubator.vector"))
}

tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("--enable-native-access=ALL-UNNAMED", "--add-modules=jdk.httpserver")
    jvmArgs("--enable-preview", "--add-modules=jdk.incubator.vector")
    testLogging {
        events("passed", "failed", "skipped")
        showStandardStreams = false
    }
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}

// Quality gate: enforce 90%+ coverage on line/instruction/method counters.
tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.test)
    violationRules {
        rule {
            element = "BUNDLE"
            limit { counter = "LINE"; minimum = BigDecimal.valueOf(0.90) }
            limit { counter = "INSTRUCTION"; minimum = BigDecimal.valueOf(0.90) }
            limit { counter = "METHOD"; minimum = BigDecimal.valueOf(0.90) }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

// Runs the JMH microbenchmark for collections/Bench via `./gradlew jmh`.
// Override JMH args with -PjmhArgs="-f 1 -wi 3 -i 5 -r 1s -w 1s dev.bottega.streams.collections.Bench".
val jmhArgs: List<String> = (findProperty("jmhArgs") as? String)
    ?.split(' ')
    ?.filter { it.isNotBlank() }
    ?: listOf("-f", "1", "-wi", "5", "-i", "5", "-r", "1s", "-w", "1s", "dev.bottega.streams.collections.Bench")

tasks.register<JavaExec>("jmh") {
    group = "verification"
    description = "Runs the JMH microbenchmark for collections/Bench."
    dependsOn(tasks.testClasses)
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("org.openjdk.jmh.Main")
    args(jmhArgs)
}

// Runs every demo (incl. preview + incubator) with the required JVM flags.
tasks.register<JavaExec>("runDemos") {
    group = "application"
    description = "Runs every advanced-streams demo with the required preview/incubator/native-access flags."
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("dev.bottega.jdkfeatures.advstreams.DemoRunner")
    jvmArgs("--enable-preview", "--add-modules=jdk.incubator.vector,jdk.httpserver", "--enable-native-access=ALL-UNNAMED")
}
