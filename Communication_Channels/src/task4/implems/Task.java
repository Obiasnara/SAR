package task4.implems;

import task4.abstracts.TaskAbstract;

public class Task extends TaskAbstract {

	private boolean killed = false;
    private EventToPublish currentEvent;
    
    protected class EventToPublish {
    	String name;
    	TaskAbstract src; TaskAbstract dst; Runnable r;
    	public EventToPublish(TaskAbstract src, TaskAbstract dst, Runnable r) {
    		this.src = src; this.dst = dst; this.r = r;
    	}
    	public EventToPublish(TaskAbstract src, TaskAbstract dst, Runnable r, String name) {
    		this.src = src; this.dst = dst; this.r = r; this.name = name;
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
    public void post(Runnable r, String name) {
    	if (!killed) {
    		EventToPublish e = new EventToPublish(task(), this, r, name);
	        EventPump.getInstance().post(e);
	        this.currentEvent = e;
    	}
    }

    @Override
    public void kill() {
        // TODO code that function ^^'
    }	

    @Override
    public boolean killed() {
        return killed;
    }
}
