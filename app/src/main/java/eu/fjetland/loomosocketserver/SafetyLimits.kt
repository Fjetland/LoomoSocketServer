package eu.fjetland.loomosocketserver

class SafetyLimits {
    companion object{
        const val LINEAR_VELOCITY_LIMIT = 4.0F      // [Float] recommend 0.8
        const val ANGULAR_VELOCITY_LIMIT = 4.0F     // [Float]  recommend 1.0

        const val USE_OBSTACLE_AVOIDANCE = true    // Can lead to laggy responce time
                                                    // due to unknown holdup in Base SDK
                                                    // Recommend: OFF
    }
}