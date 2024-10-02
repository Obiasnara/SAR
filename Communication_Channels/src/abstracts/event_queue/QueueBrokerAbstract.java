package abstracts.event_queue;

public abstract class QueueBrokerAbstract {
	//QueueBrokerAbstract(String name){}
	interface AcceptListener {
		void accepted(QueueChannelAbstract queue);
	}
	abstract boolean bind(int port, AcceptListener listener);
	abstract boolean unbind(int port);
	
	interface ConnectListener {
		void connected(QueueChannelAbstract queue);
		void refused();
	}
	
	abstract boolean connect(String name, int port, ConnectListener listener);
}
