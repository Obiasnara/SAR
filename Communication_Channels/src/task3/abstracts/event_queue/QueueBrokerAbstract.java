package task3.abstracts.event_queue;

import task3.implems.event_queue.errors.ConnectionRefused;

public abstract class QueueBrokerAbstract {
	//QueueBrokerAbstract(String name){}
	public interface AcceptListener {
		void accepted(QueueChannelAbstract queue);
	}
	public abstract boolean bind(int port, AcceptListener listener) throws InterruptedException, ConnectionRefused;
	public abstract boolean unbind(int port);
	
	public interface ConnectListener {
		void connected(QueueChannelAbstract queue);
		void refused();
	}
	
	public abstract boolean connect(String name, int port, ConnectListener listener);
}
