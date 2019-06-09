package com.arity.android.adamclockwork.receivers


import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import com.arity.android.adamclockwork.services.AlarmService
import com.arity.android.adamclockwork.activities.AlarmActivity
import com.arity.android.adamclockwork.models.AlarmLab
import java.util.*


class AlarmReceiver : BroadcastReceiver() {


    companion object {

        private const val EXTRA_ALARM_ID = "com.arity.android.adamclockwork.alarmreceiver.alarmId"

        fun newIntent(packageContext: Context, alarmId: UUID): Intent {
            val intent = Intent(packageContext, AlarmReceiver::class.java)
            intent.putExtra(EXTRA_ALARM_ID, alarmId)
            intent.putExtra("ONE_TIME", java.lang.Boolean.FALSE)//Задаем параметр интента
            return intent
        }

    }

    @SuppressLint("InvalidWakeLockTag")
    override fun onReceive(context: Context?, intent: Intent?) {

        val pma: PowerManager = context?.getSystemService(Context.POWER_SERVICE) as PowerManager
        val mWakeLock = pma.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "AlarmService")
        val alarmId = intent?.getSerializableExtra(EXTRA_ALARM_ID) as UUID

        val alarm = AlarmLab[context].getAlarm(alarmId)!!

        mWakeLock.acquire()
        mWakeLock.release()

        val launchIntent = context.packageManager.getLaunchIntentForPackage("com.arity.android.adamclockwork")
        context.startActivity(launchIntent)

        val alarmIntent = AlarmActivity.newIntent(context, alarmId)
        context.startActivity(alarmIntent)

        if (alarm.repeatable != 0) {
            AlarmService.newInstance(context)
        } else if(alarm.repeatable == 0) {
            alarm.active = false
            AlarmLab[context].updateAlarm(alarm)

            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intentAlarm = newIntent(context, alarmId)

            val highbits = alarmId.mostSignificantBits
            val lowbits = alarmId.leastSignificantBits
            val pi = PendingIntent.getBroadcast(context, (highbits - lowbits).toInt(), intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT)
            am.cancel(pi)
            Log.i("TAG", "Alarm at ${alarm.time.div(100)} : ${alarm.time.rem(100)} closed from receiver")
        }
    }
}       