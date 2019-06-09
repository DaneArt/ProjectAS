package com.arity.android.adamclockwork.activities


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.arity.android.adamclockwork.fragments.AlarmCreateFragment
import com.arity.android.adamclockwork.services.AlarmService
import com.arity.android.adamclockwork.helpers.OnBackPressedListener
import com.arity.android.adamclockwork.R
import java.util.*
import com.arity.android.adamclockwork.models.Alarm


class AlarmCreateActivity : AppCompatActivity(),
        AlarmCreateFragment.Callbacks, OnBackPressedListener {


    companion object {
        private const val EXTRA_ALARM_ID =
                "com.arity.android.adamclockwork.alarm_id"

        fun newIntent(packageContext: Context, alarmId: UUID): Intent {

            val intent = Intent(packageContext, AlarmCreateActivity::class.java)
            intent.putExtra(EXTRA_ALARM_ID, alarmId)

            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment)

        val alarmId = intent.getSerializableExtra(EXTRA_ALARM_ID) as UUID

        val fm = supportFragmentManager
        var fragment: Fragment? = fm.findFragmentById(R.id.fragment_container)

        if (fragment == null) {
            fragment = AlarmCreateFragment.newInstance(alarmId)
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit()
        }

    }

    override fun onAlarmSet(alarm: Alarm) {
        AlarmService.newInstance(applicationContext, alarm.id)
    }

    override fun onBackPressed() {
        val fm = supportFragmentManager
        var backPressedListener: OnBackPressedListener? = null
        for (fragment in fm.fragments) {
            if (fragment is OnBackPressedListener) {
                backPressedListener = fragment
                break
            }
        }

        if (backPressedListener != null) {
            backPressedListener.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }

        
}
