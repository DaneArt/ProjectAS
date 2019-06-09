package com.arity.android.adamclockwork.models

import android.media.RingtoneManager
import android.net.Uri
import java.util.*

class Alarm @JvmOverloads constructor(val id: UUID = UUID.randomUUID()) {
    var time: Int = 600
    var repeatable: Int = 0
    var active: Boolean = false
    var vibration: Boolean = false
    var soundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
    var volume: Int = 50
    var name: String = " "
    var rising: Boolean = false
    var millis: Long = Calendar.getInstance().timeInMillis
    var morning: Boolean = false
    var message: String = ""
    var friends: String = ""
}


