package com.jovanmosurovic.personalfinancemanager.data.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.jovanmosurovic.personalfinancemanager.MainActivity
import com.jovanmosurovic.personalfinancemanager.R
import java.time.LocalDate
import java.time.YearMonth
import java.util.concurrent.TimeUnit

internal object StatementReminderScheduler {
    private const val WORK_NAME = "monthly_otp_statement_reminder"

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<StatementReminderWorker>(
            1,
            TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}

internal class StatementReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams) {
    override fun doWork(): Result {
        val preferences = applicationContext.getSharedPreferences(
            PREFERENCES_NAME,
            Context.MODE_PRIVATE
        )
        if (!preferences.getBoolean(KEY_STATEMENT_REMINDER_ENABLED, false)) {
            return Result.success()
        }

        val today = LocalDate.now()
        if (today.dayOfMonth != 1) return Result.success()

        val currentMonth = YearMonth.from(today).toString()
        if (preferences.getString(KEY_LAST_REMINDER_MONTH, null) == currentMonth) {
            return Result.success()
        }

        if (postNotification()) {
            preferences.edit {
                putString(KEY_LAST_REMINDER_MONTH, currentMonth)
            }
        }
        return Result.success()
    }

    private fun postNotification(): Boolean {
        if (
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }

        val notificationManager = applicationContext.getSystemService(
            NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                applicationContext.getString(R.string.statement_reminder_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )

        val contentIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle(applicationContext.getString(R.string.statement_reminder_title))
            .setContentText(applicationContext.getString(R.string.statement_reminder_message))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        return NotificationManagerCompat.from(applicationContext).run {
            if (!areNotificationsEnabled()) return false
            notify(NOTIFICATION_ID, notification)
            true
        }
    }

    private companion object {
        const val CHANNEL_ID = "statement_reminders"
        const val NOTIFICATION_ID = 1001
        const val PREFERENCES_NAME = "finance_preferences"
        const val KEY_STATEMENT_REMINDER_ENABLED = "statement_reminder_enabled"
        const val KEY_LAST_REMINDER_MONTH = "statement_reminder_last_month"
    }
}
