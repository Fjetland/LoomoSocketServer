package eu.fjetland.loomosocketserver.data

import org.json.JSONObject

class DataResponce {
    companion object {
        // Data returns
        val DATA_LBL = "dat"
        val TIME_LBL = "time"


        val SURROUNDINGS = "sSur"
        val SURROUNDINGS_IRLEFT = "irl"
        val SURROUNDINGS_IRRIGHT = "irr"
        val SURROUNDINGS_ULTRASONIC = "uss"

        val WHEEL_SPEED = "sWS"
        val WHEEL_SPEED_L = "vl"
        val WHEEL_SPEED_R = "vr"

        val POSE2D = "sP2d"
        val POSE2D_X = "x"
        val POSE2D_Y = "y"
        val POSE2D_TH = "th"
        val POSE2D_VL = "vl"
        val POSE2D_VA = "va"

        val HEAD_WORLD = "sHPw"
        val HEAD_JOINT = "sHPj"
        val HEAD_PITCH = "p"
        val HEAD_ROLL = "r"
        val HEAD_YAW = "y"

        val BASE_POSE = "sBP"
        val BASE_POSE_PITCH = "p"
        val BASE_POSE_ROLL = "r"
        val BASE_POSE_YAW = "p"

        val BASE_TICK = "sBT"
        val BASE_TICK_L = "l"
        val BASE_TICK_R = "r"

        val DATALIST = listOf(SURROUNDINGS, WHEEL_SPEED, POSE2D,
                                HEAD_WORLD, HEAD_JOINT, BASE_POSE, BASE_TICK)



    fun sensSurroundings2JSONstring(data: SensSurroundings) : String{
        val json = JSONObject()
        json.put(DATA_LBL, data.label)
        json.put(SURROUNDINGS_IRLEFT, data.IR_Left)
        json.put(SURROUNDINGS_IRRIGHT, data.IR_Right)
        json.put(SURROUNDINGS_ULTRASONIC, data.UltraSonic)
        return json.toString()
    }

    fun sensWheelSpeed2JSONstring(data : SensWheelSpeed) : String {
        val json = JSONObject()
        json.put(DATA_LBL, data.label)
        json.put(WHEEL_SPEED_L, data.SpeedLeft)
        json.put(WHEEL_SPEED_R, data.SpeedRight)

        return json.toString()
    }

    fun sensHeadPoseWorld2JSONstring(data: SensHeadPoseWorld) : String {
        val json = JSONObject()
        json.put(DATA_LBL, HEAD_WORLD)
        json.put(HEAD_PITCH, data.pitch)
        json.put(HEAD_ROLL, data.roll)
        json.put(HEAD_YAW, data.yaw)

        return json.toString()
    }

    fun sensHeadPoseJoint2JSONstring(data: SensHeadPoseJoint) : String {
        val json = JSONObject()
        json.put(DATA_LBL, HEAD_JOINT)
        json.put(HEAD_PITCH, data.pitch)
        json.put(HEAD_ROLL, data.roll)
        json.put(HEAD_YAW, data.yaw)

        return json.toString()
    }

    fun sensBasePose2JSONstring(data : SensBasePose) : String {
        val json = JSONObject()
        json.put(DATA_LBL, BASE_POSE)
        json.put(BASE_POSE_PITCH, data.pitch)
        json.put(BASE_POSE_ROLL, data.roll)
        json.put(BASE_POSE_YAW, data.yaw)

        return json.toString()
    }

    fun sensBaseTick2JSONstring(data : SensBaseTick) : String {
        val json = JSONObject()
        json.put(DATA_LBL, BASE_TICK)
        json.put(BASE_TICK_L, data.left)
        json.put(BASE_TICK_R, data.right)

        return json.toString()
    }

    fun sensPose2D2JSONstring(data : SensPose2D) : String {
        val json = JSONObject()
        json.put(DATA_LBL, POSE2D)
        json.put(POSE2D_X, data.x)
        json.put(POSE2D_Y, data.y)
        json.put(POSE2D_TH, data.theta)
        json.put(POSE2D_VL, data.linearVelocity)
        json.put(POSE2D_VA, data.angularVelocity)

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

data class SensBasePose(
    val pitch : Float,
    val roll : Float,
    val yaw : Float,
    val label : String = DataResponce.BASE_POSE
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