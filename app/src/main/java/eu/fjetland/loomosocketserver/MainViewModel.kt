package eu.fjetland.loomosocketserver

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import eu.fjetland.loomosocketserver.connection.IpHelper
import eu.fjetland.loomosocketserver.data.*

class MainViewModel(app: Application) : AndroidViewModel(app) {

    val myIp = MutableLiveData<String>()
    val clientIp = MutableLiveData<String>()
    val readLog =  MutableLiveData<String>()
    private val context = app

    /**
     * Robot operators
     */
    val head = MutableLiveData<Head?>()
    val velocity = MutableLiveData<Velocity?>()
    val position = MutableLiveData<Position?>()
    val speak = MutableLiveData<Speak?>()
    val volume = MutableLiveData<Volume?>()
    val endableDrive = MutableLiveData<EnableDrive?>()
    val test = MutableLiveData<Boolean>()

    init {
        test.value = false

        Log.i(LOG_TAG, "Debug View Model created")
        myIp.value = "Searching for IP..."
        clientIp.value = "Not Connected"
        readLog.value = "Awaiting remote connection..."
    }

    fun updateMyIp() {
        myIp.value = IpHelper.getDeviceIp(context)
    }

    fun updateClientIp(string: String) {
        clientIp.value = string
    }

    fun updateComLogText(string: String) {
        readLog.value = string + "\n" + readLog.value
    }

    fun fullStoppMessage(){
        readLog.value = "ERROR: Socket port dead. Restart!"
    }

    /**
     * Clear Robot opperators after use
     */
}