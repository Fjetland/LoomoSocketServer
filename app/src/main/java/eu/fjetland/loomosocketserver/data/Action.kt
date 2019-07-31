package eu.fjetland.loomosocketserver.data

import eu.fjetland.loomosocketserver.ENCODING
import org.json.JSONObject

class Action(string: String){
    companion object {
        val ACTION = "ack"

        val ENABLE_DRIVE = "enableDrive"
        val ENABLE_DRIVE_VALUE = "value"

        val VELOCITY = "vel"
        val VELOCITY_ANGULAR = "av"
        val VELOCITY_LINEAR = "v"

        val POSITION = "pos"
        val POSITION_X = "x"
        val POSITION_Y = "y"
        val POSITION_TH = "th"
        val POSITION_ADD = "add"

        val SPEAK = "spk"
        val SPEAK_LENGTH = "l"
        val SPEAK_QUE = "q"
        val SPEAK_PITCH = "p"

        val VOLUME = "vol"
        val VOLUME_VALUE = "v"

        val HEAD = "hed"
        val HEAD_PITCH = "p"
        val HEAD_YAW = "y"
        val HEAD_MODE = "m"
        val HEAD_LIGHT = "li"

        val HEAD_SET_SMOOTH = 0
        val HEAD_SET_LOCK = 1

        val ACTIONLIST = listOf(ENABLE_DRIVE, VELOCITY, POSITION, SPEAK, VOLUME, HEAD)
    }

    constructor(byteArray: ByteArray) : this(byteArray.toString(charset(ENCODING)))

    private val jsonObject = JSONObject(string)
    var actionType : String

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
        return obj
    }

    fun json2speak() : Speak {
        val obj = Speak(jsonObject.getInt(SPEAK_LENGTH))
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
}

data class EnableDrive(
    val drive : Boolean,
    val act: String = Action.ENABLE_DRIVE
)

data class Head(
    var pitch :Float, // Head pitch
    var yaw : Float, // Head Yaw
    var li : Int = 10, // Head light mode 0-13
    var mode : Int = Action.HEAD_SET_SMOOTH,
    val act : String = Action.HEAD)

data class Velocity(
    val v : Float, // Linear Velocity
    val av : Float, // Angular velocity
    val act: String = Action.VELOCITY
)

data class Position(
    val x : Float, // X direction absolute movement
    val y : Float, // Y direction absolute movement
    var th: Float? = null,
    var add: Boolean = false,
    val act: String = Action.POSITION
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
