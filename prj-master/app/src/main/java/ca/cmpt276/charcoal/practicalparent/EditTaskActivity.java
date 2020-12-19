package ca.cmpt276.charcoal.practicalparent;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import ca.cmpt276.charcoal.practicalparent.model.Task;
import ca.cmpt276.charcoal.practicalparent.model.TasksManager;

/**
 *  Sets up Edit Task Activity, Allows for Editing Tasks, and saving data
 */
public class EditTaskActivity extends AppCompatActivity {
    private static final String TAG = "EditTaskActivity";
    private static final String PREFS_NAME = "SavedData";
    private static final String TASKS_PREF = "Tasks";
    public static final String EXTRA_TASK_INDEX = "ca.cmpt276.charcoal.practicalparent - taskIndex";
    private int taskIndex;
    private EditText taskNameBox;
    private final TasksManager taskManager = TasksManager.getInstance();

    public static Intent makeLaunchIntent(Context context, int taskIndex) {
        Intent intent = new Intent(context, EditTaskActivity.class);
        intent.putExtra(EXTRA_TASK_INDEX, taskIndex);
        return intent;
    }

    private void extractIntentData() {
        Intent intent = getIntent();
        taskIndex = intent.getIntExtra(EXTRA_TASK_INDEX, -1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        setupSaveButton();
        extractIntentData();
        preFillNameBox();
    }

    private void preFillNameBox() {
        taskNameBox = findViewById(R.id.edit_task_name);
        if (taskIndex >= 0) {
            Task currentTask = taskManager.getTask(taskIndex);
            taskNameBox.setText(currentTask.getTaskName());
        }
    }

    private void setupSaveButton() {
        Button saveBtn = findViewById(R.id.button_save_task);
        saveBtn.setOnClickListener(v -> saveTaskInManager());
    }

    private void saveTaskInManager() {
        String taskName = taskNameBox.getText().toString();
        if (nameIsValid(taskName)) {
            if (taskIndex >= 0) {
                Task currentTask = taskManager.getTask(taskIndex);
                currentTask.setTaskName(taskName);
            } else {
                taskManager.add(new Task(taskName));
            }
            saveTasksInSharedPrefs(this);
            finish();
        }
    }

    private boolean nameIsValid(String taskName) {
        if (taskName.length() == 0) {
            taskNameBox.setError(getString(R.string.edit_child_error_name));
            return false;
        } else {
            for (Task task : taskManager.getTasks()) {
                if (taskName.equals(task.getTaskName())) {
                    taskNameBox.setError("Tasks names must be unique");
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_task, menu);
        if (taskIndex == -1) {
            menu.findItem(R.id.action_delete).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_delete) {
            if (taskIndex >= 0) {
                taskManager.remove(taskIndex);
                saveTasksInSharedPrefs(this);
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public static void saveTasksInSharedPrefs(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        List<Task> tasks = TasksManager.getInstance().getTasks();
        Gson gson = new Gson();
        String json = gson.toJson(tasks);
        Log.i(TAG, json + "");

        editor.putString(TASKS_PREF, json);
        editor.apply();
    }

    // Reference - Gson serialization code found here:
    //   https://stackoverflow.com/questions/28107647/how-to-save-listobject-to-sharedpreferences/28107838
    public static List<Task> getSavedTasks(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        List<Task> tasks;
        String serializedTasks = prefs.getString(TASKS_PREF, null);
        Log.i(TAG , "serializedTasks: " + serializedTasks);
        if (serializedTasks != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Task>>(){}.getType();
            Log.i(TAG, "Type: " + type);
            tasks = gson.fromJson(serializedTasks, type);
            Log.i(TAG, "tasks after getting the data from sharedPref: " + tasks);
            return tasks;
        } else {
            return null;
        }
    }
}