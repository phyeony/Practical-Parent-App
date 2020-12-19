package ca.cmpt276.charcoal.practicalparent.model;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import ca.cmpt276.charcoal.practicalparent.R;
import ca.cmpt276.charcoal.practicalparent.TimeOutActivity;

/**
 *  Background service for timer activity allows timer to continue in background
 */
public class BackgroundService extends Service {
    // Reference:
    //   https://www.youtube.com/watch?v=BbXuumYactY

    private final static String TAG = "BroadcastService";
    private static final String EXTRA_TIME = "ca.cmpt276.charcoal.practicalparent.model - timeLeftinMillis";
    private static final String TIME_SCALE_FACTOR = "ca.cmpt276.charcoal.practicalparent.model - timeScaleFactor";
    private long timeLeftInMillis;
    public static final String COUNTDOWN_BR = "ca.cmpt276.charcoal.practicalparent.model";
    Intent intent = new Intent(COUNTDOWN_BR);
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning;
    private double timeScaleFactor;

    private final long[] pattern = {0, 400, 300};
    private final int NOTIFICATION_ID = 0;

    public static Intent makeLaunchIntent(Context context, long timeLeftInMillis, double timeScaleFactor) {
        Intent intent = new Intent(context, BackgroundService.class);
        intent.putExtra(EXTRA_TIME, timeLeftInMillis);
        intent.putExtra(TIME_SCALE_FACTOR, timeScaleFactor);
        return intent;
    }

    private void startTimer() {
        Log.i(TAG, "timeLeftInMillis" + timeLeftInMillis);

        int broadcastFrequency = (int) (1000/timeScaleFactor);
        countDownTimer = new CountDownTimer(timeLeftInMillis, broadcastFrequency) {
            @Override
            public void onTick(long millisUntilFinished) {
                isTimerRunning = true;
                timeLeftInMillis = millisUntilFinished;

                Log.i(TAG, "Countdown seconds remaining: " + millisUntilFinished / 1000);
                intent.putExtra("countDown", timeLeftInMillis);
                intent.putExtra("isTimerRunning", isTimerRunning);

                sendBroadcast(intent);
            }

            @Override
            public void onFinish() {
                notifyTimerDone();
                stopSelf();
                Log.i(TAG, "Timer finished");
            }
        }.start();
    }

    private void notifyTimerDone() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
        ringtone.play();

        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            // Reference:
            //   https://stackoverflow.com/questions/60466695/android-vibration-app-doesnt-work-anymore-after-android-10-api-29-update
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0)
                    , new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()
            );
        } else {
            vibrator.vibrate(pattern, 0);
        }

        createNotification(ringtone, vibrator);
    }

    private void createNotification(Ringtone ringtone, Vibrator vibrator) {
        Intent intent = TimeOutActivity.makeLaunchIntent(this);
        PendingIntent pendingLaunchIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Intent stopTimerIntent = new Intent(this, NotificationStopBroadcastReceiver.class);
        stopTimerIntent.putExtra(getString(R.string.notificationID_intent_name_tag), NOTIFICATION_ID);
        PendingIntent pendingStopTimerIntent = PendingIntent.getBroadcast(this, 0, stopTimerIntent, 0);

        AlarmInfo alarmInfo = AlarmInfo.getInstance();
        alarmInfo.setRingtone(ringtone);
        alarmInfo.setVibrator(vibrator);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                getString(R.string.timeout_alarm_notification_ID))
                .setSmallIcon(R.drawable.ic_baseline_alarm_24)
                .setContentTitle(getString(R.string.timeout_notification_title))
                .setContentText(getString(R.string.timeout_notification_body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setFullScreenIntent(pendingLaunchIntent, true)
                .setAutoCancel(false)
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_CALL)
                .setAutoCancel(false)
                .addAction(R.drawable.ic_baseline_alarm_24, "Stop", pendingStopTimerIntent);

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build());
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        timeLeftInMillis = intent.getLongExtra(EXTRA_TIME , 1000);
        timeScaleFactor = intent.getDoubleExtra(TIME_SCALE_FACTOR, 1.0);
        Log.i(TAG, "timeLeftInMillis in onStart method" + timeLeftInMillis);
        startTimer();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        countDownTimer.cancel();
        isTimerRunning = false;
        intent.putExtra("isTimerRunning", isTimerRunning);
        sendBroadcast(intent);
    }
}
