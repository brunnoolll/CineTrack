package com.movies.cinetrack.core.diagnostics

import android.util.Log
import javax.inject.Inject

class AndroidLogger @Inject constructor() : Logger {
    override fun debug(message: String, cause: Throwable?) {
        Log.d("CineTrack", message, cause)
    }

    override fun info(message: String, cause: Throwable?) {
        Log.i("CineTrack", message, cause)
    }

    override fun error(message: String, cause: Throwable?) {
        Log.e("CineTrack", message, cause)
    }
}