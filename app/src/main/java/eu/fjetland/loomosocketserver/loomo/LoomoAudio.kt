package eu.fjetland.loomosocketserver.loomo

import android.content.Context
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.util.Log
import eu.fjetland.loomosocketserver.data.Speak
import eu.fjetland.loomosocketserver.data.Volume
import java.util.*

class LoomoAudio (applicationContext: Context) {
    private val TAG = "LoomoAudio"

    private var context = applicationContext

    private var txt2speakOK = false
    private lateinit var tts : TextToSpeech
    private lateinit var mAudio : AudioManager

    fun onCreate(){
        tts = TextToSpeech(context, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR){
                //if there is no error then set language
                txt2speakOK = true
                tts.language = Locale.US
                Log.i(TAG, "T2T Online")
            } else{
                Log.d(TAG, "T2T Creation Error")
            }
        })

        mAudio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    }

    fun onDelete(){
        try {
            tts.shutdown()
            Log.i(TAG, "T2T Offline")
        } catch (e: Exception) {
            Log.e(TAG,"Shutdown TTS: ", e)
        }
    }

    fun setVolume(volume: Volume) {
        val stream = AudioManager.STREAM_MUSIC
        Log.i(TAG,"Current Volume: ${mAudio.getStreamVolume(stream)}")

        val max = mAudio.getStreamMaxVolume(stream).toDouble()
        val newVolume = max*volume.v
        try {
            mAudio.setStreamVolume(stream,newVolume.toInt(), AudioManager.FLAG_SHOW_UI)
        }catch (e: Exception) {
            Log.e(TAG, "Audio exception: ", e)
        }
    }

    fun speak(speak: Speak) {
        if (txt2speakOK) {
            try {
                val q = when (speak.que) {
                    0 -> TextToSpeech.QUEUE_FLUSH
                    else -> TextToSpeech.QUEUE_ADD
                }
                tts.setPitch(speak.pitch)
                tts.speak(speak.string,q,null,"")

            } catch (e: Exception){
                Log.e(TAG, "Error in T2T: ",e)
            }
        }
    }

}