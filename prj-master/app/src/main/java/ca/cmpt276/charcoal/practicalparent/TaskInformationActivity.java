package ca.cmpt276.charcoal.practicalparent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import ca.cmpt276.charcoal.practicalparent.model.Child;
import ca.cmpt276.charcoal.practicalparent.model.ChildManager;
import ca.cmpt276.charcoal.practicalparent.model.Task;
import ca.cmpt276.charcoal.practicalparent.model.TasksManager;

/**
 * Displays information about the selected task including the name and which child is assigned
 * Allows the user to mark the task as complete which advances it to the next child
 */
public class TaskInformationActivity extends AppCompatActivity {
    public static final String EXTRA_TASK_INDEX = "ca.cmpt276.charcoal.practicalparent - taskIndex";
    private int taskIndex;
    private final ChildManager childManager = ChildManager.getInstance();
    private final TasksManager taskManager = TasksManager.getInstance();

    public static Intent makeLaunchIntent(Context context, int childIndex) {
        Intent intent = new Intent(context, TaskInformationActivity.class);
        intent.putExtra(EXTRA_TASK_INDEX, childIndex);
        return intent;
    }

    private void extractIntentData() {
        Intent intent = getIntent();
        taskIndex = intent.getIntExtra(EXTRA_TASK_INDEX, -1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_information);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        extractIntentData();

        setupFinishedTaskButton();
        populateTextViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task_information, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_edit) {
            Intent intent = EditTaskActivity.makeLaunchIntent(this, taskIndex);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void populateTextViews() {
        if (taskIndex >= 0) {
            TextView childNameBox = findViewById(R.id.text_child_name_task);
            TextView taskNameBox = findViewById(R.id.text_info_task_name);
            ImageView childImage = findViewById(R.id.task_information_image_child);

            Task currentTask = taskManager.getTask(taskIndex);
            taskNameBox.setText(currentTask.getTaskName());
            if (childManager.getChildren().size() <= 0) {
                childNameBox.setText(R.string.msg_add_your_child);
            } else {
                Child currentChild = childManager.getChild(currentTask.getChildIdx());
                childNameBox.setText(String.format("%s", currentChild.getName()));
                childImage.setImageBitmap(currentChild.getChildImage(this));
            }
        }
    }

    private void setupFinishedTaskButton() {
        Button finishedTaskButton = findViewById(R.id.button_task_finished);
        finishedTaskButton.setOnClickListener(v -> {
            if (childManager.getChildren().size() <= 0 ) {
                Toast.makeText(TaskInformationActivity.this,"No Child Added ", Toast.LENGTH_SHORT)
                        .show();
            } else {
                taskManager.reassignChildIdx(taskIndex);
                EditTaskActivity.saveTasksInSharedPrefs(this);
                finish();
            }
        });
    }
}