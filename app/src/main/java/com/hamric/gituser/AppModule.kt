package com.hamric.gituser

import org.koin.dsl.module

val appModule = module {
    single { GreetingService() }
}

class GreetingService {
    fun greet(name: String): String = "Hello, $name! From Koin for Testing"
}