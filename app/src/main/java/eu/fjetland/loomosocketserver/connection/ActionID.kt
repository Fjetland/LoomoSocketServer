package eu.fjetland.loomosocketserver.connection

class ActionID {
    companion object {
        val ACTION = "ack"

        val SPEAK = "spk"
        val SPEAK_LENGTH = "l"
        val SPEAK_QUE = "q"
        val SPEAK_PITCH = "p"

        val VOLUME = "vol"
        val VOLUME_VALUE = "v"
    }
}