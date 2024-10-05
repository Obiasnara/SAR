package implems.event_queue;

import abstracts.event_queue.TaskAbstract;

public class Task extends TaskAbstract {

    private Thread thread;
    private Runnable currentTask;
    public QueueChannel queue;

    public static TaskAbstract task() {
         //return (TaskAbstract) Thread.currentThread();
    	return null;
    }

    @Override
    public void post(Runnable r) {
        this.currentTask = r;
        EventPump.getInstance().post(r);
    }

    @Override
    public void kill() {
        if (this.currentTask != null) {
            // Remove the task from the pump if it's still in the queue
            EventPump.getInstance().remove(this.currentTask);
            this.currentTask = null; // Clear reference to avoid reuse
        }

        // Interrupt the thread if it's already started execution (ie, removed from queue and currently running)
        if (this.thread != null && this.thread.isAlive()) {
            this.thread.interrupt();
        }
    }

    @Override
    public boolean killed() {
        return this.currentTask == null && (this.thread == null || this.thread.isInterrupted());
    }
}
