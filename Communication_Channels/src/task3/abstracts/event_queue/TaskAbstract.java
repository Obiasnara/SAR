package task3.abstracts.event_queue;

public abstract class TaskAbstract extends Thread {
	public abstract void post(Runnable r);
//	public static TaskAbstract task() { return null; };
	public abstract void kill();
	public abstract boolean killed();
}
