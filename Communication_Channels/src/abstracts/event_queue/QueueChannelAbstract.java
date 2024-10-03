package abstracts.event_queue;

public abstract class QueueChannelAbstract {
	public interface Listener {
		void recieved(byte[] msg);
		void sent(Message msg);
		void closed();
	}
	
	public abstract void setListener(Listener l);
	
	
}
