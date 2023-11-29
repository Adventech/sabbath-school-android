/*
 * Copyright (c) 2023. Adventech <info@adventech.io>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package ss.workers.impl.workers

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.withContext
import ss.foundation.coroutines.DispatcherProvider
import ss.lessons.api.ContentSyncProvider
import app.ss.translations.R as L10n
import com.cryart.design.R as DesignR

@HiltWorker
internal class SyncQuarterlyWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val contentSyncProvider: ContentSyncProvider,
    private val dispatcherProvider: DispatcherProvider,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val index = workerParams.inputData.getString(QUARTERLY_INDEX) ?: return Result.failure()
        val result = withContext(dispatcherProvider.io) {
            contentSyncProvider.syncQuarterly(index)
        }
        return if (result.isSuccess) Result.success() else Result.failure()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(NOTIFICATION_ID, createNotification())
    }

    private fun createNotification(): Notification {
        val notificationManager = NotificationManagerCompat.from(appContext)
        val channel = NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setName(appContext.getString(L10n.string.ss_channel_offline_sync))
            .build()
        notificationManager.createNotificationChannel(channel)

        val title = appContext.getString(L10n.string.ss_quarterly_downloading)
        return NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setContentTitle(title)
            .setTicker(title)
            .setSmallIcon(DesignR.drawable.ic_stat_notification)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    companion object {
        const val QUARTERLY_INDEX = "arg:quarterly_index"
        val uniqueWorkName: String = SyncQuarterlyWorker::class.java.simpleName
        private const val NOTIFICATION_ID = 123
        private const val CHANNEL_ID = "ss_notification_channel_offline_sync"
    }
}
