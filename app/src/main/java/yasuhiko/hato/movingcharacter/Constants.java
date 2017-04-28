package yasuhiko.hato.movingcharacter;

/**
 * Created by hatoy37 on 4/12/17.
 */

public class Constants {
    public static int imageL = R.drawable.robot_b_l;
    public static int imageR = R.drawable.robot_b_r;
    public static int imageU = R.drawable.robot_b_u;

    public static boolean move = true;

    private static long MOVING_TIME_INTERVAL_SOMETIMES = 60 * 1000;
    private static long MOVING_TIME_INTERVAL_STANDARD = 30 * 1000;
    private static long MOVING_TIME_INTERVAL_FREQUENTLY = 10 * 1000;
    public static long movingTimeIntervalMilliSec = MOVING_TIME_INTERVAL_STANDARD;

    public static void changeImageToBlue(){
        imageL = R.drawable.robot_b_l;
        imageR = R.drawable.robot_b_r;
        imageU = R.drawable.robot_b_u;
    }

    public static void changeImageToRed(){
        imageL = R.drawable.robot_r_l;
        imageR = R.drawable.robot_r_r;
        imageU = R.drawable.robot_r_u;
    }

    public static void changeMovingTimeIntervalToSometimes(){
        movingTimeIntervalMilliSec = MOVING_TIME_INTERVAL_SOMETIMES;
    }
    public static void changeMovingTimeIntervalToStandard(){
        movingTimeIntervalMilliSec = MOVING_TIME_INTERVAL_STANDARD;
    }
    public static void changeMovingTimeIntervalToFrequently(){
        movingTimeIntervalMilliSec = MOVING_TIME_INTERVAL_FREQUENTLY;
    }
}
