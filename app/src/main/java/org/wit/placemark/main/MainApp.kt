package org.wit.placemark.main

import android.app.Application
import org.wit.placemark.models.*
import org.wit.placemark.room.PlacemarkStoreRoom
import timber.log.Timber
import timber.log.Timber.i

class MainApp : Application() {

    lateinit var placemarks: PlacemarkStore

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        placemarks = PlacemarkFireStore(applicationContext)
        i("Placemark started")
    }
}