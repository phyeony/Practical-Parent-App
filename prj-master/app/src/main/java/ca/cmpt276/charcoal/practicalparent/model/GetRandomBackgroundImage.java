package ca.cmpt276.charcoal.practicalparent.model;

import java.util.Random;

import ca.cmpt276.charcoal.practicalparent.R;

/**
 *  Gets a random background image for timer activity
 */
public class GetRandomBackgroundImage {
    private final static int[] idArr = new int[] {
            R.drawable.timer_wallpaper1,
            R.drawable.timer_wallpaper2,
            R.drawable.timer_wallpaper3,
            R.drawable.timer_wallpaper4,
            R.drawable.timer_wallpaper5,
    };

    private GetRandomBackgroundImage() {
    }

    public static int getId() {
        final int random = new Random().nextInt(idArr.length);
        return idArr[random];
    }
}
