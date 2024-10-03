package abstracts.thread_queue;

import abstracts.BrokerAbstract;

public abstract class TaskAbstract extends Thread {
	// public Task(Broker b, Runnable r) {};
    // public Task(QueueBroker b, Runnable r);

	public static BrokerAbstract getBroker() { return null; }
	public static QueueBrokerAbstract getQueueBroker() { return null; }
}
