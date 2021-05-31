package com.sarbaevartur.wifistreamscreen

import android.app.Application
import com.elvishew.xlog.flattener.ClassicFlattener
import com.elvishew.xlog.printer.file.FilePrinter
import com.elvishew.xlog.printer.file.clean.FileLastModifiedCleanStrategy
import com.sarbaevartur.wifistreamscreen.data.settings.SettingsReadOnly
import com.sarbaevartur.wifistreamscreen.di.baseKoinModule
import com.sarbaevartur.wifistreamscreen.logging.DateSuffixFileNameGenerator
import com.sarbaevartur.wifistreamscreen.logging.getLogFolder
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

abstract class BaseApp : Application() {

    protected val settingsReadOnly: SettingsReadOnly by inject()
    protected val filePrinter: FilePrinter by lazy {
        FilePrinter.Builder(getLogFolder())
            .fileNameGenerator(DateSuffixFileNameGenerator(this@BaseApp.hashCode().toString()))
            .cleanStrategy(FileLastModifiedCleanStrategy(86400000)) // One day
            .flattener(ClassicFlattener())
            .build()
    }

    abstract fun initLogger()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@BaseApp)
            modules(baseKoinModule)
        }


        initLogger()
    }
}