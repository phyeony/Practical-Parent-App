package ca.cmpt276.charcoal.practicalparent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ca.cmpt276.charcoal.practicalparent.model.Child;
import ca.cmpt276.charcoal.practicalparent.model.ChildManager;
import ca.cmpt276.charcoal.practicalparent.model.RecordsConfig;

/**
 *  Sets up Record Activity, and Associated buttons for the activity
 */
public class RecordActivity extends AppCompatActivity {
    private Button currentRecords;
    private Button priorRecords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        setUpInitialButton();
        setUpRecordSelectorButtons();

        setUpListView();
    }

    private void setUpListView() {
        List<Child> childList = RecordsConfig.readChildFromPref(this);
        List<String> choices   = RecordsConfig.readChoiceFromPref(this);
        List<String> dateTimes= RecordsConfig.readDateFromPref(this);
        List<Integer> resultImages = RecordsConfig.readImageFromPref(this);

        ListView listView;
        if (childList != null && choices != null && dateTimes != null && resultImages != null ) {
            listView = findViewById(R.id.list_record);
            // Create adapter class
            MyAdapter adapter = new MyAdapter(this, childList, choices, resultImages, dateTimes);
            listView.setAdapter(adapter);
        }
    }

    class MyAdapter extends ArrayAdapter<Child> {
        Context context;
        List<Child> rChildren;
        List<String> rResults;
        List<String> rDateTime;
        List<Integer> rImgs;

        MyAdapter (Context c, List<Child> children, List<String> results, List<Integer> imgs, List<String> rDateTime) {
            super(c, 0, children);
            this.context = c;
            this.rChildren = children;
            this.rResults = results;
            this.rImgs = imgs;
            this.rDateTime = rDateTime;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = convertView;
            if (row == null) {
                row = layoutInflater.inflate(R.layout.record_row, parent, false);
            }
            ImageView images = row.findViewById(R.id.image_result);
            TextView whoPicked = row.findViewById(R.id.text_who_picked);
            TextView flipResult = row.findViewById(R.id.text_flip_result);
            TextView date = row.findViewById(R.id.text_date_time_flip);
            ImageView childPortrait = row.findViewById(R.id.image_record_child_portrait);

            Child currentChild = rChildren.get(position);

            // Now set our resources on views
            images.setImageResource(rImgs.get(position));
            whoPicked.setText(currentChild.getName());
            flipResult.setText(rResults.get(position));
            date.setText(rDateTime.get(position));
            childPortrait.setImageBitmap(currentChild.getChildImage(context));

            return row;
        }
    }

    private void setUpInitialButton() {
        currentRecords = findViewById(R.id.button_current_record);
        currentRecords.setBackgroundColor(getColor(R.color.unSelectedRecord));
        priorRecords = findViewById(R.id.button_prior_record);
        priorRecords.setBackgroundColor(getColor(R.color.selectedRecord));
    }

    public static Intent makeLaunchIntent(Context context) {
        return new Intent(context, RecordActivity.class);
    }

    private void setUpRecordSelectorButtons() {
        currentRecords = findViewById(R.id.button_current_record);
        priorRecords = findViewById(R.id.button_prior_record);
        currentRecords.setOnClickListener(v -> {
            Log.i("Record Activity", "Show current child records");
            currentRecords.setBackgroundColor(getColor(R.color.selectedRecord));
            showCurrentChildRecords();
            priorRecords.setBackgroundColor(getColor(R.color.unSelectedRecord));
        });
        priorRecords.setOnClickListener(v -> {
            setUpListView();
            priorRecords.setBackgroundColor(getColor(R.color.selectedRecord));
            currentRecords.setBackgroundColor(getColor(R.color.unSelectedRecord));
        });
    }

    private void showCurrentChildRecords() {
        int currentIndex = CoinFlipActivity.getCurrentIndex(this);

        List<Child> childList = RecordsConfig.readChildFromPref(this);
        List<String> choices = RecordsConfig.readChoiceFromPref(this);
        List<String> dateTimes = RecordsConfig.readDateFromPref(this);
        List<Integer> resultImages = RecordsConfig.readImageFromPref(this);

        List<Child> filteredChildren = new ArrayList<>();
        List<String> filteredChoices = new ArrayList<>();
        List<String> filteredDateTimes = new ArrayList<>();
        List<Integer> filteredResultImages = new ArrayList<>();

        ChildManager childrenManager = ChildManager.getInstance();

        if (childrenManager.getChildren().size() > 0) {
            Child currentChild = childrenManager.getChild(currentIndex);

            if (childList != null && choices != null && dateTimes != null && resultImages != null) {
                for (int i = 0; i < childList.size(); i++) {
                    if (childList.get(i).getName()
                            .equals(currentChild.getName()))
                    {
                        filteredChildren.add(currentChild);
                        filteredChoices.add(choices.get(i));
                        filteredDateTimes.add(dateTimes.get(i));
                        filteredResultImages.add(resultImages.get(i));
                    }
                }

                ListView listView = findViewById(R.id.list_record);
                MyAdapter adapter = new MyAdapter(this, filteredChildren, filteredChoices, filteredResultImages, filteredDateTimes);
                listView.setAdapter(adapter);
            }
        }
    }
}