package com.bium.youngssoo.di

import com.bium.youngssoo.di.platformModule
import com.bium.youngssoo.di.sharedModules
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(sharedModules, platformModule)
    }
}