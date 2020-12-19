package ca.cmpt276.charcoal.practicalparent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import ca.cmpt276.charcoal.practicalparent.model.BackgroundService;
import ca.cmpt276.charcoal.practicalparent.model.GetRandomBackgroundImage;

/**
 *  Sets up TimeOut activity and timer
 */
public class TimeOutActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, TimeOutBottomSheetFragment.BottomSheetListener {
    public static final String TAG = "TimeOut";
    public static final String TIME_SCALE_INDEX_TAG = "time scale index";
    public static final String TIME_SCALE_OPTIONS_TAG = "time scale options";
    private static final String PREFS_NAME = "Saved data";
    private static final String START_TIME_IN_MILLIS = "startTimeInMillis";
    private static final String STANDARD_START_TIME_IN_MILLIS_TAG = "standardStartTimeInMillis";
    private static final String TIME_LEFT_IN_MILLIS = "timeLeftInMillis";
    private static final String TIMER_IS_PAUSED = "timerIsPaused";

    private long startTimeInMillis;
    private long standardStartTimeInMillis;
    private long timeLeftInMillis;

    private View loadingView;
    private TextView countDownText, timeScaleText;
    private Button startButton, pauseButton, resetButton, setButton;
    private EditText setTimeText;
    private ProgressBar pieTimer;

    private boolean timerIsRunning;
    private boolean timerIsReset;
    private final double[] timeScaleOptions = {0.25, 0.5, 0.75, 1.0, 2.0, 3.0, 4.0};
    private final int defaultTimeScaleIndex = 3;
    private int timeScaleIndex = defaultTimeScaleIndex;
    private boolean needToFetch = true;
    private boolean timerIsPaused;

    private CustomSpinner preSetTimeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_out);

        // Reference:
        //   https://codinginflow.com/tutorials/android/countdowntimer/part-1-countdown-timer
        setupSetButton();
        setupStartButton();
        setupResetButton();
        setupPauseButton();
        setupSpinner();
        setupTimeScaleTextView();
        setLoadingScreen();
        setRandomBackgroundImage();

        getTimerIsPausedFromSharedPrefs();

        // Enable "up" on toolbar
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void getTimerIsPausedFromSharedPrefs() {
        Bundle bundle = getStateFromSharedPrefs();
        timerIsPaused = bundle.getBoolean(TIMER_IS_PAUSED);
    }

    private void setupUIWhenTimerWasPausedBefore() {
        Bundle bundle = getStateFromSharedPrefs();
        Log.i(TAG, "recoveredTImeLeftINMillis in setup function "  + timeLeftInMillis);
        Log.i(TAG, "timer was paused");
        timeLeftInMillis = bundle.getLong(TIME_LEFT_IN_MILLIS);
        startTimeInMillis = bundle.getLong(START_TIME_IN_MILLIS);
        int recoveredTimeScaleIndex = bundle.getInt(TIME_SCALE_INDEX_TAG);
        standardStartTimeInMillis = bundle.getLong(STANDARD_START_TIME_IN_MILLIS_TAG);
        prepareAlteredTimer(recoveredTimeScaleIndex);

        // Set up UI
        resetButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.VISIBLE);
        pauseButton.setText(R.string.action_resume);
        startButton.setVisibility(View.INVISIBLE);
        preSetTimeSpinner.setVisibility(View.INVISIBLE);
        setButton.setVisibility(View.INVISIBLE);
        setTimeText.setVisibility(View.INVISIBLE);
        updateCountDownText();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_time_out, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_alter_time_speed) {
            TimeOutBottomSheetFragment sheetFragment = new TimeOutBottomSheetFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(TIME_SCALE_INDEX_TAG, timeScaleIndex);
            bundle.putDoubleArray(TIME_SCALE_OPTIONS_TAG, timeScaleOptions);
            sheetFragment.setArguments(bundle);
            sheetFragment.show(getSupportFragmentManager(), "Fragment");
        }
       return super.onOptionsItemSelected(item);
    }

    private void setLoadingScreen() {
        pieTimer = findViewById(R.id.timer_progress);
        countDownText = findViewById(R.id.text_count_down);
        loadingView = findViewById(R.id.spinner_loading);
        loadingView.animate()
                .alpha(0f)
                .setDuration(1000)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        loadingView.setVisibility(View.GONE);
                    }
                });
        setupFirstTimeOutUI();
    }

    private void setupFirstTimeOutUI() {
        fadeInView(startButton);
        fadeInView(setButton);
        fadeInView(setTimeText);
        fadeInView(countDownText);
        fadeInView(preSetTimeSpinner);
        fadeInView(pieTimer);
    }

    private void fadeInView(View view) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);

        // Animate the content view to 100% opacity
        view.animate()
                .alpha(1f)
                .setDuration(6000)
                .setListener(null);
    }

    private void setRandomBackgroundImage() {
        ConstraintLayout layout = findViewById(R.id.layout_timeout);
        layout.setBackground(ContextCompat.getDrawable(this, GetRandomBackgroundImage.getId()));
    }

    public static Intent makeLaunchIntent(Context context) {
        return new Intent(context, TimeOutActivity.class);
    }

    private void setupSpinner() {
        preSetTimeSpinner = findViewById(R.id.spinner_preset_time);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.msg_preset_times, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        preSetTimeSpinner.setAdapter(adapter);
        preSetTimeSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String preSetTime = parent.getItemAtPosition(position).toString();
        long millisInput = Long.parseLong(preSetTime) * 60000;
        Log.i(TAG,"Selected drop down time: " + millisInput/1000);
        timeScaleIndex = defaultTimeScaleIndex;
        updateTimeScaleText();
        setTime(millisInput);
        startButton.setVisibility(View.VISIBLE);
        resetButton.setVisibility(View.INVISIBLE);

        if (timerIsPaused) {
            setupUIWhenTimerWasPausedBefore();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private void updateUI() {
        if (timerIsRunning) {
            resetButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.VISIBLE);
            pauseButton.setText(R.string.action_pause);
            startButton.setVisibility(View.INVISIBLE);
            preSetTimeSpinner.setVisibility(View.INVISIBLE);
            setButton.setVisibility(View.INVISIBLE);
            setTimeText.setVisibility(View.INVISIBLE);
        } else {
            if (timeLeftInMillis < 1000) {
                // If the timer is done
                resetButton.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.INVISIBLE);
                pauseButton.setText(R.string.action_pause);
                startButton.setVisibility(View.INVISIBLE);
                preSetTimeSpinner.setVisibility(View.VISIBLE);
                setButton.setVisibility(View.VISIBLE);
                setTimeText.setVisibility(View.VISIBLE);
            } else if (timerIsReset) {
                // If user press cancel when the timer is running
                resetButton.setVisibility(View.INVISIBLE);
                startButton.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.INVISIBLE);
                setButton.setVisibility(View.VISIBLE);
                setTimeText.setVisibility(View.VISIBLE);
                preSetTimeSpinner.setVisibility(View.VISIBLE);
            } else if (timeLeftInMillis < startTimeInMillis) {
                // If the timer is paused
                resetButton.setVisibility(View.VISIBLE);
                pauseButton.setVisibility(View.VISIBLE);
                pauseButton.setText(R.string.action_resume);
                startButton.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void setupTimeScaleTextView() {
        timeScaleText = findViewById(R.id.text_time_scale);
        updateTimeScaleText();
    }

    private void setupPauseButton() {
        pauseButton = findViewById(R.id.button_pause);
        pauseButton.setOnClickListener(v -> {
            if (timerIsRunning) {
                pauseTimer();
            } else {
                startTimer();
            }
        });
    }

    private void setupResetButton() {
        resetButton = findViewById(R.id.button_reset);
        resetButton.setOnClickListener(v -> resetTimer());
    }

    private void setupStartButton() {
        startButton = findViewById(R.id.button_start);
        startButton.setOnClickListener(v -> {
            if (timeLeftInMillis < 1000) {
                Toast.makeText(TimeOutActivity.this, "No Timer Made", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(TimeOutActivity.this, "Started", Toast.LENGTH_SHORT).show();
            startTimer();
        });
    }

    private void setupSetButton() {
        setButton = findViewById(R.id.button_set);
        setTimeText = findViewById(R.id.text_set_time);
        setButton.setOnClickListener(v -> {
            String input = setTimeText.getText().toString();

            // If input is empty
            if (input.length() == 0) {
                Toast.makeText(TimeOutActivity.this, "Field can't be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            long millisInput = Long.parseLong(input) * 60000;
            // If entered 0
            if (millisInput == 0) {
                Toast.makeText(TimeOutActivity.this, "Please enter a positive number", Toast.LENGTH_SHORT).show();
                return;
            }

            timeScaleIndex = defaultTimeScaleIndex;
            updateTimeScaleText();
            setTime(millisInput);
            setTimeText.setText("");
            startButton.setVisibility(View.VISIBLE);
            resetButton.setVisibility(View.INVISIBLE);
        });
    }

    private void setTime(long milliseconds) {
        standardStartTimeInMillis = milliseconds;
        Log.i(TAG, "standardTimeUpdated: " + standardStartTimeInMillis);
        startTimeInMillis = (long)(milliseconds/timeScaleOptions[timeScaleIndex]);
        timeLeftInMillis = startTimeInMillis;
        updateCountDownText();
        updateUI();
        closeKeyboard();
    }

    private void saveStateInSharedPrefs() {
        Log.i(TAG, "Save startTimeInMillis: " + startTimeInMillis);
        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean(TIMER_IS_PAUSED, timerIsPaused);
        editor.putLong(TIME_LEFT_IN_MILLIS, timeLeftInMillis);
        editor.putLong(START_TIME_IN_MILLIS, startTimeInMillis);
        editor.putInt(TIME_SCALE_INDEX_TAG, timeScaleIndex);
        editor.putLong(STANDARD_START_TIME_IN_MILLIS_TAG, standardStartTimeInMillis);
        editor.apply();
    }

    private Bundle getStateFromSharedPrefs() {
        SharedPreferences prefs = this.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        long recoveredStartTimeInMillis = prefs.getLong(START_TIME_IN_MILLIS, 0);
        int recoveredTimeScaleIndex = prefs.getInt(TIME_SCALE_INDEX_TAG, defaultTimeScaleIndex);
        long recoveredStandardStartTimeInMillis = prefs.getLong(STANDARD_START_TIME_IN_MILLIS_TAG, 0);
        long recoveredTimeLeftInMillis = prefs.getLong(TIME_LEFT_IN_MILLIS, -1);
        boolean recoveredTimerIsPaused = prefs.getBoolean(TIMER_IS_PAUSED,false);
        Log.i(TAG, "recovered StartTimeInMillis: " + recoveredStartTimeInMillis);
        Log.i(TAG, "recovered timeLeftInMillis: " + recoveredTimeLeftInMillis);
        Log.i(TAG, "recovered timeScaleIndex: " + recoveredTimeScaleIndex);
        Log.i(TAG, "recovered timerIsPaused: " + recoveredTimerIsPaused);

        Bundle bundle = new Bundle();
        bundle.putBoolean(TIMER_IS_PAUSED, recoveredTimerIsPaused);
        bundle.putLong(TIME_LEFT_IN_MILLIS, recoveredTimeLeftInMillis);
        bundle.putLong(START_TIME_IN_MILLIS, recoveredStartTimeInMillis);
        bundle.putInt(TIME_SCALE_INDEX_TAG, recoveredTimeScaleIndex);
        bundle.putLong(STANDARD_START_TIME_IN_MILLIS_TAG, recoveredStandardStartTimeInMillis);
        return bundle;
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void resetTimer() {
        timerIsReset = true;
        timerIsPaused = false;
        stopService(new Intent(this, BackgroundService.class));
        timerIsRunning = false;
        Log.i(TAG,"Reset service");
        timeScaleIndex = defaultTimeScaleIndex;
        startTimeInMillis = standardStartTimeInMillis;
        timeLeftInMillis = startTimeInMillis;
        updateTimeScaleText();
        updateCountDownText();
        updateUI();
        updatePieTimer(timeLeftInMillis, true);
    }

    private void updateTimeScaleText() {
        int timeScalePercentage = (int) (timeScaleOptions[timeScaleIndex] * 100);
        timeScaleText.setText(getString(R.string.msg_time_percent, timeScalePercentage));
    }

    private void updateCountDownText() {
        int hours = (int) ((timeLeftInMillis * timeScaleOptions[timeScaleIndex]) / 1000) / 3600;
        int minutes = (int) (((timeLeftInMillis * timeScaleOptions[timeScaleIndex]) / 1000) % 3600) / 60;
        int seconds = (int) ((timeLeftInMillis * timeScaleOptions[timeScaleIndex]) / 1000) % 60;
        Log.i(TAG, "update countdown: hours --> " + hours);
        Log.i(TAG, "update countdown: minutes --> " + minutes);
        Log.i(TAG, "update countdown: seconds --> " + seconds);

        long formattedMillis = (long)(
                (hours * 1000 * 3600 / timeScaleOptions[timeScaleIndex])
                + (minutes * 60 * 1000 / timeScaleOptions[timeScaleIndex])
                + (seconds * 1000 / timeScaleOptions[timeScaleIndex])
        );
        updatePieTimer(formattedMillis, false);

        Log.i(TAG, "Countdown seconds remaining: " + timeLeftInMillis/1000);
        String timeLeftFormatted;
        if (hours > 0) {
            timeLeftFormatted = String.format(Locale.getDefault(),
                    "%d:%02d:%02d", hours, minutes, seconds);
        } else {
         timeLeftFormatted = String.format(Locale.getDefault(),
                 "%02d:%02d", minutes, seconds);
        }
        countDownText.setText(timeLeftFormatted);
    }

    private void startTimer() {
        timerIsReset = false;
        needToFetch = false;
        timerIsPaused = false;
        Intent intent = BackgroundService.makeLaunchIntent(this, timeLeftInMillis, timeScaleOptions[timeScaleIndex]);
        startService(intent);
    }

    private void pauseTimer() {
        timerIsReset = false;
        timerIsPaused = true;
        stopService(new Intent(this, BackgroundService.class));
        Log.i(TAG,"Paused service");
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Update timeLeftInMillis
            if (intent.getExtras() != null && !timerIsReset) {
                timeLeftInMillis = intent.getLongExtra("countDown", 1000);
                timerIsRunning = intent.getBooleanExtra("isTimerRunning", false);
                Log.i(TAG, "timeLeftInMillis passed from service: " + timeLeftInMillis / 1000);
                Log.i(TAG, "isTimerRunning passed from service: " + timerIsRunning);
            }

            if (needToFetch) {
                Bundle bundle = getStateFromSharedPrefs();
                long recoveredStartTimeInMillis = bundle.getLong(START_TIME_IN_MILLIS);
                int recoveredTimeScaleIndex = bundle.getInt(TIME_SCALE_INDEX_TAG);
                long recoveredStandardStartTimeInMillis = bundle.getLong(STANDARD_START_TIME_IN_MILLIS_TAG);
                if (recoveredStartTimeInMillis != 0) {
                    standardStartTimeInMillis = recoveredStandardStartTimeInMillis;
                    startTimeInMillis = recoveredStartTimeInMillis;
                    prepareAlteredTimer(recoveredTimeScaleIndex);
                }
                needToFetch = false;
            }

            updateCountDownText();
            updateUI();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(BackgroundService.COUNTDOWN_BR));
        Log.i(TAG, "Registered broadcast receiver");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "on Destroy" + startTimeInMillis);
        saveStateInSharedPrefs();
        unregisterReceiver(broadcastReceiver);

        Log.i(TAG, "onDestroy, pieTimer" + pieTimer.getProgress());
        Log.i(TAG, "onDestroy, time left in millis" + timeLeftInMillis);
    }

    // Reference:
    //   https://www.youtube.com/watch?v=YsHHXg1vbcc&ab_channel=CodinginFlow
    private void updatePieTimer(long timeLeftInMillis, boolean reset) {
        float pieProgressFloat;
        int pieProgressInt;

        if (reset) {
            pieTimer.setProgress(0);
            return;
        }
        Log.i(TAG, "TimeLeftInMillis in UPDATE PIE TIMER " + timeLeftInMillis);
        Log.i(TAG, "StartTimeInMillis " + startTimeInMillis);

        if (startTimeInMillis > startTimeInMillis - timeLeftInMillis) {
            pieProgressFloat = startTimeInMillis - timeLeftInMillis;
            pieProgressFloat = (pieProgressFloat/(startTimeInMillis)) * 100;
            pieProgressFloat = Math.round(pieProgressFloat);
            pieProgressInt = ((int) pieProgressFloat);
        } else {
            pieProgressInt = 100;
        }
        pieTimer.setProgress(pieProgressInt);
    }

    private void prepareAlteredTimer(int newTimeScaleIndex) {
        double timeRemainingRatio = (double)timeLeftInMillis / (double)startTimeInMillis;
        startTimeInMillis = (long)(standardStartTimeInMillis / timeScaleOptions[newTimeScaleIndex]);
        Log.i("Timeout prepare", "startTimeInMillis: " + startTimeInMillis);
        Log.i("Timeout prepare", "standardStartTimeInMillis: " + standardStartTimeInMillis);

        timeLeftInMillis = (long) (timeRemainingRatio * startTimeInMillis);
        timeScaleIndex = newTimeScaleIndex;
        updateTimeScaleText();
    }

    @Override
    public void onFragmentButtonClick(int newTimeScaleIndex) {
        if (newTimeScaleIndex != timeScaleIndex) {
            if (timerIsRunning) {
                pauseTimer();
                prepareAlteredTimer(newTimeScaleIndex);
                startTimer();
            } else {
                prepareAlteredTimer(newTimeScaleIndex);
            }
        }
    }
}
