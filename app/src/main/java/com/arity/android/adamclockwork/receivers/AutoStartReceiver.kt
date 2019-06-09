package com.arity.android.adamclockwork.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.arity.android.adamclockwork.services.AlarmService

class AutoStartReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i("TAG", "onReceive " + intent!!.action)
        if (context != null) {
            AlarmService.newInstance(context)
        }
    }

}