package implems;

import abstracts.BrokerAbstract;
import abstracts.TaskAbstract;

public class Task extends TaskAbstract {

	// Task has a UUID, a broker and a runnable
	private String uuid;
	private BrokerAbstract broker;
	private Runnable runnable;
	private Thread thread;

	public Task(BrokerAbstract b, Runnable r) {
		this.broker = b;
		this.runnable = r;
		this.uuid = java.util.UUID.randomUUID().toString();
		this.thread = new Thread(this.runnable, uuid);
		this.thread.start();
	}

	public static BrokerAbstract getBroker() { 
		return ((Task) Thread.currentThread()).broker;
	}
}
