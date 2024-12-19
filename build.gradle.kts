// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.1.4" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false
    kotlin("kapt") version "1.9.23"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("com.android.test") version "8.1.4" apply false
}