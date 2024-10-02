package implems.thread_queue;

import abstracts.BaseBrokerAbstract;
import abstracts.BrokerAbstract;
import abstracts.thread_queue.QueueBrokerAbstract;
import abstracts.thread_queue.TaskAbstract;

public class Task extends TaskAbstract {

	// Task has a UUID, a broker and a runnable
	private String uuid;
	private BaseBrokerAbstract broker;
	private Runnable runnable;
	private Thread thread;

	public Task(BaseBrokerAbstract b, Runnable r) {
        this.broker = b;
        this.runnable = r;
        this.uuid = java.util.UUID.randomUUID().toString();
        this.thread = new Thread(this.runnable, uuid);
        this.thread.start();
    }

	public static BrokerAbstract getBroker() { 
		return (BrokerAbstract) ((Task) Thread.currentThread()).broker;
	}
	
	public static QueueBrokerAbstract getQueueBroker() { 
		return (QueueBrokerAbstract) ((Task) Thread.currentThread()).broker;
	}
}
