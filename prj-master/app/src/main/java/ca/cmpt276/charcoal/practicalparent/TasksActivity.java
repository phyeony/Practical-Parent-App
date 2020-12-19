package ca.cmpt276.charcoal.practicalparent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import ca.cmpt276.charcoal.practicalparent.model.Child;
import ca.cmpt276.charcoal.practicalparent.model.ChildManager;
import ca.cmpt276.charcoal.practicalparent.model.Task;
import ca.cmpt276.charcoal.practicalparent.model.TasksManager;


/**
 *  Creates ListView for Tasks Activity, and registers clicks for user interaction
 */
public class TasksActivity extends AppCompatActivity {
    private ArrayAdapter<Task> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        Toolbar toolbar = findViewById(R.id.toolbar_task);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        setupFab();
        populateListView();
        registerClickCallback();
    }

    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.fab_add_task);
        fab.setOnClickListener(view -> {
            Intent intent = EditTaskActivity.makeLaunchIntent(TasksActivity.this, -1);
            startActivity(intent);
        });
    }

    public static Intent makeLaunchIntent(Context context) {
        return new Intent(context, TasksActivity.class);
    }

    private void populateListView() {
        TasksManager manager = TasksManager.getInstance();
        List<Task> tasks = manager.getTasks();
        if (tasks != null) {
            adapter = new TaskListAdapter(this, tasks);

            ListView list = findViewById(R.id.list_tasks);
            list.setAdapter(adapter);
        }
    }

    private class TaskListAdapter extends ArrayAdapter<Task> {
        private final Context context;
        private final List<Task> tasks;

        public TaskListAdapter(Context context, List<Task> tasks) {
            super(context, 0, tasks);
            this.tasks = tasks;
            this.context = context;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if (listItem == null) {
                listItem = LayoutInflater.from(context).inflate(R.layout.task_row, parent, false);
            }

            Task currentTask = tasks.get(position);
            ChildManager childManager = ChildManager.getInstance();

            if (childManager.getChildren().size() > 0) {
                Child currentChild = childManager.getChild(currentTask.getChildIdx());

                ImageView childPortrait = listItem.findViewById(R.id.image_task_child_portrait);
                childPortrait.setImageBitmap(currentChild.getChildImage(context));

                TextView childName = listItem.findViewById(R.id.text_task_child_name);
                childName.setText(getString(R.string.label_name_placeholder, currentChild.getName()));
            }

            TextView taskName = listItem.findViewById(R.id.text_task_name);
            taskName.setText(getString(R.string.label_task_placeholder, currentTask.getTaskName()));

            return listItem;
        }
    }

    private void registerClickCallback() {
        ListView list = findViewById(R.id.list_tasks);
        list.setOnItemClickListener((parent, viewClicked, position, id) -> {
            Intent intent = TaskInformationActivity.makeLaunchIntent(TasksActivity.this, position);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        adapter.notifyDataSetChanged();
        super.onStart();
    }
}