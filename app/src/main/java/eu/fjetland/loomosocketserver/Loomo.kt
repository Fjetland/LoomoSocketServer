package eu.fjetland.loomosocketserver

import android.content.Context
import android.speech.tts.TextToSpeech
import org.json.JSONObject
import android.util.Log
import java.util.*
import android.media.AudioManager
import com.segway.robot.sdk.perception.sensor.Sensor
import kotlin.Exception
import com.segway.robot.sdk.base.bind.ServiceBinder
import com.segway.robot.sdk.locomotion.head.Head
import com.segway.robot.sdk.locomotion.sbv.Base
import com.segway.robot.sdk.vision.Vision
import eu.fjetland.loomosocketserver.connection.ActionID
import eu.fjetland.loomosocketserver.connection.ResponsID
import com.segway.robot.sdk.vision.stream.StreamInfo
import android.graphics.Bitmap
import com.segway.robot.sdk.vision.frame.Frame
import com.segway.robot.sdk.vision.stream.StreamType
import com.segway.robot.sdk.vision.stream.StreamType.DEPTH








class Loomo(applicationContext: Context) {
    private var textOK = false
    private var context = applicationContext
    private val tts = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
        if (status != TextToSpeech.ERROR){
            //if there is no error then set language
            textOK = true }
    })
    private var mAudio= context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var mSensor = Sensor.getInstance()
    private var mHead = Head.getInstance()
    var mBase = Base.getInstance()
//    private var mVision = Vision.getInstance()
//
//    private val mBitmap = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888)


    init {
        if (textOK) tts.language = Locale.US
        else Log.w(LOG_TAG,"Text To Speak not initialized")
        Log.i(LOG_TAG, "Loomo class initialized")

        mSensor.bindService(context.applicationContext, object : ServiceBinder.BindStateListener {
            override fun onBind() {
                Log.d(LOG_TAG, "sensor onBind")
            }
            override fun onUnbind(reason: String) {
            }
        })

        mHead.bindService(context.applicationContext, object : ServiceBinder.BindStateListener {
            override fun onBind() {
                Log.d(LOG_TAG, "Head onBind")
            }
            override fun onUnbind(reason: String?) {
            }
        })

        mBase.bindService(context.applicationContext, object : ServiceBinder.BindStateListener {
            override fun onBind() {
                Log.d(LOG_TAG, "Base onBind")
            }
            override fun onUnbind(reason: String?) {
            }
        })
//        mVision.bindService(context.applicationContext, object : ServiceBinder.BindStateListener {
//            override fun onBind() {
//                Log.d(LOG_TAG, "Vision onBind")
//            }
//            override fun onUnbind(reason: String?) {
//            }
//        })

      }

    fun onDelete(){
        try {
            tts.shutdown()
        } catch (e: Exception) {
            Log.e(LOG_TAG,"Shutdown TTS: ", e)
        }

    }

//    fun onLater(){
//        mVision.startListenFrame(StreamType.DEPTH, object : Vision.FrameListener {
//            override fun onNewFrame(streamType: Int, frame: Frame) {
//                mBitmap.copyPixelsFromBuffer(frame.getByteBuffer())
//            }
//        })
//    }

    fun speak(string: String, que: Int, pitch: Double) {
        try {
            val q = when (que) {
                1 -> TextToSpeech.QUEUE_FLUSH
                2 -> TextToSpeech.QUEUE_ADD
                else -> TextToSpeech.QUEUE_FLUSH
            }
            tts.setPitch(pitch.toFloat())
            tts.speak(string, q, null,"")
        } catch (e: Exception) {
            Log.e(LOG_TAG,"Exeption: ", e)
        }
    }
    fun setVolume(getVol: Double) {
        val vol = when {
            getVol>1 -> 1.0
            getVol<0 -> 0.1
            else -> getVol
        }
        val stream = AudioManager.STREAM_MUSIC
        Log.i(LOG_TAG,"Current Volume: ${mAudio.getStreamVolume(stream)}")
        //val min = mAudio.getStreamMinVolume(stream)
        val max = mAudio.getStreamMaxVolume(stream).toDouble()
        val volume = max*vol
        try {
            mAudio.setStreamVolume(stream,volume.toInt(), AudioManager.FLAG_SHOW_UI)
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Audio exception: ", e)
        }
    }

    fun headPosition(json :JSONObject) {
        if (json.getString(ActionID.ACTION).equals(ActionID.HEAD)) {
            val pitch = json.getDouble(ActionID.HEAD_PITCH).toFloat()
            val yaw = json.getDouble(ActionID.HEAD_YAW).toFloat()
            mHead.mode = Head.MODE_SMOOTH_TACKING
            mHead.setWorldPitch(pitch)
            mHead.setWorldYaw(yaw)
            if (json.has(ActionID.HEAD_LIGHT)){
                mHead.setHeadLightMode(json.getInt(ActionID.HEAD_LIGHT)) }
        }else {
            Log.e(LOG_TAG,"Not a head command")
        }
    }

    fun setSpeeds(json: JSONObject){
        val avMax = mBase.angularVelocityLimit
        val vMax = mBase.linearVelocityLimit
        Log.i(LOG_TAG,"Velocity limit: $vMax, angularVelocityLimit: $avMax")
        val avSet = json.getDouble(ActionID.VELOCITY_ANGULAR).toFloat()
        val vSet = json.getDouble(ActionID.VELOCITY_LINEAR).toFloat()
        mBase.setAngularVelocity(avSet)
        mBase.setLinearVelocity(vSet)
    }

    fun testFun(){
        //mVision.g
    }

    fun returnSurroundings() :String{
        val json = JSONObject()
        json.put(ResponsID.DATA_LBL,ResponsID.DATA_SURROUNDINGS_ID)
        json.put(ResponsID.DATA_TIME_LBL,mSensor.infraredDistance.timestamp)
        json.put(ResponsID.DATA_SURROUNDINGS_IRLEFT,mSensor.infraredDistance.leftDistance.toInt())
        json.put(ResponsID.DATA_SURROUNDINGS_IRRIGHT,mSensor.infraredDistance.rightDistance.toInt())
        json.put(ResponsID.DATA_SURROUNDINGS_IRRIGHT,mSensor.infraredDistance.rightDistance.toInt())
        json.put(ResponsID.DATA_SURROUNDINGS_ULTRASONIC,mSensor.ultrasonicDistance.distance)
        Log.i(LOG_TAG,json.toString())

        return json.toString()
    }

}