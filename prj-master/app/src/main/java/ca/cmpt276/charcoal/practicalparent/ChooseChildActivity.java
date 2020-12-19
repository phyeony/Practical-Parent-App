package ca.cmpt276.charcoal.practicalparent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import ca.cmpt276.charcoal.practicalparent.model.Child;
import ca.cmpt276.charcoal.practicalparent.model.ChildManager;

import static ca.cmpt276.charcoal.practicalparent.CoinFlipActivity.getCurrentIndex;

/**
 *  Sets up overRide activity, and listview for selection
 */
public class ChooseChildActivity extends AppCompatActivity {
    int currentIndex;
    int newIndex;
    ListView listView;

    public static Intent makeLaunchIntent(Context context) {
        return new Intent(context, ChooseChildActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_child);

        // Enable "up" on toolbar
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        setUpListView();
    }

    private void setUpListView() {
        // Get the list of users
        ChildManager manager = ChildManager.getInstance();

        // Lists to be populated for queue
        List<Child> children = manager.getChildren();
        ArrayList<String> childrenNames = new ArrayList<>();
        ArrayList<String> Position = new ArrayList<>();
        ArrayList<Bitmap> qPortraits = new ArrayList<>();

        currentIndex  = getCurrentIndex(this);

        int[] range = IntStream.rangeClosed(0, children.size()-1).toArray();

        // Create range equivalent to the number of children
        for (int i: range) {
            Position.add("" + (i+1));

            qPortraits.add(manager.getChild(currentIndex).getChildImage(this));
            childrenNames.add(manager.getChild(currentIndex).getName());

            if (currentIndex < range.length-1) {
                currentIndex++;
            } else {
                currentIndex = 0;
            }
        }

        if (children != null) {
            listView = findViewById(R.id.list_queue);
            // create adapter class
            MyAdapter adapter = new MyAdapter(this, childrenNames, Position, qPortraits);

            // Set on click listener
            listView.setOnItemClickListener((parent, view, position, id) -> {
                view.setSelected(true);
                skipQueue(children, childrenNames.get(position));
            });
            listView.setAdapter(adapter);
        }
    }

    private void skipQueue(List<Child> children, String s) {
        findSelectedChild(children, s);
        swapChild(children, newIndex);

        Intent returnIntent = CoinFlipActivity.makeLaunchIntent(this);
        returnIntent.putExtra("newIndex", newIndex);

        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    private void findSelectedChild(List<Child> children, String s) {
        for (int i=0; i < children.size(); i++) {
            if (children.get(i).getName()==s) {
                newIndex = i;
            }
        }
    }

    private void swapChild(List<Child> children, int selectedIndex) {
        Child selectedChild = children.get(selectedIndex);

        for (int idx = (selectedIndex-1); idx >= 0; idx--) {
            children.set(idx+1, children.get(idx));
        }

        children.set(0,selectedChild);
        newIndex = 0;
    }

    class MyAdapter extends ArrayAdapter<String> {
        Context context;
        ArrayList<String> rChildName;
        ArrayList<String> rPosition;
        ArrayList<Bitmap> rPortrait;

        MyAdapter (Context c, ArrayList<String> childName, ArrayList<String> qPosition, ArrayList<Bitmap> imgs) {
            super(c, R.layout.queue_row, R.id.text_child_name_queue, childName);
            this.context = c;
            this.rChildName = childName;
            this.rPosition = qPosition;
            this.rPortrait = imgs;
        }

       @NonNull
       @Override
       public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
           LayoutInflater layoutInflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

           View row = layoutInflater.inflate(R.layout.queue_row, parent, false);
           ImageView portraits = row.findViewById(R.id.text_child_portrait);
           TextView turnNumber = row.findViewById(R.id.text_queue_position);
           TextView childName = row.findViewById(R.id.text_child_name_queue);

           // Now set our resources on views
           portraits.setImageBitmap(rPortrait.get(position));
           childName.setText(rChildName.get(position));
           turnNumber.setText(rPosition.get(position));

           return row;
        }
   }
}