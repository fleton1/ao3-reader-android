package com.ao3reader

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.ao3reader.workers.WorkManagerHelper
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AO3ReaderApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var workManagerHelper: WorkManagerHelper

    override fun onCreate() {
        super.onCreate()

        // Schedule periodic update checking for followed works
        workManagerHelper.schedulePeriodicUpdateCheck()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
