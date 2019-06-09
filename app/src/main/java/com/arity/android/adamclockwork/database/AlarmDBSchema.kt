package com.arity.android.adamclockwork.database

class AlarmDBSchema {

    object AlarmTable {
        val NAME = "alarms"

        object Cols {
            val TIME = "time"
            val REPEATABLE = "repeatable"
            val UUID = "uuid"
            val ACTIVE = "active"
            val VIBRO = "vibration"
            val SOUNDURI = "sounduri"
            val VOLUME = "volume"
            val NAME = "name"
            val RISING = "rising"
            val MILLIS = "millis"
            val MORNING = "morning"
            val MESSAGE = "message"
            val FRIENDS = "friends"
        }
    }
}