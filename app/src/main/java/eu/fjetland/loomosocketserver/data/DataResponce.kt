package eu.fjetland.loomosocketserver.data

class DataResponce {
    companion object {
        // Data returns
        val DATA_LBL = "dat"
        val TIME_LBL = "time"
        val SURROUNDINGS = 113
        val SURROUNDINGS_IRLEFT = "irl"
        val SURROUNDINGS_IRRIGHT = "irr"
        val SURROUNDINGS_ULTRASONIC = "uss"

    }
}

data class ReadingSurroundings(
    val IR_Left : Int,
    val IR_Right : Int,
    val UltraSonic : Int,
    val timeStamp : Long,
    val label : Int = DataResponce.SURROUNDINGS)

data class ReadingVelocity(
    val velocity : Float,
    val velocityLeft : Float,
    val velocityRight : Float,
    val angularVelocity : Float
)