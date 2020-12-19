package ca.cmpt276.charcoal.practicalparent.model;

import java.util.ArrayList;
import java.util.List;

/**
 *  Child manager activity stores information required for child
 */
public class ChildManager {
    private List<Child> children = new ArrayList<>();
    private static ChildManager instance;

    private ChildManager() {
    }

    public static ChildManager getInstance() {
        if (instance == null) {
            instance = new ChildManager();
        }
        return instance;
    }

    public void add(Child child) {
        children.add(child);
    }

    public Child getChild(int index) {
        return children.get(index);
    }

    public void remove(int index) {
        children.remove(index);
    }

    public List<Child> getChildren() {
        return children;
    }

    public void setChildren (List<Child> children) {
        this.children = children;
    }
}
