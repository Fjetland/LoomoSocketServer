package eu.fjetland.loomosocketserver.connection

class ResponsID {
    companion object {

        // Genneral messages
        val YES = 1
        val NO = 2
        val READY4DATA = 3
        val DISCONNECT = 10
        val STRING_NEXT = 7

        val STOP = 17


        val RETURNTEST = 31

        // Data returns
        val DATA_LBL = "dat"
        val DATA_TIME_LBL = "t"
        val DATA_SURROUNDINGS_ID = 113
        val DATA_SURROUNDINGS_IRLEFT = "irl"
        val DATA_SURROUNDINGS_IRRIGHT = "irr"
        val DATA_SURROUNDINGS_ULTRASONIC = "uss"

    }
}