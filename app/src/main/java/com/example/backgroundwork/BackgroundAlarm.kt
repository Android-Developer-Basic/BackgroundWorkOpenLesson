package com.example.backgroundwork

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class BackgroundAlarm : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Toast.makeText(context, "ALARM!", Toast.LENGTH_SHORT).show()
        Log.e("BackgroundAlarm", "Run event")
    }
}