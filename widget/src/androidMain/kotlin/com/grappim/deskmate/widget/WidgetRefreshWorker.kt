package com.grappim.deskmate.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.grappim.kit.logger.logcat
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

private const val PERIODIC_WORK_NAME = "widget-refresh"
private const val ONE_TIME_WORK_NAME = "widget-refresh-now"

/** WorkManager's minimum period. IMPLEMENTATION_PLAN.md §5. */
private const val PERIOD_MINUTES = 15L

/** Runs [WidgetRefresher], then re-renders every widget instance. */
class WidgetRefreshWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params),
    KoinComponent {

    private val refresher: WidgetRefresher by inject()

    override suspend fun doWork(): Result {
        logcat { "Widget refresh run" }
        refresher.refresh()
        DeskmateWidget().updateAll(applicationContext)
        // A failure keeps the old snapshot; the next period tries again, so no retry here.
        return Result.success()
    }

    companion object {
        /** `KEEP`: an app start does not reset the period of work that is already enqueued. */
        fun schedulePeriodic(context: Context) {
            val request = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(PERIOD_MINUTES, TimeUnit.MINUTES)
                .setConstraints(networkConnected)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(PERIODIC_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }

        /** One run now. `KEEP`: taps while a run is queued or running add no extra fetch. */
        fun refreshNow(context: Context) {
            val request = OneTimeWorkRequestBuilder<WidgetRefreshWorker>()
                .setConstraints(networkConnected)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(ONE_TIME_WORK_NAME, ExistingWorkPolicy.KEEP, request)
        }

        private val networkConnected = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
    }
}
