package task4.implems;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import task4.abstracts.ChannelAbstract;

public class Channel extends ChannelAbstract {
	
	public interface ReadListener {
	    void read(byte[] bytes);
	}

    public interface WriteListener {
    	// void available(Channel l);
    	void written(int byteWrote);
    }
	
	private volatile boolean localDisconnected = false;   
	private final AtomicBoolean remoteDisconnected; 

	ReadListener listener;
	
    protected CircularBuffer buffIn;
	protected CircularBuffer buffOut;

	public Channel(CircularBuffer buffIn,  CircularBuffer buffOut, AtomicBoolean disconnected) {
		this.buffIn = buffIn;
		this.buffOut = buffOut;
		this.remoteDisconnected = disconnected;
	}

	
	@Override
	public boolean read(byte[] bytes, int offset, int length) {
		
		// Allow reading until input buffer is empty or local side disconnects
		if (localDisconnected) {
			return false;
		}

		
		if (buffIn.empty()) {
			if (remoteDisconnected.get()) return false;
			
			if(this.listener != null) this.listener.read(new byte[0]);
			
			return true;
		}
		int i = 0;
		while(i < length && !buffIn.empty()) {
			bytes[offset + i] = buffIn.pull();
			i++;
		} 
		
		if(this.listener != null) this.listener.read(bytes);
		
		return true;
	}

	@Override
	public boolean write(byte[] bytes, int offset, int length, WriteListener wl) {
		// If local or remote side is disconnected, writing is not allowed
		if (localDisconnected) {
			return false;
		}

		if (remoteDisconnected.get()) {
			// Silently ignore writing if remote side is disconnected "Wrote length"
			Task.task().post(()-> wl.written(length));
			return true; 
		}
		
		final int[] bytesWritten = {0}; // Use a final array as a wrapper

		
		if (buffOut.full()) {
			Task.task().post(()-> wl.written(0));
			return true; // "Wrote 0"
		}
		
		while (bytesWritten[0] < length && !buffOut.full()) {
				buffOut.push(bytes[offset + bytesWritten[0]]);
				bytesWritten[0]++;
				
		}
		
		// "Wrote bytesWritten"
		Task.task().post(()-> wl.written(bytesWritten[0]));
		
		return true;
	}

	// Here we have a disconnect method that sets the localDisconnected flag to true 
	// we donc care about the local one as we wont be able to read or write anymore
	@Override
	public synchronized void disconnect() {
		localDisconnected = true;
		remoteDisconnected.set(true);	
	}

	// If any of the two sides is disconnected, the channel is disconnected
	@Override
	public boolean disconnected() {
		return localDisconnected || remoteDisconnected.get();
	}


	@Override
	public void setListener(ReadListener listener) {
		this.listener = listener;
	}




}
