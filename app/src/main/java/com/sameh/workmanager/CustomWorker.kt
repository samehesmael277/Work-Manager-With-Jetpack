package com.sameh.workmanager

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.sameh.workmanager.data.DemoApi
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.net.UnknownHostException

@HiltWorker
class CustomWorker @AssistedInject constructor(
    private val api: DemoApi,
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        return try {
            val response = api.getPosts()
            if (response.isSuccessful) {
                "worker Success".toLogD()
                "data: ${response.body()}".toLogD()
                Result.success()
            } else {
                "worker Retrying...".toLogW()
                Result.retry()
            }
        } catch (e: UnknownHostException) { // if happened network error (no internet connect)
            "worker Retrying...".toLogW()
            Result.retry()
        } catch (e: Exception) {
            "worker error".toLogE()
            Result.failure(Data.Builder().putString("error: ", e.toString()).build())
        }
    }
}