package implems;

import abstracts.BrokerAbstract;
import abstracts.TaskAbstract;

public class Task extends TaskAbstract {

	private BrokerAbstract broker;
	private Runnable runnable;

	public Task(BrokerAbstract b, Runnable r) {
		this.broker = b;
		this.runnable = r;
		this.runnable.run();
	}
}
