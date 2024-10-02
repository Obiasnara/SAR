package abstracts.event_queue;

public abstract class TaskAbstract {
	abstract void post(Runnable r);
	static TaskAbstract task() { return null; };
	abstract void kill();
	abstract boolean killed();
}
