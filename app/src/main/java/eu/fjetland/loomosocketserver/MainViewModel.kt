package eu.fjetland.loomosocketserver

import android.app.Application
import android.graphics.Bitmap
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

    val isConnected = MutableLiveData<Boolean>()

    /**
     * Robot operators
     */
    val head = MutableLiveData<Head?>()
    val velocity = MutableLiveData<Velocity?>()
    val position = MutableLiveData<Position?>()
    val positionArray = MutableLiveData<PositionArray>()
    val speak = MutableLiveData<Speak?>()
    val volume = MutableLiveData<Volume?>()
    val endableDrive = MutableLiveData<EnableDrive?>()
    val headLightNotification = MutableLiveData<Int>()

    val visionIsActive = MutableLiveData<Boolean>()
    val activeStreams = MutableLiveData<EnableVision>()

    val realSenseColorImage = MutableLiveData<Bitmap>()
    val realSenseDepthImage = MutableLiveData<Bitmap>()

    val colorLargeBitArray = MutableLiveData<ByteArray>()
    val colorSmallBitArray = MutableLiveData<ByteArray>()
    val colorDepthBitArray = MutableLiveData<ByteArray>()



    init {
        Log.i(LOG_TAG, "Debug View Model created")
        myIp.value = "Searching for IP..."
        clientIp.value = "Not Connected"
        readLog.value = "Awaiting remote connection..."
        isConnected.value = false
        realSenseColorImage.value = Bitmap.createBitmap(640,480,Bitmap.Config.ARGB_8888)
        realSenseDepthImage.value = Bitmap.createBitmap(320,240,Bitmap.Config.RGB_565)
        colorLargeBitArray.value = byteArrayOf(0,0)
        colorSmallBitArray.value = byteArrayOf(0,0)
        colorDepthBitArray.value = byteArrayOf(0,0)

        visionIsActive.value = false
        activeStreams.value = EnableVision(false,false,false)
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


    /**
     * Clear Robot opperators after use
     */
}