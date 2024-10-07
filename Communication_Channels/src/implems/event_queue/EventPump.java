package implems.event_queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class EventPump extends Thread {

    private static EventPump instance;
    private final BlockingQueue<Runnable> taskQueue;
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

    public TaskType getTaskType(Runnable r) {
        if (r instanceof ReaderTask) return TaskType.READ;
        if (r instanceof WriteTask) return TaskType.WRITE;
        if (r instanceof AcceptTask) return TaskType.ACCEPT;
        if (r instanceof ConnectTask) return TaskType.CONNECT;
        
        return TaskType.UNKNOWN;
    }
    
    protected final boolean VERBOSE = false;
    
    public synchronized void run() {
    	Runnable r;
    	while(running) {
    		if (VERBOSE) System.out.println(taskQueue);
    		r = taskQueue.poll();
    		while(r!=null) {
    			switch (getTaskType(r)) {
    				case READ:
    					if (VERBOSE) System.out.println("READ");
    					Thread t = new Thread(r);
    					t.setDaemon(true);
    					t.start();
    					break;
    				case WRITE:
    					if (VERBOSE) System.out.println("WRITE");
						r.run();
						break;
    				case ACCEPT:
    					if (VERBOSE) System.out.println("ACCEPT");
						r.run();
						break;
					case CONNECT:
						if (VERBOSE) System.out.println("CONNECT");
						r.run();
						break;
					case UNKNOWN:
						if (VERBOSE) System.out.println("UNKNOWN");
    					r.run();
    					break;
				
				default:
					break;
    			}
    			if (VERBOSE) System.out.println(taskQueue);
    			r = taskQueue.poll();
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
    public boolean remove(Runnable task) {
        return taskQueue.remove(task);
    }

    // Posts a task to the queue
    public synchronized void  post(Runnable task) {
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
