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
		while (bytesRead < length) {
			if (!buffIn.empty()) {
				try {
					bytes[offset + bytesRead] = buffIn.pull();
				} catch (IllegalStateException e) {
					System.err.println("Error: " + e.getMessage());
					break;
				}
				bytesRead++;
			} else {
				break;
			}
		}
		return bytesRead;
	}

	@Override
	public int write(byte[] bytes, int offset, int length) {
		if (disconnected()) {
			return length;
		}
		int bytesWritten = 0;
		while (bytesWritten < length) {	
			if (!buffOut.full()) {
				try {
					buffOut.push(bytes[offset + bytesWritten]);
				} catch (IllegalStateException e) {
					System.err.println("Error: " + e.getMessage());
					break;
				}
				bytesWritten++;
			} else {
			break;
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
