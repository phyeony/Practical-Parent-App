package ca.cmpt276.charcoal.practicalparent.model;

import androidx.annotation.NonNull;

import java.util.Random;

public class Task {
    private String taskName;
    private int childIdx;

    public Task(String taskName) {
        this.taskName = taskName;
        ChildManager childManager = ChildManager.getInstance();
        if (childManager.getChildren().size() <= 0){
            this.childIdx = 0;
        } else {
            this.childIdx = new Random().nextInt(childManager.getChildren().size());
        }
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public int getChildIdx() {
        return childIdx;
    }

    public void setChildIdx(int childIdx) {
        this.childIdx = childIdx;
    }

    @NonNull
    @Override
    public String toString() {
        ChildManager manager = ChildManager.getInstance();
        if (manager.getChildren().size() <= 0 ) {
            return ("Task: " + taskName + "  Name:  " );
        } else {
            return ("Task: " + taskName + "  Name: " + manager.getChild(childIdx).getName());
        }
    }
}
