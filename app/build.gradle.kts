import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    id(Plugins.androidApp)
    id(Plugins.googleServices)
    kotlin(Plugins.kotlinAndroid)
    kotlin(Plugins.kotlinKapt)
    id(Plugins.androidxNavSafeargs)
    kotlin(Plugins.kotlinAndroidExtensions)
}

fun getPassword(): String  {
    var pass = ""
    val props = Properties()
    val propFile = file("../signing/apikey.properties")

    if (propFile.canRead()) {
        props.load(project.rootProject.file(propFile).inputStream())

        pass = props.getProperty("STORE_PASSWORD") ?: ""
    }

    return pass
}

android {

    signingConfigs {
        create("release") {

            storeFile = file("../signing/location2.keystore")
            storePassword = getPassword()
            keyAlias = "location_key"
            keyPassword = getPassword()

        }
    }
    compileSdkVersion(Config.compileSdk)

    defaultConfig {
        applicationId = "com.rosalynbm.locationreminder"
        minSdkVersion (Config.minSdk)
        targetSdkVersion (Config.targetSdk)
        versionCode = Config.versionCode
        versionName = Config.versionName
        testInstrumentationRunner = Config.testInstrumentationRunner

        resConfigs("en") //To limit the lamguages available from Firebase translations
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isDebuggable = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions { jvmTarget = JavaVersion.VERSION_1_8.toString() }

    buildFeatures {
        dataBinding = true
    }

    testOptions.unitTests.apply {
        isIncludeAndroidResources = true
        isReturnDefaultValues = true
    }

/*    dataBinding {
        enabled = true
        enabledForTests = true
    }*/

}

dependencies {
    implementation(fileTree(mapOf("dir" to "Dependencies", "include" to listOf("*.jar"))))

    /*implementation platform('com.google.firebase:firebase-bom:26.6.0')
    // Add the dependency for the Firebase SDK for Google Analytics
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation 'com.google.firebase:firebase-analytics-ktx'*/
    // App dependencies
    implementation(Dependencies.Androidx.appCompact)
    implementation(Dependencies.Androidx.legacySupport)
    implementation(Dependencies.Androidx.annotations)

    implementation(Dependencies.Androidx.cardView)
    implementation(Dependencies.Google.material)
    implementation(Dependencies.Androidx.recyclerView)
    implementation(Dependencies.Androidx.constraintLayout)
    implementation(Dependencies.Androidx.coreKtx)

    implementation(Dependencies.Google.gson)

    // Architecture Components
    //Navigation dependencies
    implementation(Dependencies.Androidx.appCompact)
    kapt(Dependencies.Androidx.lifecycleCompiler)
    implementation(Dependencies.Androidx.lifecycleExt)
    implementation(Dependencies.Androidx.lifecycleViewModel)
    implementation(Dependencies.Androidx.lifecycleLivedata)
    implementation(Dependencies.Androidx.navigationFragment)
    implementation(Dependencies.Androidx.navigationUi)
    implementation(Dependencies.Androidx.espressoIdlingResource)

    //Room dependencies
    implementation(Dependencies.Androidx.room)
    implementation(Dependencies.Androidx.roomRuntime)
    kapt(Dependencies.Androidx.roomCompiler)

    //Coroutines Dependencies
    implementation(Dependencies.Kotlin.coroutine)

    //Koin
    implementation(Dependencies.Koin.android)
    implementation(Dependencies.Koin.viewModel)

    implementation(Dependencies.firebaseUi)
    // Maps & Geofencing
    implementation(Dependencies.PlayServices.location)
    implementation(Dependencies.PlayServices.maps)
    implementation(Dependencies.timber)
    // Once https://issuetracker.google.com/127986458 is fixed this can be testImplementation
    implementation(Dependencies.Androidx.androidxFragmentTesting)
    implementation(Dependencies.Androidx.testCore)
    implementation(Dependencies.Androidx.fragment)


    // Dependencies for local unit tests
    testImplementation(Dependencies.junit)
    testImplementation(Dependencies.hamcrest)
    testImplementation(Dependencies.Androidx.aarchCoreTesting)
    testImplementation(Dependencies.Kotlin.coroutine)
    testImplementation(Dependencies.Kotlin.coroutineTest)
    testImplementation(Dependencies.Roboelectric.roboelectric)
    testImplementation(Dependencies.Google.truth)
    // Required if you want to use Mockito for unit tests
    testImplementation(Dependencies.mockitoCore)

    // AndroidX Test - JVM testing
    testImplementation(Dependencies.Androidx.testCoreKtx)
    testImplementation(Dependencies.Androidx.testJunit)
    testImplementation(Dependencies.Androidx.testRules)

    // AndroidX Test - Instrumented testing
    androidTestImplementation(Dependencies.Androidx.testCoreKtx)
    androidTestImplementation(Dependencies.Androidx.testJunit)
    androidTestImplementation(Dependencies.Kotlin.coroutineTest)
    androidTestImplementation(Dependencies.Androidx.testRules)
    androidTestImplementation(Dependencies.Androidx.roomTesting)
    androidTestImplementation(Dependencies.Androidx.aarchCoreTesting)
    androidTestImplementation(Dependencies.Roboelectric.annotations)
    androidTestImplementation(Dependencies.Androidx.espressoCore)
    androidTestImplementation(Dependencies.Androidx.espressoContrib)
    androidTestImplementation(Dependencies.Androidx.espressoIntents)
    androidTestImplementation(Dependencies.Androidx.espressoIdlingConcurrent)
    androidTestImplementation(Dependencies.junit)

    //androidTestImplementation(Dependencies.mockitoAndroid)

    androidTestImplementation(Dependencies.mockitoCore)
    // Commented because I was getting this error: https://github.com/InsertKoinIO/koin/issues/287
    //androidTestImplementation(Dependencies.linkedinDexmakerMockito)
    androidTestImplementation(Dependencies.Koin.test) { exclude ("org.mockito", "mockito")}


}