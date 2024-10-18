package task4.implems;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import task1.abstracts.ChannelAbstract;

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
		try {
			while (bytesRead == 0) {
				if (buffIn.empty()) {
					synchronized (buffIn) {
						while (buffIn.empty()){
							if (localDisconnected || remoteDisconnected.get()) {
								throw new IllegalStateException("Channel is disconnected, cannot read.");
							}
							try {
								buffIn.wait();
							} catch (InterruptedException e) {
								// Ignore
							}
						}
					}
				}
			
				while( bytesRead < length && !buffIn.empty()) {
					bytes[offset + bytesRead] = buffIn.pull();
					bytesRead++;
				} 
				if (bytesRead > 0) {
					synchronized (buffIn) {
						buffIn.notify();
					}
				}
			}
		} catch (IllegalStateException e) {
			if(!localDisconnected) {
				localDisconnected = true;
				synchronized (buffOut) {
					buffOut.notify();
				}
			}
			throw e;
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
		while (bytesWritten == 0) {	
			if (buffOut.full()) {
				synchronized (buffOut) {
					while (buffOut.full()){
						if (localDisconnected) {
							throw new IllegalStateException("Channel is disconnected, cannot read.");
						}
						if (remoteDisconnected.get()) {
							return length; // Silently ignore writing if remote side is disconnected
						}
						try {
							
							buffOut.wait();
						} catch (InterruptedException e) {
							// Ignore
						}
					}
				}
			}
		
			while (bytesWritten < length && !buffOut.full()) {
					buffOut.push(bytes[offset + bytesWritten]);
					bytesWritten++;
			}
			if (bytesWritten != 0) {
				synchronized (buffOut) {
					buffOut.notify();
				}
			}
		}
		return bytesWritten;
	}

	// Here we have a disconnect method that sets the localDisconnected flag to true 
	// we donc care about the local one as we wont be able to read or write anymore
	@Override
	public synchronized void disconnect() {
		localDisconnected = true;
		remoteDisconnected.set(true);
		
		synchronized (buffIn) {
			buffIn.notify();
		}
		synchronized (buffOut) {
			buffOut.notify();
		}
	}

	// If any of the two sides is disconnected, the channel is disconnected
	@Override
	public boolean disconnected() {
		return localDisconnected || remoteDisconnected.get();
	}

}
