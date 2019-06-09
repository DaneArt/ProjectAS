package com.arity.android.adamclockwork.database

import android.database.Cursor
import android.database.CursorWrapper
import android.net.Uri
import com.arity.android.adamclockwork.models.Alarm
import com.arity.android.adamclockwork.database.AlarmDBSchema.AlarmTable
import java.util.*

class AlarmCursorWrapper(cursor: Cursor) : CursorWrapper(cursor) {

    val alarm: Alarm
        get() {
            val uuidString = getString(getColumnIndex(AlarmTable.Cols.UUID))
            val time = getInt(getColumnIndex(AlarmTable.Cols.TIME))
            val isActive = getInt(getColumnIndex(AlarmTable.Cols.ACTIVE))
            val repeatable = getInt(getColumnIndex(AlarmTable.Cols.REPEATABLE))
            val vibration = getInt(getColumnIndex(AlarmTable.Cols.VIBRO))
            val soundUri = Uri.parse(getString(getColumnIndex(AlarmTable.Cols.SOUNDURI)))
            val volume = getInt(getColumnIndex(AlarmTable.Cols.VOLUME))
            val name = getString(getColumnIndex(AlarmTable.Cols.NAME))
            val rising = getInt(getColumnIndex(AlarmTable.Cols.RISING))
            val millis = getLong(getColumnIndex(AlarmTable.Cols.MILLIS))
            val morning = getInt(getColumnIndex(AlarmTable.Cols.MORNING))
            val message = getString(getColumnIndex(AlarmTable.Cols.MESSAGE))
            val friends = getString(getColumnIndex(AlarmTable.Cols.FRIENDS))

            val alarm = Alarm(UUID.fromString(uuidString))
            alarm.time = time
            alarm.active = isActive != 0
            alarm.repeatable = repeatable
            alarm.vibration = vibration !=0
            alarm.soundUri = soundUri
            alarm.volume = volume
            alarm.name = name
            alarm.rising = rising != 0
            alarm.millis = millis
            alarm.morning = morning !=0
            alarm.message = message
            alarm.friends = friends
            return alarm
        }
}
