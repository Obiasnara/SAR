package abstracts;

public abstract class Task extends Thread {
	public Task(Broker b, Runnable r) {};
	public static Broker getBroker() { return null; }
}
