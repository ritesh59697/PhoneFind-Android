package com.ritesh.phonefind.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object WorkManagerScheduler {

    private const val SIM_CHECK_WORK_NAME = "PhoneFind_SimCheckWork"
    private const val LOCATION_WORK_NAME = "PhoneFind_LocationWork"

    fun schedulePeriodicJobs(context: Context) {
        val workManager = WorkManager.getInstance(context)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Periodic SIM check worker (15 minutes)
        val simCheckRequest = PeriodicWorkRequestBuilder<SimCheckWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            SIM_CHECK_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            simCheckRequest
        )

        // Periodic Location worker (5 minutes)
        val locationRequest = PeriodicWorkRequestBuilder<LocationWorker>(5, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            LOCATION_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            locationRequest
        )
    }

    fun cancelAllJobs(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(SIM_CHECK_WORK_NAME)
        workManager.cancelUniqueWork(LOCATION_WORK_NAME)
    }
}
