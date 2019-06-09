package com.arity.android.adamclockwork.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.SwitchCompat
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import com.arity.android.adamclockwork.*
import com.arity.android.adamclockwork.fragments.TimePickerFragment.Companion.EXTRA_TIME
import com.arity.android.adamclockwork.helpers.OnBackPressedListener
import com.arity.android.adamclockwork.models.Alarm
import com.arity.android.adamclockwork.models.AlarmLab
import com.vk.sdk.VKAccessToken
import com.vk.sdk.VKCallback
import com.vk.sdk.VKSdk
import com.vk.sdk.VKUIHelper.getApplicationContext
import com.vk.sdk.api.VKError
import java.util.*


class AlarmCreateFragment : Fragment(), OnBackPressedListener {


    companion object {
        private const val ARG_ALARM_ID = "alarmId"

        fun newInstance(alarmId: UUID): AlarmCreateFragment {

            val args = Bundle()
            args.putSerializable(ARG_ALARM_ID, alarmId)

            val fragment = AlarmCreateFragment()
            fragment.arguments = args
            return fragment

        }

        private const val REQUEST_MOORNING = 0
        private const val REQUEST_TIME = 1
        private const val REQUEST_SPEC_DAYS = 2
        private const val REQUEST_SOUND = 3
        private const val DIALOG_TIME = "dialogTime"
        private const val DIALOG_SPEC_DAYS = "dialogSpecDays"
        private const val DIALOG_SOUND = "dialogSound"
        private const val DIALOG_MORNING = "dialogMorning"

        private var mediaPlayer = MediaPlayer()
    }

    private lateinit var alarmTimeTextView: TextView
    private lateinit var alarmTimeLayout: LinearLayout
    private lateinit var alarmDaysTextView: TextView
    private lateinit var alarmDaysLayout: LinearLayout
    private lateinit var alarmVibrationSwitch: SwitchCompat
    private lateinit var alarmVibrationLayout: LinearLayout
    private lateinit var alarmSoundTextView: TextView
    private lateinit var alarmSoundLayout: LinearLayout
    private lateinit var alarmNameEditText: EditText
    private lateinit var alarmRisingSwitch: SwitchCompat
    private lateinit var alarmRisingLayout: LinearLayout
    private lateinit var alarmMorningSwitch: SwitchCompat
    private lateinit var alarmMorningLayout: LinearLayout

    private lateinit var alarmVolumeSeekBar: SeekBar

    private lateinit var alarm: Alarm
    private lateinit var callbacks: Callbacks


    interface Callbacks {
        fun onAlarmSet(alarm: Alarm)
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)

        callbacks = context as Callbacks
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val alarmId: UUID = arguments!!.getSerializable(ARG_ALARM_ID) as UUID
        alarm = AlarmLab[activity as Activity].getAlarm(alarmId)!!


        val v = inflater.inflate(R.layout.fragment_alarm_create, container, false)

        alarmNameEditText = v.findViewById(R.id.alarm_name_edit_text)
        alarmNameEditText.setText(alarm.name)

        alarmNameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                alarm.name = s.toString()
                updateAlarm()
            }

            override fun afterTextChanged(s: Editable) {
                updateAlarm()
            }
        })

        alarmTimeTextView = v.findViewById(R.id.alarm_time_text_view)
        updateTime()

        alarmTimeLayout = v.findViewById(R.id.alarm_time_layout)
        alarmTimeLayout.setOnClickListener {
            val manager = fragmentManager
            val dialog = TimePickerFragment
                    .newInstance(alarm.time)
            dialog.setTargetFragment(this@AlarmCreateFragment, REQUEST_TIME)
            dialog.show(manager, DIALOG_TIME)
        }

        alarmDaysTextView = v.findViewById(R.id.alarm_repeat_text_view)
        updateRepeatable()

        alarmDaysLayout = v.findViewById(R.id.alarm_days_layout)
        alarmDaysLayout.setOnClickListener {
            val manager = fragmentManager
            val dialog = DaysFragment.newInstance(alarm.repeatable.toString())
            dialog.setTargetFragment(this@AlarmCreateFragment, REQUEST_SPEC_DAYS)
            dialog.show(manager, DIALOG_SPEC_DAYS)
            Log.i("TAG", "Days dialog started")

        }

        alarmVibrationLayout = v.findViewById(R.id.alarm_vibration_layout)
        alarmVibrationLayout.setOnClickListener {
            alarm.vibration = !alarm.vibration
            alarmVibrationSwitch.isChecked = alarm.vibration
        }

        alarmVibrationSwitch = v.findViewById(R.id.alarm_switch_vibration)
        alarmVibrationSwitch.isChecked = alarm.vibration
        alarmVibrationSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                alarm.vibration = isChecked
            }
        }

        alarmSoundTextView = v.findViewById(R.id.alarm_choose_sound_text_view)
        updateSound()

        alarmSoundLayout = v.findViewById(R.id.alarm_sound_layout)
        alarmSoundLayout.setOnClickListener {
            try {
                mediaPlayer.stop()
                mediaPlayer.release()
            } catch (t: Throwable) {
            }
            val manager = fragmentManager
            val dialog = SoundsFragment
                    .newInstance(alarm.soundUri)
            dialog.setTargetFragment(this@AlarmCreateFragment, REQUEST_SOUND)
            dialog.show(manager, DIALOG_SOUND)
        }

        alarmVolumeSeekBar = v.findViewById(R.id.alarm_volume_seek_bar)
        alarmVolumeSeekBar.progress = alarm.volume
        alarmVolumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                try {
                    mediaPlayer.stop()
                    mediaPlayer.release()
                } catch (t: Throwable) {
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                try {
                    mediaPlayer.stop()
                    mediaPlayer.release()
                } catch (t: Throwable) {
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                alarm.volume = seekBar.progress

                mediaPlayer = MediaPlayer()
                mediaPlayer.setDataSource(activity, alarm.soundUri)

                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
                mediaPlayer.setVolume(seekBar.progress.toFloat() / 100, seekBar.progress.toFloat() / 100)
                mediaPlayer.isLooping = false
                mediaPlayer.prepare()
                mediaPlayer.start()

            }
        })

        alarmRisingLayout = v.findViewById(R.id.alarm_rising_sound_layout)
        alarmRisingLayout.setOnClickListener {
            alarm.rising = !alarm.rising
            alarmRisingSwitch.isChecked = alarm.rising
        }

        alarmRisingSwitch = v.findViewById(R.id.alarm_switch_rising_sound)
        alarmRisingSwitch.isChecked = alarm.rising
        alarmRisingSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                alarm.rising = isChecked
            }
        }

        alarmMorningLayout = v.findViewById(R.id.alarm_morning_layout)
        alarmMorningLayout.setOnClickListener {
            if(alarm.morning){
            val manager = fragmentManager
            val dialog = GoodMorningFragment.newInstance(alarm.message, alarm.friends)
            dialog.setTargetFragment(this@AlarmCreateFragment, REQUEST_MOORNING)
            dialog.show(manager, DIALOG_MORNING)
            } else {
                alarm.morning = true
                alarmMorningSwitch.isChecked = alarm.morning
                createDialog(true)
            }
        }

        alarmMorningSwitch = v.findViewById(R.id.alarm_switch_good_morning)
        alarmMorningSwitch.isChecked = alarm.morning
        alarmMorningSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                alarm.morning = isChecked
                createDialog(isChecked)
            }
        }

        return v
    }


    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.menu_main, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.setAlarm -> {
                updateAlarm()
                callbacks.onAlarmSet(alarm)
                activity?.finish()
                activity?.overridePendingTransition(R.anim.alpha, R.anim.translation_exit)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, object : VKCallback<VKAccessToken> {
                    override fun onResult(res: VKAccessToken) {
                        Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(error: VKError) {
                        Toast.makeText(getApplicationContext(), "ERROR!", Toast.LENGTH_SHORT).show()
                    }
                })) {
            super.onActivityResult(requestCode, resultCode, data)
        }
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            REQUEST_TIME -> {
                alarm.time = data.getIntExtra(EXTRA_TIME, 600)
                updateTime()
            }
            REQUEST_SPEC_DAYS -> {
                data.let {
                    alarm.repeatable = it.getStringExtra(DaysFragment.EXTRA_DAYS).toInt()
                }

                updateRepeatable()
            }
            REQUEST_SOUND -> {
                alarm.soundUri = Uri.parse(data.getStringExtra(SoundsFragment.EXTRA_SOUNDS))
                updateSound()
            }
            REQUEST_MOORNING -> {
                alarm.message = data.getStringExtra(GoodMorningFragment.EXTRA_MESSAGE)
                alarm.friends = data.getStringExtra(GoodMorningFragment.EXTRA_FRIENDS)
                updateAlarm()
            }
        }

    }

    private fun updateSound() {
        alarmSoundTextView.text = RingtoneManager.getRingtone(activity, alarm.soundUri).getTitle(activity)
    }

    private fun updateAlarm() {
        alarm.active = true
        AlarmLab[activity as Activity].updateAlarm(alarm)
    }

    private fun updateTime() {
        val hour = if (alarm.time / 100 > 9) "${alarm.time / 100}" else "0${alarm.time / 100}"
        val min = if (alarm.time % 100 > 9) "${alarm.time % 100}" else "0${alarm.time % 100}"
        alarmTimeTextView.text = getString(R.string.alarm_time, hour, min)
    }

    private fun updateRepeatable() {
        alarmDaysTextView.text = when (alarm.repeatable) {
            0 -> getString(R.string.alarm_oneTime)
            2 -> getString(R.string.alarm_monday)
            3 -> getString(R.string.alarm_tuesday)
            4 -> getString(R.string.alarm_wednesday)
            5 -> getString(R.string.alarm_thursday)
            6 -> getString(R.string.alarm_friday)
            7 -> getString(R.string.alarm_saturday)
            1 -> getString(R.string.alarm_sunday)
            1234567 -> getString(R.string.alarm_everyday)
            23456 -> getString(R.string.alarm_mon_to_fri)
            else -> {
                var temp = ""
                for (c in alarm.repeatable.toString().toCharArray()) {
                    when (c) {
                        '1' -> temp += getString(R.string.alarm_sunday) + " "
                        '2' -> temp += getString(R.string.alarm_monday) + " "
                        '3' -> temp += getString(R.string.alarm_tuesday) + " "
                        '4' -> temp += getString(R.string.alarm_wednesday) + " "
                        '5' -> temp += getString(R.string.alarm_thursday) + " "
                        '6' -> temp += getString(R.string.alarm_friday) + " "
                        '7' -> temp += getString(R.string.alarm_saturday) + " "
                    }
                }
                temp
            }
        }
    }

    private fun createDialog(create: Boolean) {
        if (create) {
            val builder = AlertDialog.Builder(activity!!)
            builder.setTitle("Vk authorisation requires")
            builder.setMessage("If want to you use this function you should authorize to VK.com")
            builder.setPositiveButton("OK") { dialog, which ->
                if (!VKSdk.isLoggedIn()) {
                    VKSdk.login(activity!!, "wall,messages,friends")
                } else {
                    val manager = fragmentManager
                    val dialog = GoodMorningFragment.newInstance(alarm.message, alarm.friends)
                    dialog.setTargetFragment(this@AlarmCreateFragment, REQUEST_MOORNING)
                    dialog.show(manager, DIALOG_MORNING)
                }
            }
            builder.setNegativeButton("Back") { dialog, which ->
                alarm.morning = false
                alarmMorningSwitch.isChecked = alarm.morning
            }
            val dialog: AlertDialog = builder.create()

            dialog.show()
        }
    }

    override fun onBackPressed() {
        activity?.finish()
        activity?.overridePendingTransition(R.anim.alpha, R.anim.translation_exit)
    }

    override fun onStop() {
        super.onStop()
        try {
            mediaPlayer.stop()
            mediaPlayer.release()
        } catch (t: Throwable) {
        }
    }


}