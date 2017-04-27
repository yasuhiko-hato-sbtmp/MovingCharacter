package yasuhiko.hato.movingcharacter;

/**
 * Created by hatoy37 on 4/12/17.
 */

public class Constants {
    public static int imageL;
    public static int imageR;
    public static int imageU;

    public Constants(){
        changeImageToBlue();
    }

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
}
