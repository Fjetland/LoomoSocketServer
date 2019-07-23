package eu.fjetland.loomosocketserver

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.*
import android.media.AudioManager
import kotlin.Exception


class Loomo(applicationContext: Context) {
    var textOK = false;
    var context = applicationContext
    val tts = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
        if (status != TextToSpeech.ERROR){
            //if there is no error then set language
            textOK = true }
    })
    var mAudio= context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    init {
        if (textOK) tts.language = Locale.US
        else Log.w(LOG_TAG,"Text To Speak not initialized")
        Log.i(LOG_TAG, "Loomo class initialized")


    }

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
        val volume = max*vol;
        try {
            mAudio.setStreamVolume(stream,volume.toInt(), AudioManager.FLAG_SHOW_UI)
        } catch (e: Exception){

        }


    }

}