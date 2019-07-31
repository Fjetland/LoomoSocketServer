package eu.fjetland.loomosocketserver.loomo

/**
 * Note: The unit of Distance is the millimeter. The unit of Angle is the radian.
 * The unit of LinearVelocity is meters per second. The unit of AngularVelcity
 * is radians per second. The unit of LeftTicks and RightTicks is Tick,
 * which equals one centimeter when the tires are properly inflated.
 *
 * Note: There is a known issue that when the distance between the obstacle and the
 * ultrasonic sensor is less than 250 millimeters, an incorrect value may be returned.
 *
 * Note: When you call robotTotalInfo.getHeadWorldYaw().getAngle() to get the value of
 * WorldYaw, it will always returns 0.0f since it is not yet supported in the current version.
 */

import android.content.Context
import android.util.Log
import com.segway.robot.sdk.base.bind.ServiceBinder
import com.segway.robot.sdk.perception.sensor.Sensor
import eu.fjetland.loomosocketserver.data.*


class LoomoSensor (context: Context) {
    private val TAG = "LoomoSensor"

    private var mSensor = Sensor.getInstance()

    //var surroundings  = getSurroundings()

    init {
        mSensor.bindService(context.applicationContext, object : ServiceBinder.BindStateListener {
            override fun onBind() {
                Log.d(TAG, "sensor onBind")
            }
            override fun onUnbind(reason: String) {
            }
        })


    }

     fun getSurroundings() : SensSurroundings {
         val mInfraredData = mSensor.querySensorData(listOf(Sensor.INFRARED_BODY))[0]
         val mUltrasonicData = mSensor.querySensorData(listOf(Sensor.ULTRASONIC_BODY))[0]
         return SensSurroundings(
             IR_Left = mInfraredData.intData[0],
             IR_Right = mInfraredData.intData[1],
             UltraSonic = mUltrasonicData.intData[0]
         )
     }

    fun getWheelSpeed() : SensWheelSpeed {
        val mWheelSpeed = mSensor.querySensorData(listOf(Sensor.WHEEL_SPEED))[0]
        return SensWheelSpeed(
            SpeedLeft = mWheelSpeed.intData[0].toFloat(),
            SpeedRight = mWheelSpeed.intData[1].toFloat()
        )
    }

    fun getHeadPoseWorld() : SensHeadPoseWorld {
        val mHeadImu = mSensor.querySensorData(listOf(Sensor.HEAD_WORLD_IMU))[0]
        return SensHeadPoseWorld(
            pitch = mHeadImu.floatData[0],
            roll = mHeadImu.floatData[1],
            yaw = mHeadImu.floatData[2]
        )
    }

    fun getHeadPoseJoint() : SensHeadPoseJoint {
        val mHeadPitch = mSensor.querySensorData(listOf(Sensor.HEAD_JOINT_PITCH))[0]
        val mHeadYaw = mSensor.querySensorData(listOf(Sensor.HEAD_JOINT_YAW))[0]
        val mHeadRoll = mSensor.querySensorData(listOf(Sensor.HEAD_JOINT_ROLL))[0]

        return SensHeadPoseJoint(
            pitch = mHeadPitch.floatData[0],
            roll = mHeadRoll.floatData[0],
            yaw = mHeadYaw.floatData[0]
        )
    }

    fun getSensBasePose() : SensBasePose {
        val mBaseImu = mSensor.querySensorData(listOf(Sensor.BASE_IMU))[0]

        return SensBasePose(
            pitch = mBaseImu.floatData[0],
            roll = mBaseImu.floatData[1],
            yaw = mBaseImu.floatData[2]
        )
    }

    fun getSensBaseTick() : SensBaseTick {
        val mBaseTick = mSensor.querySensorData(listOf(Sensor.ENCODER))[0]

        return SensBaseTick(
            left = mBaseTick.intData[0],
            right = mBaseTick.intData[1]
        )
    }

    fun getSensPose2D() : SensPose2D {
        val mPose2DData = mSensor.querySensorData(listOf(Sensor.POSE_2D))[0]
        val pose2D = mSensor.sensorDataToPose2D(mPose2DData)

        return SensPose2D(
            x = pose2D.x,
            y = pose2D.y,
            theta = pose2D.theta,
            linearVelocity = pose2D.linearVelocity,
            angularVelocity = pose2D.angularVelocity
        )
    }

}