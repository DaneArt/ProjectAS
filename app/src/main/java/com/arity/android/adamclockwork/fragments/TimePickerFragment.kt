package com.arity.android.adamclockwork.fragments

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.widget.TimePicker
import com.arity.android.adamclockwork.R
import java.util.*

class TimePickerFragment : DialogFragment() {

    companion object {
        val EXTRA_TIME =
                "com.arity.android.adamclockwork.time"
        private val ARG_TIME = "time"

        fun newInstance(time: Int): TimePickerFragment {
            val args = Bundle()
            args.putInt(ARG_TIME, time)

            val fragment = TimePickerFragment()
            fragment.arguments = args
            return fragment
        }
    }


    private lateinit var timePicker: TimePicker

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val hour = arguments!!.getInt(ARG_TIME) / 100
        val min = arguments!!.getInt(ARG_TIME) % 100

        val v = LayoutInflater.from(activity)
                .inflate(R.layout.dialog_time, null)

        timePicker = v.findViewById(R.id.dialog_time_picker)
        timePicker.setIs24HourView(true)
        timePicker.currentHour = hour
        timePicker.currentMinute = min

        val cal = Calendar.getInstance()


        return android.support.v7.app.AlertDialog.Builder(activity!!, R.style.MyCustomTheme)
                .setView(v)
                .setTitle(R.string.time_picker_title)
                .setPositiveButton(android.R.string.ok
                ) { dialog, which ->
                    val time = timePicker.currentHour * 100 + timePicker.currentMinute
                    sendResult(Activity.RESULT_OK, time)
                }
                .create()
    }

    private fun sendResult(resultCode: Int, time: Int) {
        if (targetFragment == null) {
            return
        }

        val intent = Intent()
        intent.putExtra(EXTRA_TIME, time)

        targetFragment!!
                .onActivityResult(targetRequestCode, resultCode, intent)
    }


}