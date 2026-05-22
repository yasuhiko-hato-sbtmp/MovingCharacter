package yasuhiko.hato.movingcharacter

object Constants {
    @JvmField
    var imageL: Int = R.drawable.robot_b_l
    @JvmField
    var imageR: Int = R.drawable.robot_b_r
    @JvmField
    var imageU: Int = R.drawable.robot_b_u

    @JvmField
    var move: Boolean = true

    private const val MOVING_TIME_INTERVAL_SOMETIMES = 60 * 1000L
    private const val MOVING_TIME_INTERVAL_STANDARD = 30 * 1000L
    private const val MOVING_TIME_INTERVAL_FREQUENTLY = 10 * 1000L
    
    @JvmField
    var movingTimeIntervalMilliSec: Long = MOVING_TIME_INTERVAL_STANDARD

    @JvmStatic
    fun changeImageToBlue() {
        imageL = R.drawable.robot_b_l
        imageR = R.drawable.robot_b_r
        imageU = R.drawable.robot_b_u
    }

    @JvmStatic
    fun changeImageToRed() {
        imageL = R.drawable.robot_r_l
        imageR = R.drawable.robot_r_r
        imageU = R.drawable.robot_r_u
    }

    @JvmStatic
    fun changeMovingTimeIntervalToSometimes() {
        movingTimeIntervalMilliSec = MOVING_TIME_INTERVAL_SOMETIMES
    }

    @JvmStatic
    fun changeMovingTimeIntervalToStandard() {
        movingTimeIntervalMilliSec = MOVING_TIME_INTERVAL_STANDARD
    }

    @JvmStatic
    fun changeMovingTimeIntervalToFrequently() {
        movingTimeIntervalMilliSec = MOVING_TIME_INTERVAL_FREQUENTLY
    }
}
