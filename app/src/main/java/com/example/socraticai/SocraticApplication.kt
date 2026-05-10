package com.example.socraticai

import android.app.Application
import com.example.socraticai.data.ObjectBox

class SocraticApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ObjectBox.init(this)
    }
}
