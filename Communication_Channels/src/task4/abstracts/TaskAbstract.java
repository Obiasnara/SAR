package task4.abstracts;

public abstract class TaskAbstract extends Thread {
	public abstract void post(Runnable r);
	public abstract void post(Runnable r, String name);
	public abstract void kill();
	public abstract boolean killed();
}
