package com.arity.android.adamclockwork

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import java.util.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("com.arity.android.adamclockwork", appContext.packageName)
    }

    @Test
    fun howMuchBefore(){

        val c1 = Calendar.getInstance()
        val c2 = Calendar.getInstance()
        c2.add(Calendar.DAY_OF_WEEK,1)

        val diff = c2.timeInMillis - c1.timeInMillis

        assertEquals(2,diff/1000/60/60)

    }
}
