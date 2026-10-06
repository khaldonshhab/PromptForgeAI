plugins { id("com.android.application") }
android {
    namespace="com.promptforge.ai"
    compileSdk=37
    defaultConfig {
        applicationId="com.promptforge.ai"
        minSdk=24
        targetSdk=37
        versionCode=1
        versionName="1.0.0"
    }
    buildTypes {
        getByName("debug") { isMinifyEnabled=false }
        getByName("release") { isMinifyEnabled=false }
    }
}
dependencies {
    implementation("com.android.billingclient:billing:9.1.0")
}
