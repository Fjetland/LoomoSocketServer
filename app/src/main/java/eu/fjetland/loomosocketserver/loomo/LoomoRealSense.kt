package eu.fjetland.loomosocketserver.loomo

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.lifecycle.ViewModel
import com.segway.robot.sdk.base.bind.ServiceBinder
import com.segway.robot.sdk.vision.Vision
import com.segway.robot.sdk.vision.frame.Frame
import com.segway.robot.sdk.vision.stream.StreamInfo
import com.segway.robot.sdk.vision.stream.StreamType
import kotlinx.coroutines.*


class LoomoRealSense (context: Context, val viewModel: ViewModel){
    val TAG = "LoomoRealSense"

    var mVision : Vision = Vision.getInstance()
    var isActive = false;

    var mImageColor : Bitmap = Bitmap.createBitmap(640,480,Bitmap.Config.ARGB_8888)

    init {
        mVision.bindService(context.applicationContext, object : ServiceBinder.BindStateListener {
            override fun onBind() {
                Log.d(TAG, "Vision onBind")
            }
            override fun onUnbind(reason: String?) {
            }
        })

        //val infos : Array<out StreamInfo>? = mVision.activatedStreamInfo
        Log.i(TAG,"Vision isBound: ${mVision.isBind}")
    }

    fun startCamera(){
        GlobalScope.launch { // launch a new coroutine in background and continue
            while (!mVision.isBind) {
                delay(10L) // non-blocking delay for 1 second (default time unit is ms)
            }

            mVision.startListenFrame(StreamType.COLOR, object : Vision.FrameListener {
            override fun onNewFrame(streamType: Int, frame: Frame) {
                mImageColor.copyPixelsFromBuffer(frame.byteBuffer)
            }
            })
            this@LoomoRealSense.isActive = true
            //viewModel.test.

            Log.i(TAG,"Vision isBound: ${mVision.isBind}")
        }
    }
}