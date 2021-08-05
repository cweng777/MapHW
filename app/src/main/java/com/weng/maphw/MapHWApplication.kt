package com.weng.maphw

import android.app.Application
import com.weng.maphw.di.apiModule
import com.weng.maphw.di.retrofitModule
import com.weng.maphw.di.userModule
import com.weng.maphw.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin

class MapHWApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            if (BuildConfig.DEBUG) {
                androidLogger()
            }
            androidContext(this@MapHWApplication)
        }
        loadKoinModules(listOf(
            retrofitModule,
            apiModule,
            userModule,
            viewModelModule
        ))
    }
}