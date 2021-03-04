plugins {
    id(Plugins.androidApp)
    id(Plugins.googleServices)
    kotlin(Plugins.kotlinAndroid)
    kotlin(Plugins.kotlinKapt)
    id(Plugins.androidxNavSafeargs)
    kotlin(Plugins.kotlinAndroidExtensions)
}

fun Project.propertyOrEmpty(name: String): String {
    return findProperty(name) as String? ?: ""
}

android {

    signingConfigs {
        create("release") {

            storeFile = file("../signing/apikey.keystore")
            storePassword = propertyOrEmpty("STORE_PASSWORD")
            keyAlias = propertyOrEmpty("location_key")
            keyPassword = propertyOrEmpty("KEY_PASSWORD")
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
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions { jvmTarget = JavaVersion.VERSION_1_8.toString() }

    /*testOptions.unitTests {
        includeAndroidResources = true
        returnDefaultValues = true
    }

    //dataBinding {
    //    enabled = true
    //    enabledForTests = true
    //}
    buildFeatures {
        dataBinding true
    }*/

}

dependencies {
    implementation(fileTree(mapOf("dir" to "Dependencies", "include" to listOf("*.jar"))))

    /*implementation platform('com.google.firebase:firebase-bom:26.6.0')
    // Add the dependency for the Firebase SDK for Google Analytics
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation 'com.google.firebase:firebase-analytics-ktx'*/
    // App dependencies
    implementation(Dependencies.Androidx.appCompact) //androidx.appcompat:appcompat:$appCompatVersion"
    implementation(Dependencies.Androidx.legacySupport)// "androidx.legacy:legacy-support-v4:$androidXLegacySupport"
    implementation(Dependencies.Androidx.annotations) // "androidx.annotation:annotation:$androidXAnnotations"

    implementation(Dependencies.Androidx.cardView) // "androidx.cardview:cardview:$cardVersion"
    implementation(Dependencies.Google.material) // "com.google.android.material:material:$materialVersion"
    implementation(Dependencies.Androidx.recyclerView) //"androidx.recyclerview:recyclerview:$recyclerViewVersion"
    implementation(Dependencies.Androidx.constraintLayout) // "androidx.constraintlayout:constraintlayout:$constraintVersion"

    implementation(Dependencies.Google.gson) // 'com.google.code.gson:gson:2.8.5'

    // Architecture Components
    //Navigation dependencies
    implementation(Dependencies.Androidx.appCompact) //'androidx.appcompat:appcompat:1.2.0'
    kapt(Dependencies.Androidx.lifecycleCompiler) //"androidx.lifecycle:lifecycle-compiler:$archLifecycleVersion"
    implementation(Dependencies.Androidx.lifecycleExt) //"androidx.lifecycle:lifecycle-extensions:$archLifecycleVersion"
    implementation(Dependencies.Androidx.lifecycleViewModel) //"androidx.lifecycle:lifecycle-viewmodel-ktx:$archLifecycleVersion"
    implementation(Dependencies.Androidx.lifecycleLivedata) //"androidx.lifecycle:lifecycle-livedata-ktx:$archLifecycleVersion"
    implementation(Dependencies.Androidx.navigationFragment) //"androidx.navigation:navigation-fragment-ktx:$navigationVersion"
    implementation(Dependencies.Androidx.navigationUi) //"androidx.navigation:navigation-ui-ktx:$navigationVersion"
    implementation(Dependencies.Androidx.espressoIdlingResource) // "androidx.test.espresso:espresso-idling-resource:$espressoVersion"

    //Room dependencies
    implementation(Dependencies.Androidx.room) //"androidx.room:room-ktx:$roomVersion"
    implementation(Dependencies.Androidx.roomRuntime) //"androidx.room:room-runtime:$roomVersion"
    kapt(Dependencies.Androidx.roomCompiler) //"androidx.room:room-compiler:$roomVersion"

    //Coroutines Dependencies
    implementation(Dependencies.Kotlin.coroutine) //"org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion"

    //Koin
    implementation(Dependencies.Koin.android) //"org.koin:koin-android:$koinVersion"
    implementation(Dependencies.Koin.viewModel) //"org.koin:koin-androidx-viewmodel:$koinVersion"


    // Dependencies for local unit tests
    testImplementation(Dependencies.junit) //"junit:junit:$junitVersion"
    testImplementation(Dependencies.hamcrest) // "org.hamcrest:hamcrest-all:$hamcrestVersion"
    testImplementation(Dependencies.Androidx.aarchCoreTesting) // "androidx.arch.core:core-testing:$archTestingVersion"
    testImplementation(Dependencies.Kotlin.coroutine)// "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion"
    testImplementation(Dependencies.Kotlin.coroutineTest) // "org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion"
    testImplementation(Dependencies.Roboelectric.roboelectric) //"org.robolectric:robolectric:$robolectricVersion"
    testImplementation(Dependencies.Google.truth) //"com.google.truth:truth:$truthVersion"
    testImplementation(Dependencies.mockitoCore) //"org.mockito:mockito-core:$mockitoVersion"

    // AndroidX Test - JVM testing
    testImplementation(Dependencies.Androidx.testCoreKtx) //"androidx.test:core-ktx:$androidXTestCoreVersion"
    testImplementation(Dependencies.Androidx.testJunit) //"androidx.test.ext:junit-ktx:$androidXTestExtKotlinRunnerVersion"
    testImplementation(Dependencies.Androidx.testRules) //"androidx.test:rules:$androidXTestRulesVersion"

    // AndroidX Test - Instrumented testing
    androidTestImplementation(Dependencies.Androidx.testCoreKtx) //"androidx.test:core-ktx:$androidXTestCoreVersion"
    androidTestImplementation(Dependencies.Androidx.testJunit) //"androidx.test.ext:junit-ktx:$androidXTestExtKotlinRunnerVersion"
    androidTestImplementation(Dependencies.Kotlin.coroutineTest) //"org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion"
    androidTestImplementation(Dependencies.Androidx.testRules) //"androidx.test:rules:$androidXTestRulesVersion"
    androidTestImplementation(Dependencies.Androidx.roomTesting) //"androidx.room:room-testing:$roomVersion"
    androidTestImplementation(Dependencies.Androidx.aarchCoreTesting) //"androidx.arch.core:core-testing:$archTestingVersion"
    androidTestImplementation(Dependencies.Roboelectric.annotations) //"org.robolectric:annotations:$robolectricVersion"
    androidTestImplementation(Dependencies.Androidx.espressoCore) //"androidx.test.espresso:espresso-core:$espressoVersion"
    androidTestImplementation(Dependencies.Androidx.espressoContrib) //"androidx.test.espresso:espresso-contrib:$espressoVersion"
    androidTestImplementation(Dependencies.Androidx.espressoIntents) //"androidx.test.espresso:espresso-intents:$espressoVersion"
    androidTestImplementation(Dependencies.Androidx.espressoIdlingConcurrent) //"androidx.test.espresso.idling:idling-concurrent:$espressoVersion"
    androidTestImplementation(Dependencies.junit) //"junit:junit:$junitVersion"
    // Once https://issuetracker.google.com/127986458 is fixed this can be testImplementation
    implementation(Dependencies.Androidx.androidxFragmentTesting) //"androidx.fragment:fragment-testing:$fragmentVersion"
    implementation(Dependencies.Androidx.testCore) //"androidx.test:core:$androidXTestCoreVersion"
    implementation(Dependencies.Androidx.fragment) //"androidx.fragment:fragment:$fragmentVersion"
    androidTestImplementation(Dependencies.mockitoCore) //"org.mockito:mockito-core:$mockitoVersion"
    androidTestImplementation(Dependencies.linkedinDexmakerMockito) //"com.linkedin.dexmaker:dexmaker-mockito:$dexMakerVersion"
    androidTestImplementation(Dependencies.Koin.test) { exclude ("org.mockito", "mockito")}

    //Maps & Geofencing
    implementation(Dependencies.PlayServices.location)  //"com.google.android.gms:play-services-location:$playServicesVersion"
    implementation(Dependencies.PlayServices.maps) //"com.google.android.gms:play-services-maps:$playServicesVersion"
}

apply(plugin = "com.google.gms.google-services")
