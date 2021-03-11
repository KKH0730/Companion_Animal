package studio.seno.companion_animal

import android.app.Application
import org.koin.android.BuildConfig
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import studio.seno.companion_animal.module.repositoryModule
import studio.seno.companion_animal.module.useCaseModule
import studio.seno.companion_animal.module.viewModelModule

class KoinApplication : Application(){

    override fun onCreate() {
        super.onCreate()

        startKoin{
            if (BuildConfig.DEBUG) {
                androidLogger()
            } else {
                androidLogger(Level.ERROR)
            }
            androidContext(this@KoinApplication)
            koin.loadModules(listOf(repositoryModule, viewModelModule, useCaseModule))
            koin.createRootScope()
        }
    }
}