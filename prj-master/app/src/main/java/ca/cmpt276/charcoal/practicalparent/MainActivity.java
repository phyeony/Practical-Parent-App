package ca.cmpt276.charcoal.practicalparent;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

import java.util.List;

import ca.cmpt276.charcoal.practicalparent.model.Child;
import ca.cmpt276.charcoal.practicalparent.model.ChildManager;
import ca.cmpt276.charcoal.practicalparent.model.Record;
import ca.cmpt276.charcoal.practicalparent.model.RecordsConfig;
import ca.cmpt276.charcoal.practicalparent.model.Task;
import ca.cmpt276.charcoal.practicalparent.model.TasksManager;

/**
 *  Sets up Main Activity and buttons for starting activities
 */
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setupCoinActivityBtn();
        setupChildrenActivityBtn();
        setupTimeOutActivityBtn();
        setupChildren();
        setupTasks();
        createNotificationChannel();
        setupRecordActivityBtn();
        setupRecords();
        setupHelpActivityBtn();
        setupTaskActivityBtn();
        setupTakeBreathActivityBtn();
    }

    private void setupTakeBreathActivityBtn() {
        Button btn = findViewById(R.id.button_take_breath_activity);
        btn.setOnClickListener(v -> {
            Intent i = TakeBreathActivity.makeLaunchIntent(MainActivity.this);
            startActivity(i);
        });
    }

    private void setupTaskActivityBtn() {
        Button btn = findViewById(R.id.button_task_activity);
        btn.setOnClickListener(v -> {
            Intent i = TasksActivity.makeLaunchIntent(MainActivity.this);
            startActivity(i);
        });
    }

    private void setupHelpActivityBtn() {
        Button btn = findViewById(R.id.button_help_activity);
        btn.setOnClickListener(v -> {
            Intent i = HelpActivity.makeLaunchIntent(MainActivity.this);
            startActivity(i);
        });
    }

    private void setupCoinActivityBtn() {
        Button btn = findViewById(R.id.button_coin_flip_activity);
        btn.setOnClickListener(v -> {
            Intent i = CoinFlipActivity.makeLaunchIntent(MainActivity.this);
            startActivity(i);
        });
    }

    private void setupChildren() {
        List<Child> children = EditChildActivity.getSavedChildren(this);
        if (children != null) {
            ChildManager manager = ChildManager.getInstance();
            manager.setChildren(children);
        }
    }

    private void setupTasks() {
        List<Task> tasks = EditTaskActivity.getSavedTasks(this);
        if (tasks != null) {
            TasksManager manager = TasksManager.getInstance();
            manager.setTasks(tasks);
        }
    }

    private void setupRecords() {
        List<Child> user = RecordsConfig.readChildFromPref(this);
        List<String> choice = RecordsConfig.readChoiceFromPref(this);
        List<String> date = RecordsConfig.readDateFromPref(this);
        List<Integer> img = RecordsConfig.readImageFromPref(this);
        if (user != null) {
            Record recManager = Record.getInstance();
            recManager.setUsers(user);
            recManager.setDateTimes(date);
            recManager.setChoices(choice);
            recManager.setImages(img);
        }
    }

    private void setupTimeOutActivityBtn() {
        Button btn = findViewById(R.id.button_timeout_activity);
        btn.setOnClickListener(v -> {
            Intent i = TimeOutActivity.makeLaunchIntent(MainActivity.this);
            startActivity(i);
        });
    }

    private void setupRecordActivityBtn() {
        Button btn = findViewById(R.id.button_records_activity);
        btn.setOnClickListener(v -> {
            Intent i = RecordActivity.makeLaunchIntent(MainActivity.this);
            startActivity(i);
        });
    }

    private void setupChildrenActivityBtn() {
        Button btn =  findViewById(R.id.button_child_activity);
        btn.setOnClickListener(v -> {
            Intent i = ChildrenActivity.makeLaunchIntent(MainActivity.this);
            startActivity(i);
        });
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.notification_channel);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(getString(R.string.timeout_alarm_notification_ID),
                    name, importance);
            channel.setDescription(description);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}