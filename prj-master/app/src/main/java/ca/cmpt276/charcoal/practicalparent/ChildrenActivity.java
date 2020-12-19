package ca.cmpt276.charcoal.practicalparent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import ca.cmpt276.charcoal.practicalparent.model.Child;
import ca.cmpt276.charcoal.practicalparent.model.ChildManager;

/**
 *  Creates ListView for Children Activity, and registers clicks for user interaction
 */
public class ChildrenActivity extends AppCompatActivity {
    private ChildListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_children);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        setupFab();
        populateListView();
        registerClickCallback();
    }

    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.addChild_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = EditChildActivity.makeLaunchIntent(ChildrenActivity.this, -1);
                startActivity(intent);
            }
        });
    }

    public static Intent makeLaunchIntent(Context context) {
        return new Intent(context, ChildrenActivity.class);
    }

    private void populateListView() {
        ChildManager manager = ChildManager.getInstance();
        List<Child> children = manager.getChildren();
        if (children != null) {
            adapter = new ChildListAdapter(this, children);

            ListView list = findViewById(R.id.list_children);
            list.setAdapter(adapter);
        }
    }

    private class ChildListAdapter extends ArrayAdapter<Child> {
        private final Context context;
        private final List<Child> children;

        public ChildListAdapter(Context context, List<Child> children) {
            super(context, 0, children);
            this.children = children;
            this.context = context;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if (listItem == null) {
                listItem = LayoutInflater.from(context).inflate(R.layout.child_row, parent, false);
            }

            if (ChildManager.getInstance().getChildren().size() > 0) {
                Child currentChild = children.get(position);

                TextView childName = listItem.findViewById(R.id.text_child_name_row);
                childName.setText(currentChild.getName());

                ImageView childImage = listItem.findViewById(R.id.image_child_portrait_row);
                childImage.setImageBitmap(currentChild.getChildImage(context));
            }
            return listItem;
        }
    }

    private void registerClickCallback() {
        ListView list = findViewById(R.id.list_children);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                Intent intent = EditChildActivity.makeLaunchIntent(ChildrenActivity.this, position);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        adapter.notifyDataSetChanged();
        super.onStart();
    }
}
