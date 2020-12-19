package ca.cmpt276.charcoal.practicalparent.model;

import java.util.ArrayList;
import java.util.List;

public class TasksManager {
    List<Task> tasks = new ArrayList<>();
    private final ChildManager childManager = ChildManager.getInstance();
    private static TasksManager instance;

    private TasksManager() {
    }

    public static TasksManager getInstance() {
        if (instance == null) {
            instance = new TasksManager();
        }
        return instance;
    }

    public void add(Task task) {
        tasks.add(task);
    }

    public void reassignChildIdx(int taskIdx) {
        Task task = tasks.get(taskIdx);
        if (childManager.getChildren().size() <= 0) {
            task.setChildIdx(0);
        } else {
            int currentChildIdx = task.getChildIdx();
            int newChildIdx = (currentChildIdx+1) % (childManager.getChildren().size());
            task.setChildIdx(newChildIdx);
        }
    }

    public void reassignTaskForDeletedChild(int deletedChildIndex) {
        for (Task task : tasks) {
            if (task.getChildIdx() > deletedChildIndex) {
                int currentChildIdx = task.getChildIdx();
                int newChildIdx = currentChildIdx - 1;
                task.setChildIdx(newChildIdx);
            }

            if (childManager.getChildren().size() <= 0) {
                task.setChildIdx(0);
            } else if (task.getChildIdx() == childManager.getChildren().size()) {
                int currentChildIdx = task.getChildIdx();
                int newChildIdx = (currentChildIdx) % (childManager.getChildren().size());
                task.setChildIdx(newChildIdx);
            }
        }
    }

    public Task getTask(int index) {
        return tasks.get(index);
    }

    public void remove(int index) {
        tasks.remove(index);
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks (List<Task> tasks) {
        this.tasks = tasks;
    }
}
