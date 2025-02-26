package com.example.backgroundwork

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat

class ForegroundService : Service() {

    private var counter = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val handler = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            Handler()
        } else {
            Handler.createAsync(Looper.getMainLooper())
        }

        createNotificationChannel()
        val notification = getNotification()
        startForeground(1, notification)

        Thread {
            while (counter < 1_000_000) {
                try {
                    Thread.sleep(1000)
                    counter++
                    Log.e("ForegroundService", "Counter $counter")
                    if ((counter >= 100 && counter % 100 == 0) || counter == 1 || counter == 2 || counter == 3) {
                        handler.post {
                            Toast.makeText(
                                applicationContext,
                                "Hooray! $counter",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    updateNotification()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }.start()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun getNotification(): Notification {
        return NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("My Foreground Service Running")
            .setContentText("Counter: $counter")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager?.notify(1, getNotification());
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "My Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    companion object {
        const val CHANNEL_ID = "My Channel"
    }
}