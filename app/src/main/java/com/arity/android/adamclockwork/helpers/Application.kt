package com.arity.android.adamclockwork.helpers


import com.vk.sdk.VKSdk

class Application : android.app.Application() {

    override fun onCreate() {
        super.onCreate()
        VKSdk.initialize(this)
    }
}