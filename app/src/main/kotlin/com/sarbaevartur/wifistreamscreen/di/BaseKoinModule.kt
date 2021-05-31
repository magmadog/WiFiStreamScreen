package com.sarbaevartur.wifistreamscreen.di

import com.elvishew.xlog.XLog
import com.ironz.binaryprefs.BinaryPreferencesBuilder
import com.ironz.binaryprefs.Preferences
import com.sarbaevartur.wifistreamscreen.data.settings.Settings
import com.sarbaevartur.wifistreamscreen.data.settings.SettingsImpl
import com.sarbaevartur.wifistreamscreen.data.settings.SettingsReadOnly
import com.sarbaevartur.wifistreamscreen.service.helper.NotificationHelper
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.bind
import org.koin.dsl.module

val baseKoinModule = module {

    single<Preferences> {
        BinaryPreferencesBuilder(androidApplication())
            .supportInterProcess(true)
            .exceptionHandler { ex -> XLog.e(ex) }
            .build()
    }

    single<Settings> { SettingsImpl(get()) } bind SettingsReadOnly::class

    single { NotificationHelper(androidApplication()) }
}