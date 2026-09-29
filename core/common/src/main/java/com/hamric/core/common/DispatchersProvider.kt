package com.hamric.core.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

interface DispatchersProvider {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
}

class DefaultDispatchersProvider : DispatchersProvider {
    override val io = Dispatchers.IO
    override val default = Dispatchers.Default
    override val main = Dispatchers.Main
}