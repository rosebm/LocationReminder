// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {

    repositories {
        google() // Google's Maven repository
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath ("androidx.navigation:navigation-safe-args-gradle-plugin:2.3.3")
        //classpath ("androidx.navigation:navigation-safe-args-gradle-plugin:${ext["navigationVersion"]}")
        classpath ("com.google.gms:google-services:${Versions.googleServices}")
        classpath ("com.android.tools.build:gradle:${Versions.gradle}")
        classpath ("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.21")
        //classpath ("org.jetbrains.kotlin:kotlin-gradle-plugin:${ext["kotlinVersion"]}")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        mavenCentral()
        jcenter()
        google()
      /*  maven {
            url "https://jitpack.io"
        }*/
    }
}

tasks.register("clean", Delete::class) {
    delete (rootProject.buildDir)
}