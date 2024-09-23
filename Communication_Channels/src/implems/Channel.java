package implems;

import abstracts.ChannelAbstract;
import java.util.concurrent.ConcurrentHashMap;

public class Channel extends ChannelAbstract {

	private boolean connected = true;

    protected CircularBuffer buffIn;
	protected CircularBuffer buffOut;
    ConcurrentHashMap<String, CircularBuffer> readBufferMap = new ConcurrentHashMap<>();

	static final int BUFFER_SIZE = 10;


	public Channel(CircularBuffer buffIn,  CircularBuffer buffOut) {
		this.buffIn = buffIn;
		this.buffOut = buffOut;
	}

	
	@Override
	public int read(byte[] bytes, int offset, int length) {

		int bytesRead = 0;
		while (bytesRead == 0) {
			try {
				for (int i = offset; i < offset + length; i++) {
					if (!buffIn.empty()) {
						bytes[i] = buffIn.pull();
						bytesRead++;
					} else {
						break;
					}
				}
			} catch (IllegalStateException e) { // bufferOne is empty
				System.out.println("This state is Illegal, tried to read from an empty buffer");
			}
		}
		return bytesRead;
	}

	@Override
	public int write(byte[] bytes, int offset, int length) {

		int bytesWritten = 0;
		while (bytesWritten == 0) {
			
				try {
					for (int i = offset; i < offset + length; i++) {
						if (!buffOut.full()) {
							buffOut.push(bytes[i]);
							bytesWritten++;
						} else {
							break;
						}
					}
				} catch (IllegalStateException e) { // bufferTwo is full
					System.out.println("This state is Illegal, tried to write on a full buffer");
				}
		}
		return bytesWritten;
	}

	@Override
	public void disconnect() {
		connected = false;
	}

	@Override
	public boolean disconnected() {
		return connected;
	}

}
