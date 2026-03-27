package com.bium.youngssoo.di

import com.bium.youngssoo.core.data.HttpClientFactory
import com.bium.youngssoo.core.data.repository.QuestionRepository
import com.bium.youngssoo.game.math.presentation.MathGameViewModel
import com.bium.youngssoo.game.vocab.presentation.VocabGameViewModel
import com.bium.youngssoo.game.hanja.presentation.HanjaGameViewModel
import com.bium.youngssoo.minigame.data.repository.MiniGameProgressRepository
import com.bium.youngssoo.minigame.presentation.MiniGameViewModel
import com.bium.youngssoo.reward.domain.RewardRepository
import com.bium.youngssoo.reward.presentation.RewardViewModel
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import com.bium.youngssoo.core.data.local.AppDatabase
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.HttpTimeout
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

expect val platformModule: Module

val sharedModules = module {
    single { get<AppDatabase>().userStatsDao() }
    single { get<AppDatabase>().questionDao() }
    single { get<AppDatabase>().miniGameProgressDao() }
    single { RewardRepository(get()) }
    single { MiniGameProgressRepository(get()) }

    // Firestore용 HttpClient
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(json = Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(HttpTimeout) {
                socketTimeoutMillis = 10000L
                requestTimeoutMillis = 10000L
            }
        }
    }

    // QuestionRepository에 HttpClient 연결
    single<Settings> { Settings() }
    single { QuestionRepository(get(), get(), get()) }

    viewModelOf(::MathGameViewModel)
    viewModelOf(::VocabGameViewModel)
    viewModelOf(::HanjaGameViewModel)
    single { MiniGameViewModel(get(), get(), null) }
    viewModelOf(::RewardViewModel)
}