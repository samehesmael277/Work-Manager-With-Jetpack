package com.sameh.workmanager

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.sameh.workmanager.ui.theme.WorkManagerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import java.time.Duration
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalPermissionsApi::class)
    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WorkManagerTheme {
                CallCustomPeriodicWorker()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Composable
    private fun CallCustomPeriodicWorker() {
        val lifecycleOwner = LocalLifecycleOwner.current

        val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresCharging(true)
            .build()

        LaunchedEffect(key1 = Unit) {
            val workRequest = PeriodicWorkRequestBuilder<CustomPeriodicWorker>(
                repeatInterval = 1,
                repeatIntervalTimeUnit = TimeUnit.HOURS,
                flexTimeInterval = 15, // that mean our work will repeated after 45:60 minute because i create repeat after 1 hour and create flexTimeInterval 15 minute
                flexTimeIntervalUnit = TimeUnit.MINUTES
            ).setBackoffCriteria(
                backoffPolicy = BackoffPolicy.LINEAR,
                duration = Duration.ofSeconds(15)
            ).setConstraints(constraint)
                .build()

            val workManager = WorkManager.getInstance(applicationContext)
            //workManager.enqueue(workRequest)
            workManager.enqueueUniquePeriodicWork(
                "myWorkName",
                ExistingPeriodicWorkPolicy.KEEP, // if the old work still work that will ignore the new work
                workRequest
            )

            // observe our worker
            workManager.getWorkInfosForUniqueWorkLiveData("myWorkName")
                .observe(lifecycleOwner) {
                    it.forEach { workInfo ->
                        workInfo.state.toString().toLogD()
                    }
                }

            // cancel worker after 5 second
            delay(5000)
            workManager.cancelUniqueWork("myWorkName")
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    private fun HandlePermissionForWorkerWithNotification() {
        val permission =
            rememberPermissionState(permission = android.Manifest.permission.POST_NOTIFICATIONS)
        LaunchedEffect(key1 = Unit) {
            if (permission.status.isGranted)
                callCustomWorkerWithNotification()
            else
                permission.launchPermissionRequest()
        }
    }

    @SuppressLint("NewApi")
    private fun callCustomWorkerWithNotification() {
        val workRequest = OneTimeWorkRequestBuilder<CustomWorkerWithNotification>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .setBackoffCriteria(
                backoffPolicy = BackoffPolicy.LINEAR, // if i use EXPONENTIAL that mean when worker failed to get data ot error happened it will repeat first time after 15 sec and second time after 15*2= 30 sec and third time after 30*2=30 ..... until get the data
                duration = Duration.ofSeconds(15) // mean that when worker failed to get data ot error happened it will repeat after 15 sec and linear mean if still failed will work after failed by 15 sec
            )
            .build()
        WorkManager.getInstance(applicationContext).enqueue(workRequest)
    }

    @SuppressLint("NewApi")
    private fun callCustomWorker() {
        val workRequest = OneTimeWorkRequestBuilder<CustomWorker>()
            .setInitialDelay(Duration.ofSeconds(10)) // mean that when i run app it delay 10 sec then worker will work
            .setBackoffCriteria(
                backoffPolicy = BackoffPolicy.LINEAR, // if i use EXPONENTIAL that mean when worker failed to get data ot error happened it will repeat first time after 15 sec and second time after 15*2= 30 sec and third time after 30*2=30 ..... until get the data
                duration = Duration.ofSeconds(15) // mean that when worker failed to get data ot error happened it will repeat after 15 sec and linear mean if still failed will work after failed by 15 sec
            )
            .build()
        WorkManager.getInstance(applicationContext).enqueue(workRequest)
    }
}

fun String.toLogD(tag: String = "debuggingTag") {
    Log.d(tag, this)
}

fun String.toLogW(tag: String = "debuggingTag") {
    Log.w(tag, this)
}

fun String.toLogE(tag: String = "debuggingTag") {
    Log.e(tag, this)
}
