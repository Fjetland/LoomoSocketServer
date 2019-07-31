package eu.fjetland.loomosocketserver.loomo

import android.content.Context
import android.util.Log
import com.segway.robot.sdk.base.bind.ServiceBinder
import com.segway.robot.sdk.locomotion.sbv.Base
import eu.fjetland.loomosocketserver.data.EnableDrive
import eu.fjetland.loomosocketserver.data.Position
import eu.fjetland.loomosocketserver.data.Velocity
import com.segway.robot.algo.Pose2D
import com.segway.robot.algo.minicontroller.CheckPoint
import com.segway.robot.algo.minicontroller.CheckPointStateListener


class LoomoBase(context: Context) {
    private val TAG = "LoomoBase"

    var mBase = Base.getInstance()

    private var drive = false
    private var isDrivingOnCheckPoints = false

    init {
        mBase.bindService(context.applicationContext, object : ServiceBinder.BindStateListener {
            override fun onBind() {
                Log.d(TAG, "Base onBind")
            }
            override fun onUnbind(reason: String?) {
            }
        })

        mBase.setOnCheckPointArrivedListener(object : CheckPointStateListener {
            override fun onCheckPointArrived(checkPoint: CheckPoint, realPose: Pose2D, isLast: Boolean) {
                if (isLast){
                    Log.i(TAG, "Final Checkpoint Reached")
                }
            }

            override fun onCheckPointMiss(checkPoint: CheckPoint, realPose: Pose2D, isLast: Boolean, reason: Int) {
                if (isLast){
                    Log.i(TAG, "Final Checkpoint Missed")
                }
            }
        })
    }

    fun setEnableDrive(enableDrive: EnableDrive) {
        drive = enableDrive.drive
        if (!drive) {
            mBase.controlMode = Base.CONTROL_MODE_RAW
            isDrivingOnCheckPoints = false
        }
    }

    fun setVelocity(velocity: Velocity){
        if (drive) {
            isDrivingOnCheckPoints = false
            mBase.controlMode = Base.CONTROL_MODE_RAW
            mBase.setLinearVelocity(velocity.v)
            mBase.setAngularVelocity(velocity.av)
        }
    }

    fun setPosition(position: Position){
        if (drive){
            if (position.add && isDrivingOnCheckPoints){

                if (position.th == null){
                    mBase.addCheckPoint(position.x,position.y)
                } else {
                    mBase.addCheckPoint(position.x,position.y,position.th!!)
                }

            } else {
                mBase.setControlMode(Base.CONTROL_MODE_NAVIGATION)

                mBase.cleanOriginalPoint()
                val pose2D = mBase.getOdometryPose(-1)
                mBase.setOriginalPoint(pose2D)

                if (position.th == null){
                    mBase.addCheckPoint(position.x,position.y)
                } else {
                    mBase.addCheckPoint(position.x,position.y,position.th!!)
                }
            }


        }
    }
}