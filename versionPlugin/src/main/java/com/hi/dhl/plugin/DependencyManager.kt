package com.hi.dhl.plugin

/**
 * <pre>
 *     author: dhl
 *     date  : 2020/6/15
 *     desc  : 如果数量少的话，放在一个类里面就可以，如果数量多的话，可以拆分为多个类
 * </pre>
 */

object Versions {
    val retrofit = "2.3.0"
    val appcompat = "1.1.0"
    val coreKtx = "1.3.0"
    val constraintlayout = "1.1.3"

    val kotlin = "1.3.72"
    val koin = "2.1.5"

    val junit = "4.12"
    val junitExt = "1.1.1"
    val espressoCore = "3.2.0"
}

object AndroidX {
    val appcompat = "androidx.appcompat:appcompat:${Versions.appcompat}"
    val coreKtx = "androidx.core:core-ktx:${Versions.coreKtx}"
    val constraintlayout =
        "androidx.constraintlayout:constraintlayout:${Versions.constraintlayout}"
}

object kotlinLib {
    val stdlibJdk7 = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}"
    val stdlibJdk8 = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlin}"
    val test = "org.jetbrains.kotlin:kotlin-test-junit:${Versions.kotlin}"
    val plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
}

object Koin {
    val core = "org.koin:koin-core:${Versions.koin}"
    val viewmodel = "org.koin:koin-androidx-viewmodel:${Versions.koin}"
}

object Depend {
    val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    val junit = "junit:junit:${Versions.junit}"
    val androidTestJunit = "androidx.test.ext:junit:${Versions.junitExt}"
    val espressoCore = "androidx.test.espresso:espresso-core:${Versions.espressoCore}"
}

