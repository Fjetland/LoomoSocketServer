package eu.fjetland.loomosocketserver.loomo

import android.content.Context
import android.util.Log
import com.segway.robot.sdk.base.bind.ServiceBinder
import com.segway.robot.sdk.locomotion.sbv.Base
import com.segway.robot.algo.Pose2D
import com.segway.robot.algo.minicontroller.CheckPoint
import com.segway.robot.algo.minicontroller.CheckPointStateListener
import eu.fjetland.loomosocketserver.SafetyLimits
import com.segway.robot.algo.PoseVLS
import com.segway.robot.algo.minicontroller.ObstacleStateChangedListener
import com.segway.robot.sdk.locomotion.sbv.StartVLSListener
import eu.fjetland.loomosocketserver.data.*
import eu.fjetland.loomosocketserver.updateConversationHandler
import eu.fjetland.loomosocketserver.viewModel


class LoomoBase(context: Context) {
    private val TAG = "LoomoBase"

    var mBase = Base.getInstance()

    private var drive = false
    private var isDrivingOnCheckPoints = false
    private var isDrivingOnVls = false

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
                    updateConversationHandler.post {
                        viewModel.headLightNotification.value = LoomoHead.LIGHT_GREEN_FIVE_PULSES
                    }

                } else {
                    updateConversationHandler.post {
                        viewModel.headLightNotification.value = LoomoHead.LIGHT_GREEN_FIVE_PULSES
                    }
                }
            }

            override fun onCheckPointMiss(checkPoint: CheckPoint, realPose: Pose2D, isLast: Boolean, reason: Int) {
                if (isLast){
                    Log.i(TAG, "Final Checkpoint Missed")
                    updateConversationHandler.post {
                        viewModel.headLightNotification.value = LoomoHead.LIGHT_RED_FIVE_PULSES
                    }
                } else {
                    updateConversationHandler.post {
                        viewModel.headLightNotification.value = LoomoHead.LIGHT_ORANGE_SLOW
                    }
                }
            }
        })

        mBase.setObstacleStateChangeListener(object : ObstacleStateChangedListener {
            override fun onObstacleStateChanged(ObstacleAppearance: Int) {

                if (ObstacleAppearance == 1) {
                    Log.i(TAG,"Obstacle detected")
                    updateConversationHandler.post {
                        viewModel.speak.value = Speak(0,string = "You are in my way, please move")
                        viewModel.headLightNotification.value = LoomoHead.LIGHT_RED_FIVE_PULSES
                    }
                } else {
                    Log.i(TAG,"Obstacle dissapared")
                    updateConversationHandler.post {
                        viewModel.speak.value = Speak(0,string = "Thank you")
                        viewModel.headLightNotification.value = LoomoHead.LIGHT_RED_FIVE_PULSES
                    }
                }
            }
        })

    }

    fun setEnableDrive(enableDrive: EnableDrive) {
        drive = enableDrive.drive
        if (!drive) {
            Log.i(TAG, "Control mode set to raw | driveEnabled: $drive")

            if (mBase.controlMode == Base.CONTROL_MODE_NAVIGATION) {
                if (SafetyLimits.USE_OBSTACLE_AVOIDANCE) {
                    mBase.isUltrasonicObstacleAvoidanceEnabled = false // Unknown if this help mitigate obstacle bug
                }
                mBase.clearCheckPointsAndStop()
                if (isDrivingOnVls) {
                    mBase.stopVLS()
                    isDrivingOnVls = false
                }
                isDrivingOnCheckPoints = false
            }
            mBase.controlMode = Base.CONTROL_MODE_RAW
            mBase.stop()
            isDrivingOnCheckPoints = false
            isDrivingOnVls = false
        }
    }

    fun setVelocity(velocity: Velocity){
        if (drive) {
            var linVel = velocity.v
            if (linVel > SafetyLimits.LINEAR_VELOCITY_LIMIT){
                linVel = SafetyLimits.LINEAR_VELOCITY_LIMIT
            }

            var angVel = velocity.av
            if (angVel > SafetyLimits.ANGULAR_VELOCITY_LIMIT){
                angVel = SafetyLimits.ANGULAR_VELOCITY_LIMIT
            }

            if (mBase.controlMode == Base.CONTROL_MODE_NAVIGATION) {
                mBase.clearCheckPointsAndStop()
                if (isDrivingOnVls) {
                    mBase.stopVLS()
                    isDrivingOnVls = false
                }
                isDrivingOnCheckPoints = false
            }


            if (mBase.controlMode != Base.CONTROL_MODE_RAW) {
                mBase.controlMode = Base.CONTROL_MODE_RAW
            }

            mBase.setLinearVelocity(linVel)
            mBase.setAngularVelocity(angVel)
        }
    }

    fun setPosition(position: Position){
        if (drive){

            /**
             *  Cancle any lingering position commands
             */

            if (mBase.controlMode == Base.CONTROL_MODE_NAVIGATION) {
                mBase.clearCheckPointsAndStop()
            }
            if (isDrivingOnVls) {
                mBase.stopVLS()
                isDrivingOnVls = false
            }
            isDrivingOnCheckPoints = false

            /**
             * Issue new commands
             */

            mBase.controlMode = Base.CONTROL_MODE_NAVIGATION
            if (SafetyLimits.USE_OBSTACLE_AVOIDANCE) {
                mBase.ultrasonicObstacleAvoidanceDistance = 0.6f
                mBase.isUltrasonicObstacleAvoidanceEnabled = true
            }

            if (!position.vls) {
                isDrivingOnCheckPoints = true


                mBase.cleanOriginalPoint()
                val pose2D = mBase.getOdometryPose(-1)
                mBase.setOriginalPoint(pose2D)

                if (position.th == null) {
                    mBase.addCheckPoint(position.x, position.y)
                } else {
                    mBase.addCheckPoint(position.x, position.y, position.th!!)
                }
            }
            else {
                isDrivingOnVls = true
                Log.i(TAG,"Creating VLS Navigation")
                // start VLS
                mBase.startVLS(true, true, object : StartVLSListener {
                    override fun onOpened() {
                        // set navigation data source
                        mBase.setNavigationDataSource(Base.NAVIGATION_SOURCE_TYPE_VLS)
                        mBase.cleanOriginalPoint()
                        val poseVLS = mBase.getVLSPose(-1)
                        mBase.setOriginalPoint(poseVLS as Pose2D)

                        if (position.th == null) {
                            mBase.addCheckPoint(position.x, position.y)
                        } else {
                            mBase.addCheckPoint(position.x, position.y, position.th!!)
                        }
                        Log.i(TAG,"Running VLS Navigation")
                    }

                    override fun onError(errorMessage: String) {
                        Log.d(TAG, "onError() called with: errorMessage = [$errorMessage]")
                    }
                })

            }
        }
    }

    fun setPositionArray(position: PositionArray){
        if (drive){

            /**
             *  Cancle any lingering position commands
             */

            if (mBase.controlMode == Base.CONTROL_MODE_NAVIGATION) {
                mBase.clearCheckPointsAndStop()
            }
            if (isDrivingOnVls) {
                mBase.stopVLS()
                isDrivingOnVls = false
            }
            isDrivingOnCheckPoints = false

            /**
             * Issue new commands
             */

            mBase.controlMode = Base.CONTROL_MODE_NAVIGATION
            if (SafetyLimits.USE_OBSTACLE_AVOIDANCE) {
                mBase.ultrasonicObstacleAvoidanceDistance = 0.6f
                mBase.isUltrasonicObstacleAvoidanceEnabled = true
            }

            if (!position.vls) {
                isDrivingOnCheckPoints = true


                mBase.cleanOriginalPoint()
                val pose2D = mBase.getOdometryPose(-1)
                mBase.setOriginalPoint(pose2D)

                val length = position.x.size
                if (position.th == null) {
                    for (stepp in 0 until length) {
                        Log.i(TAG,"Stepp Added: X: ${position.x[stepp]}, Y: ${position.y[stepp]}")
                        mBase.addCheckPoint(position.x[stepp],position.y[stepp])
                    }
                } else {
                    for (stepp in 0 until length) {
                        Log.i(TAG,"Stepp Added: X: ${position.x[stepp]}, Y: ${position.y[stepp]}, th: ${position.th!![stepp]}")
                        mBase.addCheckPoint(position.x[stepp],position.y[stepp], position.th!![stepp])
                    }
                }
            }
            else {
                isDrivingOnVls = true
                Log.i(TAG,"Creating VLS Navigation")
                // start VLS
                mBase.startVLS(true, true, object : StartVLSListener {
                    override fun onOpened() {
                        // set navigation data source
                        mBase.setNavigationDataSource(Base.NAVIGATION_SOURCE_TYPE_VLS)
                        mBase.cleanOriginalPoint()
                        val poseVLS = mBase.getVLSPose(-1)
                        mBase.setOriginalPoint(poseVLS as Pose2D)

                        val length = position.x.size
                        if (position.th == null) {
                            for (stepp in 0 until length) {
                                mBase.addCheckPoint(position.x[stepp],position.y[stepp])
                            }
                        } else {
                            for (stepp in 0 until length) {
                                mBase.addCheckPoint(position.x[stepp],position.y[stepp], position.th!![stepp])
                            }
                        }
                        Log.i(TAG,"Running VLS Navigation")
                    }

                    override fun onError(errorMessage: String) {
                        Log.d(TAG, "onError() called with: errorMessage = [$errorMessage]")
                    }
                })

            }
        }
    }
}