package com.sameh.workmanager

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.sameh.workmanager.data.DemoApi
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: CustomWorkerFactory

    @Inject
    lateinit var workerWithNotificationFactory :CustomWorkerWithNotificationFactory

    @Inject
    lateinit var customPeriodicWorkerFactory :CustomPeriodicWorkerFactory

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setWorkerFactory(customPeriodicWorkerFactory)
            .build()
}

class CustomWorkerFactory @Inject constructor(private val api: DemoApi) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker = CustomWorker(api, appContext, workerParameters)
}

class CustomWorkerWithNotificationFactory @Inject constructor() : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker = CustomWorkerWithNotification(appContext, workerParameters)
}

class CustomPeriodicWorkerFactory @Inject constructor() : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker = CustomPeriodicWorker(appContext, workerParameters)
}