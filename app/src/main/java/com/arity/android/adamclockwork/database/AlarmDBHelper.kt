package com.arity.android.adamclockwork.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.arity.android.adamclockwork.database.AlarmDBSchema.AlarmTable

class AlarmDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {

        Log.i("TAG_LOG", "onCreateDB")

        db.execSQL("CREATE TABLE " + AlarmTable.NAME + " ( " +
                " _id integer primary key autoincrement, " +
                AlarmTable.Cols.UUID + ", " +
                AlarmTable.Cols.TIME + ", " +
                AlarmTable.Cols.REPEATABLE + ", " +
                AlarmTable.Cols.ACTIVE + ", " +
                AlarmTable.Cols.VIBRO + ", " +
                AlarmTable.Cols.SOUNDURI + ", " +
                AlarmTable.Cols.VOLUME + ", " +
                AlarmTable.Cols.NAME + ", " +
                AlarmTable.Cols.RISING + ", " +
                AlarmTable.Cols.MILLIS + ", " +
                AlarmTable.Cols.MORNING + ", " +
                AlarmTable.Cols.MESSAGE + ", " +
                AlarmTable.Cols.FRIENDS  +
                ")"
        )

    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        Log.i("TAG_LOG", "onUpdateDB")
        db.execSQL("DROP TABLE IF EXISTS ${AlarmTable.NAME}")
        onCreate(db)
    }

    companion object {
        const val DATABASE_NAME = "alarmBase.db" // название бд
        private const val DATABASE_VERSION = 5

    }
}
