package com.arity.android.adamclockwork.services

import android.app.AlarmManager
import android.app.IntentService
import android.app.PendingIntent
import android.content.Intent
import android.content.Context
import android.util.Log
import com.arity.android.adamclockwork.models.Alarm
import com.arity.android.adamclockwork.models.AlarmLab
import com.arity.android.adamclockwork.receivers.AlarmReceiver
import java.util.*

class AlarmService : IntentService("AlarmService") {

    companion object {

        private const val EXTRA_ID = "com.arity.android.adamclockwork.extra.ID"
        fun newInstance(context: Context, alarmId: UUID) {
            val intent = Intent(context, AlarmService::class.java).apply {
                putExtra(EXTRA_ID, alarmId)
            }
            context.startService(intent)
        }
        @JvmStatic
        fun newInstance(context: Context) {
            val intent = Intent(context, AlarmService::class.java)
            context.startService(intent)
        }
    }

    override fun onHandleIntent(intent: Intent?) {

        if (intent?.data != null) {
            val alarmId = intent.getSerializableExtra(EXTRA_ID) as UUID
            val alarm = AlarmLab[applicationContext].getAlarm(alarmId)
            if(alarm!!.active)setAlarm(alarm)

        }else{
            for (alarm in AlarmLab[applicationContext].alarms){
                if(alarm.active)setAlarm(alarm)
            }
        }
    }

    private fun setAlarm(alarm: Alarm) {
        //подготовка объекта календаря
        val calendar: Calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, alarm.time / 100)
        calendar.set(Calendar.MINUTE, alarm.time % 100)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        //настройка времени звонка в соответствии с полем repeatable(частотой звонка)
        if (alarm.repeatable != 0) {
            val temp = Integer.toString(alarm.repeatable)
            val daysList = IntArray(temp.length)
            for (i in 0 until temp.length) {
                daysList[i] = temp[i] - '0'
            }

            val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            var dif = 0
            for (i in daysList) {
                dif = i - today
                if (dif >= 0) break
            }

            calendar.set(Calendar.DAY_OF_WEEK, today + dif)
            if (dif == 0 && calendar.timeInMillis < System.currentTimeMillis()) {
                for (i in daysList) {
                    dif = i - today
                    if (dif > 0) break
                }

                if (dif == 0) calendar.set(Calendar.DAY_OF_WEEK, daysList[0])
                else calendar.set(Calendar.DAY_OF_WEEK, today + dif)
            }
            if (calendar.timeInMillis < System.currentTimeMillis()) calendar.add(Calendar.WEEK_OF_YEAR, 1)
        } else if (calendar.timeInMillis < System.currentTimeMillis()) calendar.add(Calendar.DAY_OF_WEEK, 1)

        alarm.millis = calendar.timeInMillis
        AlarmLab[applicationContext].updateAlarm(alarm)

        //Установка Broadcast
        val am = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intentAlarm = AlarmReceiver.newIntent(applicationContext, alarm.id)
        //Унификация Broadcast путем связи UUID объекта будильника с PendingIntent
        val highbits = alarm.id.mostSignificantBits
        val lowbits = alarm.id.leastSignificantBits
        val pi = PendingIntent.getBroadcast(applicationContext, (highbits - lowbits).toInt(),
                intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT)
        am.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pi)
        Log.i("TAG", "Alarm set at ${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)} of ${calendar.get(Calendar.DAY_OF_WEEK)}")
    }


}
