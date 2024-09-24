package implems;

import abstracts.ChannelAbstract;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Channel extends ChannelAbstract {

	private volatile boolean localDisconnected = false;   
	private final AtomicBoolean remoteDisconnected; 
	
    protected CircularBuffer buffIn;
	protected CircularBuffer buffOut;
    ConcurrentHashMap<String, CircularBuffer> readBufferMap = new ConcurrentHashMap<>();

	static final int BUFFER_SIZE = 10;


	public Channel(CircularBuffer buffIn,  CircularBuffer buffOut, AtomicBoolean disconnected) {
		this.buffIn = buffIn;
		this.buffOut = buffOut;
		this.remoteDisconnected = disconnected;
	}

	
	@Override
	public int read(byte[] bytes, int offset, int length) {
		// Allow reading until input buffer is empty or local side disconnects
		if (localDisconnected) {
			throw new IllegalStateException("Local channel is disconnected, cannot read.");
		}

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
				break; // No more bytes in transit
			}
		}

		// If buffer is empty, consider remote side disconnected
		if (buffIn.empty() && remoteDisconnected.get()) {
			localDisconnected = true;
		}
		return bytesRead;
	}

	@Override
	public int write(byte[] bytes, int offset, int length) {
		// If local or remote side is disconnected, writing is not allowed
		if (localDisconnected) {
			throw new IllegalStateException("Channel is disconnected, cannot write.");
		}

		int bytesWritten = 0;
		while (bytesWritten < length) {	
			if (localDisconnected || remoteDisconnected.get()) {
				// Drop bytes silently after disconnection
				return bytesWritten;
			}

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

	// Here we have a disconnect method that sets the localDisconnected flag to true 
	// we donc care about the local one as we wont be able to read or write anymore
	@Override
	public void disconnect() {
		localDisconnected = true;
		remoteDisconnected.set(true);
	}

	// If any of the two sides is disconnected, the channel is disconnected
	@Override
	public boolean disconnected() {
		return localDisconnected || remoteDisconnected.get();
	}

}
