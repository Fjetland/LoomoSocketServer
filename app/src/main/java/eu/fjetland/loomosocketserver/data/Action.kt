package eu.fjetland.loomosocketserver.data

import eu.fjetland.loomosocketserver.ENCODING
import org.json.JSONObject

class Action(json: JSONObject){
    constructor(byteArray: ByteArray) : this(byteArray.toString(charset(ENCODING)))
    constructor(string: String) : this(JSONObject(string))

    val jsonObject = json
    var actionType : String

    companion object {
        /**
         * Must correspond with MATLAB and doccumentation
         *
         * All new actions must be registerd in the list at the end
         * of the companion object in order to be actionable trugh TCP
         */
        const val ACTION = "ack"

        const val SEQUENCE = "seq"

        const val ENABLE_DRIVE = "enableDrive"
        const val ENABLE_DRIVE_VALUE = "value"

        const val ENABLE_VISION = "enableVision" // See Dataresponce for camera labels

        const val VELOCITY = "vel"
        const val VELOCITY_ANGULAR = "av"
        const val VELOCITY_LINEAR = "v"

        const val POSITION = "pos"
        const val POSITION_ARRAY = "posar"

        const val POSITION_X = "x"
        const val POSITION_Y = "y"
        const val POSITION_TH = "th"
        const val POSITION_ADD = "add"
        const val POSITION_VLS = "vls"

        const val SPEAK = "spk"
        const val SPEAK_STRING = "s"
        const val SPEAK_LENGTH = "l"
        const val SPEAK_QUE = "q"
        const val SPEAK_PITCH = "p"

        const val VOLUME = "vol"
        const val VOLUME_VALUE = "v"

        const val HEAD = "hed"
        const val HEAD_PITCH = "p"
        const val HEAD_YAW = "y"
        const val HEAD_MODE = "m"
        const val HEAD_LIGHT = "li"

        const val HEAD_SET_SMOOTH = 0
        const val HEAD_SET_LOCK = 1

        val ACTIONLIST = listOf(SEQUENCE,ENABLE_DRIVE, VELOCITY, POSITION, POSITION_ARRAY, SPEAK, VOLUME, HEAD, ENABLE_VISION)
    }

    init {
        if (jsonObject.has(ACTION)){
            actionType = jsonObject.getString(ACTION)
        } else {
            actionType = "None"
        }
    }

    fun json2head() : Head {
       val obj = Head(jsonObject.getDouble(HEAD_PITCH).toFloat(),
           jsonObject.getDouble(HEAD_YAW).toFloat())
        if (jsonObject.has(HEAD_LIGHT)){
            obj.li = jsonObject.getInt(HEAD_LIGHT)
        }
        if (jsonObject.has(HEAD_MODE)) {
            obj.mode = jsonObject.getInt(HEAD_MODE)
        }
        return  obj
    }

    fun json2velocity() : Velocity{
        val obj = Velocity(
            jsonObject.getDouble(VELOCITY_LINEAR).toFloat(),
            jsonObject.getDouble(VELOCITY_ANGULAR).toFloat()
        )
        return obj
    }

    fun json2position() : Position {
        val obj = Position(
            jsonObject.getDouble(POSITION_X).toFloat(),
            jsonObject.getDouble(POSITION_Y).toFloat()
        )
        if (jsonObject.has(POSITION_TH)){
            obj.th = jsonObject.getDouble(POSITION_TH).toFloat()
        }
        if (jsonObject.has(POSITION_ADD)){
            obj.add = jsonObject.getBoolean(POSITION_ADD)
        }

        if (jsonObject.has(POSITION_VLS)){
            obj.vls = jsonObject.getBoolean(POSITION_VLS)
        }
        return obj
    }

    fun json2positionArray()  : PositionArray{
        val xJson = jsonObject.getJSONArray(POSITION_X)
        val yJson = jsonObject.getJSONArray(POSITION_Y)
        val numPoints = xJson.length()

        val x = FloatArray(numPoints)
        val y = FloatArray(numPoints)

        val obj: PositionArray?

        if (jsonObject.has(POSITION_TH)) {
            val thJson = jsonObject.getJSONArray(POSITION_TH)
            val th = FloatArray(numPoints)
            for (element in 0 until numPoints) {
                x[element] = xJson.getDouble(element).toFloat()
                y[element] = yJson.getDouble(element).toFloat()
                th[element] = thJson.getDouble(element).toFloat()
            }
            obj = PositionArray(
                x = x,
                y = y,
                th = th
            )
        } else {
            for (element in 0 until numPoints) {
                x[element] = xJson.getDouble(element).toFloat()
                y[element] = yJson.getDouble(element).toFloat()
            }
            obj = PositionArray(
                x = x,
                y = y
            )
        }
        if (jsonObject.has(POSITION_VLS)){
            obj.vls = jsonObject.getBoolean(POSITION_VLS)
        }
        return obj
    }

    fun json2speak() : Speak {
        val obj = Speak(jsonObject.getInt(SPEAK_LENGTH))
        obj.string = jsonObject.getString(SPEAK_STRING)
        if (jsonObject.has(SPEAK_PITCH)) obj.pitch = jsonObject.getDouble(SPEAK_PITCH).toFloat()
        if (jsonObject.has(SPEAK_QUE)) obj.que = jsonObject.getInt(SPEAK_QUE)
        return obj
    }

    fun json2volume() : Volume {
        return Volume(jsonObject.getDouble(VOLUME_VALUE))
    }

    fun json2enableDrive() : EnableDrive {
        return EnableDrive(jsonObject.getBoolean(ENABLE_DRIVE_VALUE))
    }

    fun json2imageResponce() : ImageResponse {
        val obj =  if (jsonObject.has(DataResponce.IMAGE_TYPE)) {
                        ImageResponse(jsonObject.getString(DataResponce.IMAGE_TYPE))
        } else ImageResponse()
        return obj
    }

    fun json2EenableVision() : EnableVision {
        val obj = EnableVision(
            depth = jsonObject.getBoolean(DataResponce.IMAGE_TYPE_DEPTH),
            color = jsonObject.getBoolean(DataResponce.IMAGE_TYPE_COLOR),
            colorSmall = jsonObject.getBoolean(DataResponce.IMAGE_TYPE_COLOR_SMALL)
        )
        return obj
    }
}

data class EnableDrive( //
    val drive : Boolean,
    val act: String = Action.ENABLE_DRIVE
)

data class EnableVision( //
    val depth : Boolean,
    val color : Boolean,
    val colorSmall : Boolean,
    val act: String = Action.ENABLE_VISION
)

data class Head( //
    var pitch :Float, // Head pitch
    var yaw : Float, // Head Yaw
    var li : Int? = null, // Head light mode 0-13
    var mode : Int = Action.HEAD_SET_SMOOTH,
    val act : String = Action.HEAD)

data class Velocity( //
    val v : Float, // Linear Velocity
    val av : Float, // Angular velocity
    val act: String = Action.VELOCITY
)

data class Position(
    val x : Float, // X direction absolute movement
    val y : Float, // Y direction absolute movement
    var th: Float? = null,
    var add: Boolean = false,
    var vls: Boolean = false,
    val act: String = Action.POSITION
)

data class PositionArray(
    val x: FloatArray,
    val y: FloatArray,
    var th: FloatArray? = null,
    var add: Boolean = false,
    var vls: Boolean = false,
    val act: String = Action.POSITION_ARRAY
)

data class Speak(
    val length : Int, // Length Of string to come
    var pitch : Float = 1.0F, // Pitch of the voice
    var que : Int = 0, // Should the speaker be qued
    var string: String = "",
    val act : String = Action.SPEAK
    )

data class Volume(
    val v : Double,
    val act: String = Action.VOLUME
)
