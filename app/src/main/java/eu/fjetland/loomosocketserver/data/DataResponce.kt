package eu.fjetland.loomosocketserver.data

import eu.fjetland.loomosocketserver.loomo.LoomoRealSense
import org.json.JSONObject

class DataResponce {
    companion object {
        /**
         *  Data return types
         *
         *  All new return structures must be registerd in list
         *  at the end of the companion object to be actionable trugh tcp
         */
        const val DATA_LBL = "dat"
        const val TIME_LBL = "time"

        const val IMAGE = "img"
        const val IMAGE_TYPE = "type"
        const val IMAGE_TYPE_COLOR = "Color"
        const val IMAGE_TYPE_COLOR_SMALL = "ColorSmall"
        const val IMAGE_TYPE_DEPTH = "Depth"

        const val IMAGE_SIZE = "size"
        const val IMAGE_WIDTH = "width"
        const val IMAGE_HEIGHT = "height"

        const val SURROUNDINGS = "sSur"
        const val SURROUNDINGS_IRLEFT = "irl"
        const val SURROUNDINGS_IRRIGHT = "irr"
        const val SURROUNDINGS_ULTRASONIC = "uss"

        const val WHEEL_SPEED = "sWS"
        const val WHEEL_SPEED_L = "vl"
        const val WHEEL_SPEED_R = "vr"

        const val POSE2D = "sP2d"
        const val POSE2D_X = "x"
        const val POSE2D_Y = "y"
        const val POSE2D_TH = "th"
        const val POSE2D_VL = "vl"
        const val POSE2D_VA = "va"

        const val HEAD_WORLD = "sHPw"
        const val HEAD_JOINT = "sHPj"
        const val HEAD_PITCH = "p"
        const val HEAD_ROLL = "r"
        const val HEAD_YAW = "y"

        const val BASE_IMU = "sBP"
        const val BASE_IMU_PITCH = "p"
        const val BASE_IMU_ROLL = "r"
        const val BASE_IMU_YAW = "y"

        const val BASE_TICK = "sBT"
        const val BASE_TICK_L = "l"
        const val BASE_TICK_R = "r"

        val DATALIST = listOf(SURROUNDINGS, WHEEL_SPEED, POSE2D,
                                HEAD_WORLD, HEAD_JOINT, BASE_IMU, BASE_TICK, IMAGE)
        /**
         *  Data to json
         */
        fun sensSurroundings2JSON(data: SensSurroundings) : JSONObject{
            val json = JSONObject()
            json.put(DATA_LBL, data.label)
            json.put(SURROUNDINGS_IRLEFT, data.IR_Left)
            json.put(SURROUNDINGS_IRRIGHT, data.IR_Right)
            json.put(SURROUNDINGS_ULTRASONIC, data.UltraSonic)
            return json
        }

        fun sensWheelSpeed2JSON(data : SensWheelSpeed) : JSONObject {
            val json = JSONObject()
            json.put(DATA_LBL, data.label)
            json.put(WHEEL_SPEED_L, data.SpeedLeft)
            json.put(WHEEL_SPEED_R, data.SpeedRight)

            return json
        }

        fun sensHeadPoseWorld2JSON(data: SensHeadPoseWorld) : JSONObject {
            val json = JSONObject()
            json.put(DATA_LBL, HEAD_WORLD)
            json.put(HEAD_PITCH, data.pitch)
            json.put(HEAD_ROLL, data.roll)
            json.put(HEAD_YAW, data.yaw)

            return json
        }

        fun sensHeadPoseJoint2JSON(data: SensHeadPoseJoint) : JSONObject {
            val json = JSONObject()
            json.put(DATA_LBL, HEAD_JOINT)
            json.put(HEAD_PITCH, data.pitch)
            json.put(HEAD_ROLL, data.roll)
            json.put(HEAD_YAW, data.yaw)

            return json
        }

        fun sensBasePose2JSON(data : SensBaseImu) : JSONObject {
            val json = JSONObject()
            json.put(DATA_LBL, BASE_IMU)
            json.put(BASE_IMU_PITCH, data.pitch)
            json.put(BASE_IMU_ROLL, data.roll)
            json.put(BASE_IMU_YAW, data.yaw)

            return json
        }

        fun sensBaseTick2JSON(data : SensBaseTick) : JSONObject {
            val json = JSONObject()
            json.put(DATA_LBL, BASE_TICK)
            json.put(BASE_TICK_L, data.left)
            json.put(BASE_TICK_R, data.right)

            return json
        }

        fun sensPose2D2JSON(data : SensPose2D) : JSONObject {
            val json = JSONObject()
            json.put(DATA_LBL, POSE2D)
            json.put(POSE2D_X, data.x)
            json.put(POSE2D_Y, data.y)
            json.put(POSE2D_TH, data.theta)
            json.put(POSE2D_VL, data.linearVelocity)
            json.put(POSE2D_VA, data.angularVelocity)

            return json
        }

        /**
         * data 2 json string
         */

        fun sensImage2JSONstring(data: ImageResponse) : String{
            val json = JSONObject()
            json.put(DATA_LBL, IMAGE)
            json.put(IMAGE_SIZE, data.size)
            json.put(IMAGE_TYPE, data.type)
            json.put(IMAGE_WIDTH, data.width)
            json.put(IMAGE_HEIGHT, data.height)
            return json.toString()
        }

    }
}

data class SensSurroundings(
    val IR_Left : Int,
    val IR_Right : Int,
    val UltraSonic : Int,
    val label : String = DataResponce.SURROUNDINGS)

data class SensWheelSpeed(
    val SpeedLeft : Float,
    val SpeedRight : Float,
    val label : String = DataResponce.WHEEL_SPEED
)

data class SensHeadPoseWorld(
    val pitch : Float,
    val roll : Float,
    val yaw : Float,
    val label : String = DataResponce.HEAD_WORLD
)

data class SensHeadPoseJoint(
    val pitch : Float,
    val roll : Float,
    val yaw : Float,
    val label : String = DataResponce.HEAD_JOINT
)

data class SensBaseImu(
    val pitch : Float,
    val roll : Float,
    val yaw : Float,
    val label : String = DataResponce.BASE_IMU
)

data class SensBaseTick(
    val left : Int,
    val right : Int,
    val label : String = DataResponce.BASE_TICK
)

data class SensPose2D(
    val x : Float,
    val y : Float,
    val theta : Float,
    val linearVelocity : Float,
    val angularVelocity: Float,
    val label : String = DataResponce.POSE2D
)

data class ImageResponse(
    var type : String = DataResponce.IMAGE_TYPE_COLOR_SMALL,
    var size : Int = 0,
    var width : Int = 0,
    var height : Int = 0,
    val label : String = DataResponce.IMAGE
) {
    init {
        if (type == DataResponce.IMAGE_TYPE_COLOR) {
            width = LoomoRealSense.COLOR_WIDTH
            height = LoomoRealSense.COLOR_HEIGHT
        } else {
            width = LoomoRealSense.SMALL_COLOR_WIDTH
            height = LoomoRealSense.SMALL_COLOR_HEIGHT
        }
    }
}