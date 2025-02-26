package com.example.backgroundwork

import android.app.job.JobParameters
import android.app.job.JobService
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast

class BackgroundJob : JobService() {

    private var counter: Int = 0

    override fun onStartJob(params: JobParameters?): Boolean {
        val handler = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            Handler()
        } else {
            Handler.createAsync(Looper.getMainLooper())
        }

        Thread {
            while (counter < 1_000_000) {
                try {
                    Thread.sleep(1000)
                    counter++
                    Log.e("BackgroundJob", "Counter $counter")
                    if ((counter >= 100 && counter % 100 == 0) || counter == 1 || counter == 2 || counter == 3) {
                        handler.post {
                            Toast.makeText(
                                applicationContext,
                                "Hooray! $counter",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }.start()

        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d("BackgroundJob", "Job stopped before completion");
        return true; // Позволяет повторно запланировать задачу
    }

}