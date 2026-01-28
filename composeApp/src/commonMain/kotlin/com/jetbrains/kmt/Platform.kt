package com.jetbrains.kmt

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform