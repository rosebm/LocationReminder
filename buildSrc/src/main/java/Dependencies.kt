/**
 * Dependency coordinates.
 */
object Dependencies {

    object Androidx {
        const val annotations = "androidx.annotation:annotation:${Versions.androidXAnnotations}"
        // Architecture Components
        //Navigation dependencies
        const val appCompact = "androidx.appcompat:appcompat:${Versions.appCompat}"
        const val aarchCoreTesting = "androidx.arch.core:core-testing:${Versions.archTesting}"
        const val cardView = "androidx.cardview:cardview:${Versions.cardView}"
        const val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.androidxConstraint}"
        // Android KTX for framework API
        const val coreKtx = "androidx.core:core-ktx:${Versions.androidXCoreKtx}"
        const val espressoCore = "androidx.test.espresso:espresso-core:${Versions.espresso}"
        const val espressoContrib = "androidx.test.espresso:espresso-contrib:${Versions.espresso}"
        const val espressoIdlingConcurrent = "androidx.test.espresso.idling:idling-concurrent:${Versions.espresso}"
        const val espressoIdlingResource = "androidx.test.espresso:espresso-idling-resource:${Versions.espresso}"
        const val espressoIntents = "androidx.test.espresso:espresso-intents:${Versions.espresso}"
        const val fragment = "androidx.fragment:fragment:${Versions.fragment}"
        // Once https://issuetracker.google.com/127986458 is fixed this can be testImplementation
        const val androidxFragmentTesting = "androidx.fragment:fragment-testing:${Versions.fragment}"
        const val legacySupport = "androidx.legacy:legacy-support-v4:${Versions.androidXLegacySupport}"
        const val lifecycleCompiler = "androidx.lifecycle:lifecycle-compiler:${Versions.archLifecycle}"
        const val lifecycleExt = "androidx.lifecycle:lifecycle-extensions:${Versions.archLifecycle}"
        const val lifecycleViewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.archLifecycle}"
        const val lifecycleLivedata = "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.archLifecycle}"
        const val navigationFragment = "androidx.navigation:navigation-fragment-ktx:${Versions.androidXNavigation}"
        const val navigationUi = "androidx.navigation:navigation-ui-ktx:${Versions.androidXNavigation}"
        const val recyclerView = "androidx.recyclerview:recyclerview:${Versions.recyclerView}"
        //Room dependencies
        const val room = "androidx.room:room-ktx:${Versions.room}"
        const val roomCompiler = "androidx.room:room-compiler:${Versions.room}"
        const val roomRuntime = "androidx.room:room-runtime:${Versions.room}"
        const val roomTesting = "androidx.room:room-testing:${Versions.room}"
        // AndroidX Test - Instrumented testing
        const val testCore = "androidx.test:core:${Versions.androidXTestCore}"
        const val testCoreKtx = "androidx.test:core-ktx:${Versions.androidXTestCore}"
        const val testJunit = "androidx.test.ext:junit-ktx:${Versions.androidXTestExtKotlinRunner}"
        const val testRules = "androidx.test:rules:${Versions.androidXTestRules}"
    }

    object Google {
        const val gson = "com.google.code.gson:gson:${Versions.googleGson}"
        const val material = "com.google.android.material:material:${Versions.material}"
        const val truth = "com.google.truth:truth:${Versions.truth}"
    }

    const val hamcrest = "org.hamcrest:hamcrest-all:${Versions.hamcrest}"
    // Dependencies for local unit tests
    const val junit = "junit:junit:${Versions.junit}"
    const val firebaseUi = "com.firebaseui:firebase-ui-auth:${Versions.firebaseUi}"

    //Koin
    object Koin {
        const val android = "org.koin:koin-android:${Versions.koin}"
        const val test = "org.koin:koin-test:${Versions.koin}"
        const val viewModel = "org.koin:koin-androidx-viewmodel:${Versions.koin}"
    }

    object Kotlin {
        //Coroutines Dependencies
        const val coroutine = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"
        const val coroutineTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutines}"
    }

    const val linkedinDexmakerMockito = "com.linkedin.dexmaker:dexmaker-mockito:${Versions.dexMakerMockito}"
    const val mockitoCore = "org.mockito:mockito-core:${Versions.mockito}"
    const val mockitoAndroid = "org.mockito:mockito-android:${Versions.mockito}"

    object PlayServices {
        //Maps & Geofencing
        const val location = "com.google.android.gms:play-services-location:${Versions.playServices}"
        const val maps = "com.google.android.gms:play-services-maps:${Versions.playServices}"
    }


    object Roboelectric {
        const val roboelectric = "org.robolectric:robolectric:${Versions.robolectric}"
        const val annotations = "org.robolectric:annotations:${Versions.robolectric}"
    }

    const val timber = "com.jakewharton.timber:timber:${Versions.timber}"
}