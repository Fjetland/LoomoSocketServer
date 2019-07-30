package eu.fjetland.loomosocketserver.connection

class ActionID {
    companion object {
        val ACTION = "ack"

        val VELOCITY = "vel"
        val VELOCITY_ANGULAR = "av"
        val VELOCITY_LINEAR = "v"

        val SPEAK = "spk"
        val SPEAK_LENGTH = "l"
        val SPEAK_QUE = "que"
        val SPEAK_PITCH = "pitch"

        val VOLUME = "vol"
        val VOLUME_VALUE = "v"

        val HEAD = "hed"
        val HEAD_PITCH = "pitch"
        val HEAD_YAW = "y"
        val HEAD_LIGHT = "li"
    }
}