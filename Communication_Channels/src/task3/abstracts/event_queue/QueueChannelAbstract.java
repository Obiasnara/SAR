package task3.abstracts.event_queue;

import task3.implems.event_queue.Message;

public abstract class QueueChannelAbstract {
	public interface Listener {
		void received(byte[] msg);
		void sent(Message msg);
		void closed();
	}
	
	public abstract void setListener(Listener l);
	
	public abstract boolean send(Message mst);
	
	public abstract void close();
	
	public abstract boolean closed();
}
