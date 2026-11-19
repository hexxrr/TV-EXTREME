plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "com.tvextreme.optimizer"
    compileSdk = 35
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    defaultConfig { applicationId = "com.tvextreme.optimizer"; minSdk = 23; targetSdk = 28; versionCode = 1; versionName = "1.0" }
}
