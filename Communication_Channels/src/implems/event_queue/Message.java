package implems.event_queue;

public class Message {
	public byte[] bytes;
	public int offset;
	public int length;
	
	public Message(byte[] msg) {
		this.bytes = msg;
		this.length = msg.length;
		this.offset = 0;
	}
}
