package task2.abstracts.thread_queue;

public abstract class QueueBrokerAbstract {
    //	public QueueBroker(String name) { };
	public abstract QueueChannelAbstract accept(int port);
    public abstract QueueChannelAbstract connect(String name, int port);
    public abstract String name();
}
