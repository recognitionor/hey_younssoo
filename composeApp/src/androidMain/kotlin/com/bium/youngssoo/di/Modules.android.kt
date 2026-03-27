package com.bium.youngssoo.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.bium.youngssoo.core.data.local.AppDatabase
import com.bium.youngssoo.getPlatformContext
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<AppDatabase> {
        val appCtx = getPlatformContext() as Context
        val dbFile = appCtx.getDatabasePath("youngsso.db")
        Room.databaseBuilder<AppDatabase>(
            context = appCtx,
            name = dbFile.absolutePath
        )
        .addMigrations(
            AppDatabase.MIGRATION_1_3,
            AppDatabase.MIGRATION_2_3,
            AppDatabase.MIGRATION_3_4
        )
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
    }
}