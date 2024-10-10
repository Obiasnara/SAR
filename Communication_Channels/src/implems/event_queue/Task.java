package implems.event_queue;

import abstracts.event_queue.TaskAbstract;

public class Task extends TaskAbstract {

	private boolean killed = false;
    private EventToPublish currentEvent;
    public QueueChannel queue;
    
    protected class EventToPublish {
    	TaskAbstract src; TaskAbstract dst; Runnable r;
    	public EventToPublish(TaskAbstract src, TaskAbstract dst, Runnable r) {
    		this.src = src; this.dst = dst; this.r = r;
    	}
    	
    	public void run() {
    		this.r.run();
    	}
    }
    
    public static TaskAbstract task() {
    	TaskAbstract curr = EventPump.getInstance().getCurrentTask();
    	if (curr == null) return new Task(); // For going back to event world
    	return curr;
    }

    @Override
    public void post(Runnable r) {
    	if (!killed) {
    		EventToPublish e = new EventToPublish(task(), this, r);
	        EventPump.getInstance().post(e);
	        this.currentEvent = e;
    	}
    }

    @Override
    public void kill() {
        if (this.currentEvent != null) {
            // Remove the task from the pump if it's still in the queue
            EventPump.getInstance().remove(this.currentEvent);
            this.currentEvent = null; // Clear reference to avoid reuse
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
