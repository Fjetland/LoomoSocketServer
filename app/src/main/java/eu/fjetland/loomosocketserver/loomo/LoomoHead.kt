package eu.fjetland.loomosocketserver.loomo

import android.content.Context
import android.util.Log
import com.segway.robot.sdk.base.bind.ServiceBinder
import com.segway.robot.sdk.locomotion.head.Head
import eu.fjetland.loomosocketserver.data.Action
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import eu.fjetland.loomosocketserver.data.Head as myHead

class LoomoHead (context: Context){

    companion object {

        val LIGHT_OFF = 0
        val LIGHT_BLUE = 1
        val LIGHT_BLUE_SPINN = 2
        val LIGHT_BLUE_WHITE_ROTATE = 3
        val LIGHT_BLUE_WHITE_PULSE = 4

        val LIGHT_RED_FIVE_PULSES = 5
        val LIGHT_GREEN_FIVE_PULSES = 6

        val LIGHT_GREEN_SLOW = 7
        val LIGHT_ORANGE_SLOW = 8
        val LIGHT_BLUE_SLOW = 9

        val LIGHT_PURPLE_WHITE_ROTATE = 10
        val LIGHT_PURPLE_WHITE_PULSE = 11

        val LIGHT_BLUE_PULSE = 12
        val LIGHT_WHITE_ROTATE = 13
    }

    private val TAG = "LoomoHead"
    private var mHead = Head.getInstance()

    private var myEarLight = LIGHT_OFF

    init {
        mHead.bindService(context.applicationContext, object : ServiceBinder.BindStateListener {
            override fun onBind() {
                Log.d(TAG, "Head onBind")
            }
            override fun onUnbind(reason: String?) {
            }
        })
    }

    fun setHead(head: myHead) {
        Log.i(TAG,"$head")
        if (head.mode == Action.HEAD_SET_SMOOTH) {
            mHead.mode = Head.MODE_SMOOTH_TACKING
            mHead.setWorldYaw(head.yaw)
            mHead.setWorldPitch(head.pitch)
        } else {

            mHead.mode = Head.MODE_ORIENTATION_LOCK
            mHead.setYawAngularVelocity(head.yaw)
            mHead.setPitchAngularVelocity(head.pitch)
        }
        if (head.li != null) {
            mHead.setHeadLightMode(head.li!!)
        }
    }

    fun setHeadLight(int: Int) {
        mHead.setHeadLightMode(int)
        myEarLight = int
    }

    fun setEnableDriveLight(enableDrive : Boolean){
        if (enableDrive){
            setHeadLight(LIGHT_PURPLE_WHITE_ROTATE)
        } else {
            setHeadLight(LIGHT_BLUE)
        }
    }

    fun setConnectedLight(boolean: Boolean){
        GlobalScope.launch {
            while (!mHead.isBind){
                delay(10L)
            }
            if (boolean) {
                setHeadLight(LIGHT_GREEN_FIVE_PULSES)
                delay(1000L)
                setHeadLight(LIGHT_BLUE)
            } else {
                setHeadLight(LIGHT_RED_FIVE_PULSES)
                delay(1200L)
                setHeadLight(LIGHT_ORANGE_SLOW)
            }
        }
    }

    fun setStartupLight(){
        GlobalScope.launch {
            while (!mHead.isBind){
                delay(10L)
            }
            setHeadLight(LIGHT_BLUE_SPINN)
            delay(1200L)
            setHeadLight(LIGHT_ORANGE_SLOW)
        }
    }

    fun headLightNotification(light : Int){
        val oldLight = myEarLight
        GlobalScope.launch {
            setHeadLight(light)
            delay(1200L)
            setHeadLight(oldLight)
        }
    }

}