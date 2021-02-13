package com.example.testmydagger

import android.app.Application

class MyApp : Application() {
    var myComponent: MyComponent? = null
        private set

    override fun onCreate() {
        super.onCreate()
//        myComponent = DaggerMyComponent.create()
        myComponent = DaggerMyComponent.builder().contextModule(ContextModule(this)).build()
    }
}



