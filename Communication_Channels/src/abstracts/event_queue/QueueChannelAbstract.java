package abstracts.event_queue;

public abstract class QueueChannelAbstract {
	interface Listener {
		void recieved(byte[] msg);
		void sent(Message msg);
		void closed();
	}
	
	abstract void setListener(Listener l);
	
	
}
