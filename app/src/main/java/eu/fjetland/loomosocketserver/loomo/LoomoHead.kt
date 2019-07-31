package eu.fjetland.loomosocketserver.loomo

import android.app.Application
import android.content.Context
import android.util.Log
import com.segway.robot.sdk.base.bind.ServiceBinder
import com.segway.robot.sdk.locomotion.head.Head
import eu.fjetland.loomosocketserver.data.Action
import eu.fjetland.loomosocketserver.data.Head as myHead

class LoomoHead (context: Context){

    private val TAG = "LoomoHead"
    private var mHead = Head.getInstance()

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
        if (head.mode == Action.HEAD_SET_SMOOTH) {
            mHead.mode = Head.MODE_SMOOTH_TACKING
            mHead.setWorldYaw(head.pitch)
            mHead.setWorldPitch(head.yaw)
        } else {
            mHead.mode = Head.MODE_ORIENTATION_LOCK
            mHead.setYawAngularVelocity(head.yaw)
            mHead.setPitchAngularVelocity(head.pitch)
        }
        mHead.setHeadLightMode(head.li)
    }
}