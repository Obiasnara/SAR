package abstracts.event_queue;

public abstract class QueueBrokerAbstract {
	//QueueBrokerAbstract(String name){}
	public interface AcceptListener {
		void accepted(QueueChannelAbstract queue);
	}
	public abstract boolean bind(int port, AcceptListener listener);
	public abstract boolean unbind(int port);
	
	public interface ConnectListener {
		void connected(QueueChannelAbstract queue);
		void refused();
	}
	
	public abstract boolean connect(String name, int port, ConnectListener listener);
}
