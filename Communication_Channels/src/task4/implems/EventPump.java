package task4.implems;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import task4.abstracts.TaskAbstract;
import task4.implems.Task.EventToPublish;

public class EventPump extends Thread {

    private static EventPump instance;
    private final ArrayList<EventToPublish> taskQueue;
    private boolean running;
    // Private constructor to prevent instantiation
    private EventPump() {
    	this.setName("EVENT_PUMP");
        this.taskQueue = new ArrayList<>();
        running = true;
    }
    
    public void stopPump() {
    	running = false;
    }
    
    public enum TaskType {
        CONNECT, ACCEPT, READ, WRITE, UNKNOWN
    }

  
    private EventToPublish currentEvent;
    
    public TaskAbstract getCurrentTask() {
    	if (this.currentEvent == null) return null; 
    	return currentEvent.src;
    }
    
    protected final boolean VERBOSE = false;
    
    public synchronized void run() {
    	
    	while(running) {
    		
    		currentEvent = taskQueue.remove(0);
    		while(currentEvent!=null) {
    			currentEvent.run();
    			if(!taskQueue.isEmpty())
    				currentEvent = taskQueue.remove(0);
    		}
    		sleep();
    	}
    }

    // Static method to get the single instance of EventPump
    public static synchronized EventPump getInstance() {
        if (instance == null) {
            instance = new EventPump();
        }
        return instance;
    }
    
    // Removes a task from the queue
    public boolean remove(EventToPublish task) {
        return taskQueue.remove(task);
    }

    // Posts a task to the queue
    public synchronized void  post(EventToPublish task) {
    	if(!running) return; // Silently drop
        taskQueue.add(task);
        notify();
    }

  
    private void sleep() {
    	try {
    		wait();
    	} catch (InterruptedException ex) {
    		// Do nothing	
    	}
    }
}
