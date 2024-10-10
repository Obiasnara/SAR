package implems.event_queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import abstracts.event_queue.TaskAbstract;
import implems.event_queue.Task.EventToPublish;

public class EventPump extends Thread {

    private static EventPump instance;
    private final BlockingQueue<EventToPublish> taskQueue;
    private boolean running;
    // Private constructor to prevent instantiation
    private EventPump() {
        this.taskQueue = new LinkedBlockingQueue<>();
        running = true;
    }
    
    public void stopPump() {
    	running = false;
    }
    
    public enum TaskType {
        CONNECT, ACCEPT, READ, WRITE, UNKNOWN
    }

    public TaskType getTaskType(EventToPublish r) {
        if (r.r instanceof ReaderTask) return TaskType.READ;
        if (r.r instanceof WriteTask) return TaskType.WRITE;
        if (r.r instanceof AcceptTask) return TaskType.ACCEPT;
        if (r.r instanceof ConnectTask) return TaskType.CONNECT;
        
        return TaskType.UNKNOWN;
    }
    
    private EventToPublish currentEvent;
    public TaskAbstract getCurrentTask() {
    	if (this.currentEvent == null) return null; 
    	return currentEvent.src;
    }
    
    protected final boolean VERBOSE = false;
    
    public synchronized void run() {
    	
    	while(running) {
    		if (VERBOSE) System.out.println(taskQueue);
    		currentEvent = taskQueue.poll();
    		while(currentEvent!=null) {
    			switch (getTaskType(currentEvent)) {
    				case READ:
    					if (VERBOSE) System.out.println("READ");
    					// Dont do anything
    					break;
    				case WRITE:
    					if (VERBOSE) System.out.println("WRITE");
    					currentEvent.r.run();
    					break;
    				case ACCEPT:
    					if (VERBOSE) System.out.println("ACCEPT");
    					currentEvent.r.run();
    					break;
					case CONNECT:
						if (VERBOSE) System.out.println("CONNECT");
						currentEvent.r.run();
						break;
					case UNKNOWN:
						if (VERBOSE) System.out.println("UNKNOWN");
						currentEvent.run();
						break;
				
				default:
					break;
    			}
    			
    			if (VERBOSE) System.out.println(taskQueue);
    			currentEvent = taskQueue.poll();
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
        taskQueue.offer(task);
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
