package com.arity.android.adamclockwork.models


import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.arity.android.adamclockwork.database.AlarmCursorWrapper
import com.arity.android.adamclockwork.database.AlarmDBHelper
import com.arity.android.adamclockwork.database.AlarmDBSchema.AlarmTable
import com.arity.android.adamclockwork.database.AlarmDBSchema.AlarmTable.Cols.ACTIVE
import com.arity.android.adamclockwork.database.AlarmDBSchema.AlarmTable.Cols.FRIENDS
import com.arity.android.adamclockwork.database.AlarmDBSchema.AlarmTable.Cols.MESSAGE
import com.arity.android.adamclockwork.database.AlarmDBSchema.AlarmTable.Cols.MILLIS
import com.arity.android.adamclockwork.database.AlarmDBSchema.AlarmTable.Cols.MORNING
import com.arity.android.adamclockwork.database.AlarmDBSchema.AlarmTable.Cols.NAME
import com.arity.android.adamclockwork.database.AlarmDBSchema.AlarmTable.Cols.REPEATABLE
import com.arity.android.adamclockwork.database.AlarmDBSchema.AlarmTable.Cols.RISING
import com.arity.android.adamclockwork.database.AlarmDBSchema.AlarmTable.Cols.SOUNDURI
import com.arity.android.adamclockwork.database.AlarmDBSchema.AlarmTable.Cols.TIME
import com.arity.android.adamclockwork.database.AlarmDBSchema.AlarmTable.Cols.UUID
import com.arity.android.adamclockwork.database.AlarmDBSchema.AlarmTable.Cols.VIBRO
import com.arity.android.adamclockwork.database.AlarmDBSchema.AlarmTable.Cols.VOLUME
import java.util.*


class AlarmLab private constructor(context: Context) {
    private val mContext: Context
    private val mDatabase: SQLiteDatabase

    val alarms: ArrayList<Alarm>
        get() {
            val alarms = ArrayList<Alarm>()
            val cursor = queryCrimes(// having
                    null  // orderBy
                    , null)
            try {
                cursor?.moveToFirst()
                while (!cursor?.isAfterLast!!) {
                    alarms.add(cursor.alarm)
                    cursor.moveToNext()
                }
            } finally {
                cursor?.close()
            }
            return alarms
        }

    init {
        mContext = context.applicationContext
        mDatabase = AlarmDBHelper(mContext)
                .writableDatabase

    }

    fun addAlarm(a: Alarm) {
        val values = getContentValues(a)
        mDatabase.insert(AlarmTable.NAME, null, values)
    }

    fun removeAlarm(a: Alarm) {
        mDatabase.execSQL("delete from " + AlarmTable.NAME + " where uuid='" + a.id + "'")
    }

    fun getAlarm(id: UUID): Alarm? {
        try {
            val cursor = queryCrimes(
                    AlarmTable.Cols.UUID + " = ?",
                    arrayOf(id.toString())
            )
            cursor.use { cursor ->
                if (cursor?.count == 0) {
                    return null
                }
                cursor?.moveToFirst()
                return cursor?.alarm
            }
        } catch (t: Throwable) {
            return null
        }
    }

    fun updateAlarm(alarm: Alarm) {
        val uuidString = alarm.id.toString()
        val values = getContentValues(alarm)
        mDatabase.update(AlarmTable.NAME, values,
                AlarmTable.Cols.UUID + " = ?",
                arrayOf(uuidString))
    }

    private fun queryCrimes(whereClause: String?, whereArgs: Array<String>?): AlarmCursorWrapper? {
        try {
            val cursor = mDatabase.query(
                    AlarmTable.NAME, null,
                    whereClause,
                    whereArgs, null, null, null
            )
            return AlarmCursorWrapper(cursor)
        } catch (t: Throwable) {
            return null
        }

    }

    companion object {

        private var alarmLab: AlarmLab? = null

        operator fun get(context: Context): AlarmLab {
            if (alarmLab == null) {
                alarmLab = AlarmLab(context)
            }

            return alarmLab as AlarmLab
        }

        private fun getContentValues(alarm: Alarm): ContentValues {
            val values = ContentValues()
            values.put(UUID, alarm.id.toString())
            values.put(TIME, alarm.time)
            values.put(ACTIVE, if (alarm.active) 1 else 0)
            values.put(REPEATABLE, alarm.repeatable)
            values.put(VIBRO, if (alarm.vibration) 1 else 0)
            values.put(SOUNDURI, alarm.soundUri.toString())
            values.put(VOLUME, alarm.volume)
            values.put(NAME, alarm.name)
            values.put(RISING, if (alarm.rising) 1 else 0)
            values.put(MILLIS, alarm.millis)
            values.put(MORNING, if (alarm.morning) 1 else 0)
            values.put(MESSAGE, alarm.message)
            values.put(FRIENDS, alarm.friends)
            return values
        }

    }

}


