package task4.implems;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import task4.abstracts.TaskAbstract;
import task4.implems.Task.EventToPublish;

public class EventPump extends Thread {

	private static EventPump instance;
    private final BlockingQueue<EventToPublish> taskQueue;
    private boolean running;

    // Private constructor to prevent instantiation
    private EventPump() {
        this.setName("EVENT_PUMP");
        this.taskQueue = new LinkedBlockingQueue<>(); // Thread-safe queue
        this.running = true;
    }

    public void stopPump() {
        running = false;
        interrupt(); // Interrupt the thread when stopping
    }

    private EventToPublish currentEvent;

    public TaskAbstract getCurrentTask() {
        if (this.currentEvent == null) return null;
        return currentEvent.src;
    }

    protected final boolean VERBOSE = false;

    @Override
    public void run() {
        while (running) {
            try {
                // Take blocks until a task is available
                currentEvent = taskQueue.take(); 
                if( VERBOSE ) System.out.println("Pump processing : " + currentEvent.name);
                currentEvent.run();
            } catch (InterruptedException e) {
                // If interrupted and we're no longer running, exit loop
                if (!running) {
                    break;
                }
            }
        }
    }

    // Static method to get the single instance of EventPump
    public static synchronized EventPump getInstance() {
        if (instance == null) {
            instance = new EventPump();
        }
        return instance;
    }

    // Posts a task to the queue
    public void post(EventToPublish task) {
        if (!running) return; // Silently drop if the pump is not running
        taskQueue.offer(task); // Non-blocking, thread-safe method to add a task
    }
}
