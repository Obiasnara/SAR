package task2.implems.thread_queue;

import task1.abstracts.BrokerAbstract;
import task2.abstracts.thread_queue.QueueBrokerAbstract;
import task2.abstracts.thread_queue.TaskAbstract;

public class Task extends TaskAbstract {

	private BrokerAbstract broker;
	private QueueBrokerAbstract queueBroker;
	private Runnable runnable;
	private Thread thread;

	public Task(Runnable r) {
        this.broker = null;
        this.runnable = r;
    
        this.thread = new Thread(this.runnable);
        this.thread.start();
    }
	
	public Task(BrokerAbstract b, Runnable r) {
        this.broker = b;
        this.runnable = r;
    
        this.thread = new Thread(this.runnable, "TASK_THREAD");
        this.thread.start();
    }
	
	public Task(QueueBrokerAbstract b, Runnable r) {
        this.queueBroker = b;
        this.runnable = r;
    
        this.thread = new Thread(this.runnable);
        this.thread.start();
    }

	public static BrokerAbstract getBroker() { 
		return (BrokerAbstract) ((Task) Thread.currentThread()).broker;
	}
	
	public static QueueBrokerAbstract getQueueBroker() { 
		return ((Task) Thread.currentThread()).queueBroker;
	}
}
