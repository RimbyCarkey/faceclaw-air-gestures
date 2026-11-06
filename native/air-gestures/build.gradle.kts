import org.jetbrains.kotlin.gradle.dsl.JvmTarget
plugins { id("com.android.library") version "8.13.2"; id("org.jetbrains.kotlin.android") version "2.4.20" }
android {
 namespace = "dev.kb896.airgestures"; compileSdk = 36
 defaultConfig { minSdk = 33; consumerProguardFiles("consumer-rules.pro"); ndk { abiFilters += "arm64-v8a" } }
 sourceSets["main"].apply {
  manifest.srcFile("src/main/AndroidManifest.xml")
  java.srcDir("build/upstream/app/src/main/java")
  res.srcDir("../../vendor/air-gestures/app/src/main/res")
  assets.srcDir("../../vendor/air-gestures/app/src/main/assets")
  jniLibs.srcDir("jniLibs")
 }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
kotlin { jvmToolchain(21); compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }
