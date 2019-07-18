package eu.fjetland.loomosocketserver

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import eu.fjetland.loomosocketserver.connection.IpHelper

class DebugViewModel(app: Application) : AndroidViewModel(app) {

    val myIp = MutableLiveData<String>()
    val clientIp = MutableLiveData<String>()
    val readLog =  MutableLiveData<String>()
    private val context = app

    init {
        Log.i(LOG_TAG, "Debug View Model created")
        myIp.value = "Searching for IP..."
        clientIp.value = "Not Connected"
        readLog.value = "Awaiting remote connection..."
    }

    fun updateMyIp() {
        myIp.value = IpHelper.getDeviceIp(context)

    }

}