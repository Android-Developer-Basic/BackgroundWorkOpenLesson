package com.example.backgroundwork

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.backgroundwork.ui.theme.BackgroundWorkTheme
import java.util.Calendar
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BackgroundWorkTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Spacer(modifier = Modifier.weight(1f))

                        Button("Start Work Manager") {
                            val constraints = Constraints.Builder()
                                .setRequiresCharging(false)
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .setRequiresBatteryNotLow(true)
                                .setRequiresStorageNotLow(true)
                                .build()

                            val worker = OneTimeWorkRequest.Builder(BackgroundWorkManager::class.java)
                                .setConstraints(constraints)
                                .build()

                            WorkManager.getInstance(applicationContext)
                                .enqueueUniqueWork(
                                    "123",
                                    ExistingWorkPolicy.REPLACE,
                                    worker
                                )
                        }

                        Spacer(modifier = Modifier.padding(top = 20.dp))

                        Button("Start Job Scheduler") {
                            val componentName = ComponentName(applicationContext, BackgroundJob::class.java)
                            val jobInfo = JobInfo.Builder(1, componentName)
                                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                                .setPersisted(true)
                                .setPeriodic((15 * 60 * 1000).toLong())
                                .build()

                            val jobScheduler = getSystemService(JOB_SCHEDULER_SERVICE) as? JobScheduler
                            jobScheduler?.let {
                                val result = jobScheduler.schedule(jobInfo)
                                if (result == JobScheduler.RESULT_SUCCESS) {
                                    Log.d("BackgroundJob", "Job scheduled successfully")
                                } else {
                                    Log.d("BackgroundJob", "Job scheduling failed")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.padding(top = 20.dp))

                        Button("Start Foreground Service") {
                            val intent = Intent(applicationContext, ForegroundService::class.java)
                            val bundle = Bundle()
                            intent.putExtras(bundle)
                            startService(intent)
                        }

                        Spacer(modifier = Modifier.padding(top = 20.dp))

                        Button("Start Alarm Manager") {
                            val calendar = Calendar.getInstance()
                            calendar.add(Calendar.SECOND, 10)
                            val intent = Intent(applicationContext, BackgroundAlarm::class.java)
                            val pendingIntent = PendingIntent.getBroadcast(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)
                            val alarmManager = getSystemService(ALARM_SERVICE) as? AlarmManager
                            alarmManager?.let {
                                // на Android 13 и выше по-умолчанию нельзя ставить будильники
                                // нужно проверить разрещение специальным методом
                                if (it.canScheduleExactAlarms()) {
                                    try {
                                        it.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                                    } catch (e: SecurityException) {
                                        e.printStackTrace()
                                    }
                                } else {
                                    // если разрещения нет, то нужно в настройках дать доступ приложению
                                    startActivity(Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

    @Composable
    private fun Button(text: String, onClick: () -> Unit) {
        Button(onClick = onClick) {
            Text(text = text)
        }
    }
}
