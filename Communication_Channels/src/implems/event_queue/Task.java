package implems.event_queue;

import abstracts.event_queue.TaskAbstract;

public class Task extends TaskAbstract {

	private boolean killed = false;
    private Runnable currentTask;
    public QueueChannel queue;
    
    public static TaskAbstract task() {
         //return (TaskAbstract) Thread.currentThread();
    	return new Task();
    }

    @Override
    public void post(Runnable r) {
    	if (!killed) {
	        this.currentTask = r;
	        EventPump.getInstance().post(r);
    	}
    }

    @Override
    public void kill() {
        if (this.currentTask != null) {
            // Remove the task from the pump if it's still in the queue
            EventPump.getInstance().remove(this.currentTask);
            this.currentTask = null; // Clear reference to avoid reuse
        }
        if (this.queue != null) {
        	this.queue.close();
        }
        killed = true;
    }	

    @Override
    public boolean killed() {
        return killed;
    }
}
