package implems.event_queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class EventPump {

    private static EventPump instance;
    private final BlockingQueue<Runnable> taskQueue;
    private final Thread pumpThread;
    private final AtomicBoolean running = new AtomicBoolean(false);

    // Private constructor to prevent instantiation
    private EventPump() {
        this.taskQueue = new LinkedBlockingQueue<>();
        this.pumpThread = new Thread(() -> {
            while (running.get()) {
                try {
                    Runnable task = taskQueue.take(); // Blocks until a task is available
                    task.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    // Static method to get the single instance of EventPump
    public static synchronized EventPump getInstance() {
        if (instance == null) {
            instance = new EventPump();
        }
        return instance;
    }

    // Starts the event pump
    public void start() {
        if (!running.get()) {
            running.set(true);
            pumpThread.start();
        }
    }

    // Stops the event pump
    public void stop() {
        running.set(false);
        pumpThread.interrupt();
    }
    
    // Removes a task from the queue
    public boolean remove(Runnable task) {
        return taskQueue.remove(task);
    }

    // Posts a task to the queue
    public void post(Runnable task) {
        if (running.get()) {
            taskQueue.offer(task);
        } else {
            throw new IllegalStateException("EventPump is not running.");
        }
    }

    // Checks if the pump is running
    public boolean isRunning() {
        return running.get();
    }
}
