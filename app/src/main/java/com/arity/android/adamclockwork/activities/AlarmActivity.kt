package com.arity.android.adamclockwork.activities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Vibrator
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.WindowManager
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_alarm.*
import java.util.*
import android.app.Activity
import android.app.KeyguardManager
import android.os.Process
import android.widget.Toast
import com.arity.android.adamclockwork.models.AlarmLab
import com.arity.android.adamclockwork.receivers.AlarmReceiver
import com.arity.android.adamclockwork.R
import com.arity.android.adamclockwork.models.Alarm
import com.vk.sdk.api.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AlarmActivity : AppCompatActivity() {

    private val TAG = AlarmActivity::class.java.simpleName

    private var v: Vibrator? = null
    private var vibA: Boolean = true
    private lateinit var alarm: Alarm

    companion object {
        private const val EXTRA_ALARM_ID = "com.arity.android.adamclockwork.alarmActvity.alarmId"

        fun newIntent(packageContext: Context, alarmId: UUID): Intent {
            val intent = Intent("android.intent.action.MAIN")
            intent.setClass(packageContext, AlarmActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP or Intent.FLAG_FROM_BACKGROUND

            intent.putExtra(EXTRA_ALARM_ID, alarmId)
            return intent
        }

        private var mediaPlayer: MediaPlayer = MediaPlayer()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val window = this.window
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_alarm)

        val alarmid = intent.getSerializableExtra(EXTRA_ALARM_ID) as UUID
         alarm = AlarmLab[applicationContext].getAlarm(alarmid)!!

        val hour = if (alarm.time / 100 > 9) "${alarm.time / 100}" else "0${alarm.time / 100}"
        val min = if (alarm.time % 100 > 9) "${alarm.time % 100}" else "0${alarm.time % 100}"
        txtAlarmTime.text = getString(R.string.alarm_time, hour, min)

        txtAlarmName.text = alarm.name

        if (alarm.vibration) startVibro()

        startSound()

        civWakeup.setOnClickListener {
            if (alarm.morning) {
                var friends = alarm.friends.split("R")
                friends = friends.dropLast(1)
                for (friend in friends)
                    try {
                        writeMessage(friend.toInt(), alarm.message)
                    } catch (t: Throwable) {
                        t.printStackTrace()
                    }
            }
            finish()
        }

        civSleep.setOnClickListener {

            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MINUTE, 5)

            val am = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val intentAlarm = AlarmReceiver.newIntent(applicationContext, alarm.id)

            val highbits = alarm.id.mostSignificantBits
            val lowbits = alarm.id.leastSignificantBits

            val pi = PendingIntent.getBroadcast(applicationContext, (highbits - lowbits).toInt(), intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT)

            am.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pi)

            alarm.millis = calendar.timeInMillis
            alarm.active = true
            AlarmLab[applicationContext].updateAlarm(alarm)

            finish()

        }

    }

    private fun writeMessage(userId: Int, text: String) {
        val message = VKRequest("messages.send", VKParameters.from(VKApiConst.USER_ID, userId, VKApiConst.MESSAGE, text))
        message.executeWithListener(object : VKRequest.VKRequestListener() {

            override fun onComplete(response: VKResponse?) {
                super.onComplete(response)
                Toast.makeText(this@AlarmActivity, "Success!", Toast.LENGTH_LONG).show()
            }

            override fun onError(error: VKError?) {
                super.onError(error)
                Toast.makeText(this@AlarmActivity, "Error: $error", Toast.LENGTH_LONG).show()
                Log.e(TAG,error.toString())
            }
        })
    }

    override fun onBackPressed() {

    }

    private fun startSound(){
        mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(this, alarm.soundUri)
        val mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0)

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM)
        mediaPlayer.isLooping = true
        if (alarm.rising) {
            mediaPlayer.setVolume(0f, 0f)
            launch {
                var vol = 0f
                val maxVol = alarm.volume
                while (vol < maxVol) {
                    vol += 0.005f
                    mediaPlayer.setVolume(vol, vol)
                    delay(1000 * 2)
                }
            }
        } else {
            mediaPlayer.setVolume(alarm.volume.toFloat() / 100, alarm.volume.toFloat() / 100)
        }
        mediaPlayer.prepare()
        mediaPlayer.start()

    }

    private fun stopSound(){
        mediaPlayer.stop()
        mediaPlayer.release()
    }

    private fun startVibro(){
        launch {
            while(vibA){
                v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                v!!.vibrate(5 * 1000)
                delay(7 * 1000)}
        }
    }

    private fun stopVibro(){
        vibA = false
        v!!.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()

        try {
            stopSound()
        }catch (t:Throwable){
            t.printStackTrace()
        }

        try {
            stopVibro()
        }catch (t:Throwable){
            t.printStackTrace()
        }

    }
}


