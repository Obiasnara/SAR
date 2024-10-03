package implems.thread_queue;

import abstracts.BrokerAbstract;
import abstracts.thread_queue.QueueBrokerAbstract;
import abstracts.thread_queue.TaskAbstract;

public class Task extends TaskAbstract {

	private BrokerAbstract broker;
	private QueueBrokerAbstract queueBroker;
	private Runnable runnable;
	private Thread thread;

	public Task(BrokerAbstract b, Runnable r) {
        this.broker = b;
        this.runnable = r;
    
        this.thread = new Thread(this.runnable);
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
