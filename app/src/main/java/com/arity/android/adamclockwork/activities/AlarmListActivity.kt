package com.arity.android.adamclockwork.activities

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.SystemClock
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import com.arity.android.adamclockwork.receivers.AlarmReceiver
import com.arity.android.adamclockwork.services.AlarmService
import com.arity.android.adamclockwork.R
import com.arity.android.adamclockwork.models.Alarm
import com.arity.android.adamclockwork.models.AlarmLab
import com.vk.sdk.util.VKUtil
import kotlinx.android.synthetic.main.activity_list.*
import java.util.*


class AlarmListActivity : AppCompatActivity() {

    private var adapter: AlarmAdapter? = null

    private val TAG = AlarmListActivity::class.java.simpleName

    private lateinit var alarmRecyclerView: RecyclerView
    private lateinit var alarmCreateFab: FloatingActionButton
    private lateinit var alarmNearestTextView: TextView

    private  var alarmTask : UpdateTask = UpdateTask()
    interface AlarmItemTouchHelperAdapter {
        fun onItemDismiss(position: Int)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        val fingerprints = VKUtil.getCertificateFingerprint(this, this.packageName)
        Log.e(TAG,"fingerprint ${fingerprints[0]}")

        alarmRecyclerView = findViewById(R.id.alarm_recycler_view)
        alarmRecyclerView.layoutManager = LinearLayoutManager(baseContext)

        alarmCreateFab = findViewById(R.id.alarm_create_button)
        alarmCreateFab.setOnClickListener {
            val alarm = Alarm()
            AlarmLab[applicationContext].addAlarm(alarm)
            val intent = AlarmCreateActivity.newIntent(applicationContext, alarm.id)
            startActivity(intent)
            overridePendingTransition(R.anim.translation, R.anim.alpha)
        }

        alarmNearestTextView = findViewById(R.id.alarm_next_text_view)
        updateUI()

        val callback = AlarmSimpleItemTouchHelperCallback(adapter as AlarmItemTouchHelperAdapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(alarmRecyclerView)

        AlarmService.newInstance(applicationContext)

        alarmTask.execute()
    }

    public override fun onResume() {
        super.onResume()
        val fingerprints = VKUtil.getCertificateFingerprint(this, this.packageName)
        Log.i("TAG", fingerprints.toString())
        try {
            alarmTask.execute()
        }catch (t:Throwable){}
        updateUI()
    }

    //Холдер списка
    private inner class AlarmHolder(inflater: LayoutInflater, parent: ViewGroup) : RecyclerView.ViewHolder(inflater.inflate(R.layout.list_item_alarm, parent, false)),
            View.OnClickListener, CompoundButton.OnCheckedChangeListener {

        private var alarm: Alarm? = null

        private val timeTextView: TextView
        private val daysTextView: TextView
        private val activeCheckBox: CheckBox
        private val nameTextView: TextView

        init {
            itemView.setOnClickListener(this)

            timeTextView = itemView.findViewById(R.id.alarm_time) as TextView
            daysTextView = itemView.findViewById(R.id.alarm_day) as TextView
            activeCheckBox = itemView.findViewById(R.id.alarm_active) as CheckBox
            nameTextView = itemView.findViewById(R.id.alarm_name_text_view) as TextView

        }

        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            if (buttonView!!.isPressed) {
                alarm!!.active = isChecked
                AlarmLab[applicationContext].updateAlarm(alarm!!)
                if (alarm!!.active) {
                    AlarmService.newInstance(applicationContext, alarm!!.id)
                    daysTextView.setTextColor(resources.getColor(R.color.activeAlarm))
                    timeTextView.setTextColor(resources.getColor(R.color.activeAlarm))
                    nameTextView.setTextColor(resources.getColor(R.color.activeAlarm))
                } else {
                    removeAlarm(alarm!!)
                    daysTextView.setTextColor(resources.getColor(R.color.inactiveAlarm))
                    timeTextView.setTextColor(resources.getColor(R.color.inactiveAlarm))
                    nameTextView.setTextColor(resources.getColor(R.color.inactiveAlarm))
                }
                updateNearestAlarm()
            }
        }

        override fun onClick(v: View?) {

            if (v!!.id == R.id.alarm_cardView) {
                val alarmId: UUID = AlarmLab[applicationContext].alarms[adapterPosition].id
                val intent = AlarmCreateActivity.newIntent(baseContext, alarmId)
                startActivity(intent)
                overridePendingTransition(R.anim.translation, R.anim.alpha)
            }

        }

        @SuppressLint("SetTextI18n")
        fun bind(alarm: Alarm) {
            this.alarm = alarm

            val hour = if ((alarm.time / 100) > 9) "${alarm.time / 100}" else "0${alarm.time / 100}"
            val min = if ((alarm.time % 100) > 9) "${alarm.time % 100}" else "0${alarm.time % 100}"

            timeTextView.text = getString(R.string.alarm_time, hour, min)

            daysTextView.text = when (alarm.repeatable) {
                1 -> getString(R.string.alarm_sunday)
                0 -> getString(R.string.alarm_oneTime)
                2 -> getString(R.string.alarm_monday)
                3 -> getString(R.string.alarm_tuesday)
                4 -> getString(R.string.alarm_wednesday)
                5 -> getString(R.string.alarm_thursday)
                6 -> getString(R.string.alarm_friday)
                7 -> getString(R.string.alarm_saturday)
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

            activeCheckBox.isChecked = alarm.active
            activeCheckBox.setOnCheckedChangeListener(this)

            nameTextView.text = alarm.name

            if (alarm.active) {
                daysTextView.setTextColor(resources.getColor(R.color.activeAlarm))
                timeTextView.setTextColor(resources.getColor(R.color.activeAlarm))
                nameTextView.setTextColor(resources.getColor(R.color.activeAlarm))
            } else {
                daysTextView.setTextColor(resources.getColor(R.color.inactiveAlarm))
                timeTextView.setTextColor(resources.getColor(R.color.inactiveAlarm))
                nameTextView.setTextColor(resources.getColor(R.color.inactiveAlarm))
            }

        }


    }

    //Адаптер списка
    private inner class AlarmAdapter(private var alarms: ArrayList<Alarm>?) : RecyclerView.Adapter<AlarmHolder>(),
            AlarmItemTouchHelperAdapter {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmHolder {
            val layoutInflater = LayoutInflater.from(baseContext)
            return AlarmHolder(layoutInflater, parent)
        }

        override fun onBindViewHolder(holder: AlarmHolder, position: Int) {
            val alarm = alarms!![position]
            holder.bind(alarm)
        }

        override fun getItemCount(): Int {
            return alarms!!.size
        }

        fun setAlarms(alarms: ArrayList<Alarm>) {
            this.alarms = alarms
        }

        override fun onItemDismiss(position: Int) {

            val alarm = AlarmLab[applicationContext].alarms[position]

            removeAlarm(alarm)
            AlarmLab[applicationContext].removeAlarm(alarm)

            notifyItemRemoved(position)
            updateUI()
        }

    }

    //Класс отвечающий за управление элементами списка
    inner class AlarmSimpleItemTouchHelperCallback(private val touchHelperAdapter: AlarmItemTouchHelperAdapter) : ItemTouchHelper.Callback() {

        override fun onMove(p0: RecyclerView, p1: ViewHolder, p2: ViewHolder): Boolean {
            return false
        }

        override fun isLongPressDragEnabled(): Boolean {
            return true
        }

        override fun isItemViewSwipeEnabled(): Boolean {
            return true
        }

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: ViewHolder): Int {
            val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
            return ItemTouchHelper.Callback.makeMovementFlags(0, swipeFlags)
        }

        override fun onSwiped(viewHolder: ViewHolder, direction: Int) {
            touchHelperAdapter.onItemDismiss(viewHolder.adapterPosition)
        }

    }

    private fun updateUI() {
        val alarmLab = AlarmLab[baseContext]
        val alarms = alarmLab.alarms

        if (adapter == null) {
            adapter = AlarmAdapter(alarms)
            alarm_recycler_view.adapter = adapter
        } else {
            adapter?.setAlarms(alarms)
            adapter?.notifyDataSetChanged()
        }

        updateNearestAlarm()
        Log.i("TAG", "UI updated")
    }

    private fun removeAlarm(alarm: Alarm) {

        val am = applicationContext!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intentAlarm = Intent(applicationContext, AlarmReceiver::class.java)
        intentAlarm.putExtra("ONE_TIME", java.lang.Boolean.FALSE)

        val highbits = alarm.id.mostSignificantBits
        val lowbits = alarm.id.leastSignificantBits
        val pi = PendingIntent.getBroadcast(applicationContext, (highbits - lowbits).toInt(), intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT)
        am.cancel(pi)

        Log.i("TAG", "Alarm at ${alarm.time.div(100)} : ${alarm.time.rem(100)} closed")
    }

    private fun updateNearestAlarm() {
        val arrayOfMillis = arrayListOf<Long>()
        for (alarm in AlarmLab[applicationContext].alarms) {
            if (alarm.active)
                arrayOfMillis.add(alarm.millis)
        }
        arrayOfMillis.sort()

        if (arrayOfMillis.size != 0) {
            val def = arrayOfMillis[0] - Calendar.getInstance().timeInMillis
            val days = def / 1000 / 60 / 60 / 24
            val hours = def / 1000 / 60 / 60 - days * 24
            val mins = def / 1000 / 60 - (def / 1000 / 60 / 60 * 60) + 1
            alarmNearestTextView.text = getString(R.string.alarm_nearest, days, hours, mins)
        } else alarmNearestTextView.text = getString(R.string.alarm_no_alarms)
    }

    override fun onDestroy() {
        super.onDestroy()
        alarmTask.cancel(true)
    }

    inner class UpdateTask : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg p0: Void?): Void {
            while(true){
                SystemClock.sleep( 1000)
                publishProgress()
            }
        }

        override fun onProgressUpdate(vararg values: Void) {
            updateNearestAlarm()
        }
    }
}

