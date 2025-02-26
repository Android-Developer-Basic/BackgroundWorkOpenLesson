package com.example.backgroundwork

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters

class BackgroundWorkManager(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val handler = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            Handler()
        } else {
            Handler.createAsync(Looper.getMainLooper())
        }

        var counter = 0
        while (counter < 1_000_000) {
            if (isStopped) { // если задача отменена, то возвращаем ошибку
                return Result.failure()
            }

            Thread.sleep(1000)
            counter++
            Log.e("Worker", "Counter $counter")
            if ((counter >= 100 && counter % 100 == 0) || counter == 1 || counter == 2 || counter == 3) {
                handler.post {
                    Toast.makeText(
                        applicationContext,
                        "Hooray! $counter",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        return Result.success()
    }
}