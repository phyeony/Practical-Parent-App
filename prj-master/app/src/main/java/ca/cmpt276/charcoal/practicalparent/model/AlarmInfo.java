package ca.cmpt276.charcoal.practicalparent.model;

import android.media.Ringtone;
import android.os.Vibrator;

import java.io.Serializable;

/**
 *  Alarm info allows to create and set ringtone for alarm
 */
public class AlarmInfo implements Serializable {
    private Ringtone ringtone;
    private Vibrator vibrator;
    private static AlarmInfo instance = null;

    public static AlarmInfo getInstance() {
        if (instance == null) {
            instance = new AlarmInfo();
        }
        return instance;
    }

    public Ringtone getRingtone() {
        return ringtone;
    }

    public void setRingtone(Ringtone ringtone) {
        this.ringtone = ringtone;
    }

    public Vibrator getVibrator() {
        return vibrator;
    }

    public void setVibrator(Vibrator vibrator) {
        this.vibrator = vibrator;
    }
}
