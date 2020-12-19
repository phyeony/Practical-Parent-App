package ca.cmpt276.charcoal.practicalparent.model;

import java.util.ArrayList;
import java.util.List;

import ca.cmpt276.charcoal.practicalparent.R;

/**
 *  Records class allows for storage of user, dates, and information required for records activity
 */
public class Record {
    private List<Child> children= new ArrayList<>();
    private List<String> dateTimes = new ArrayList<>();
    private List<String> choices = new ArrayList<>();
    private List<Integer> images = new ArrayList<>();
    private static Record instance;

    private Record() {
    }

    public void addChild(Child child) {
        children.add(0, child);
    }

    public void addDateTime(String dateTime) {
        dateTimes.add(0, dateTime);
    }

    public void addChoice(String choice) {
        choices.add(0, choice);
    }

    public void addResult(Boolean result) {
        if (result) {
            images.add(0, R.drawable.ic_won);
        } else {
            images.add(0, R.drawable.ic_lost);
        }
    }

    public List<Child> getChildren() {
        return children;
    }

    public List<String> getDateTimes() {return dateTimes;
    }

    public List<String> getChoices() {return choices;
    }

    public static Record getInstance() {
        if (instance == null) {
            instance = new Record();
        }
        return  instance;
    }
    public List<Integer> getImages() {
        return images;
    }

    public void setUsers(List<Child> children) {
        this.children = children;
    }

    public void setDateTimes(List<String> users) {
        this.dateTimes = users;
    }

    public void setChoices(List<String> users) {
        this.choices = users;
    }

    public void setImages(List<Integer> image) {
        this.images = image;
    }
}
