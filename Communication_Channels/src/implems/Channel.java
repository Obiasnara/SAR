package implems;

import java.util.concurrent.ConcurrentHashMap;

import abstracts.ChannelAbstract;

public class Channel extends ChannelAbstract {

	private boolean connected = true;

    ConcurrentHashMap<String, CircularBuffer> writeBufferMap = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, CircularBuffer> readBufferMap = new ConcurrentHashMap<>();

	static final int BUFFER_SIZE = 10;

	CircularBuffer bufferOne = new CircularBuffer(BUFFER_SIZE);
	CircularBuffer bufferTwo = new CircularBuffer(BUFFER_SIZE);	

	// Method to associate a task with read and write buffers
    public void addBuffersForTask(Thread task) {
		if (readBufferMap.isEmpty() && writeBufferMap.isEmpty()) {
			readBufferMap.put(task.getName(), bufferOne);
			writeBufferMap.put(task.getName(), bufferTwo);
		} else {
			readBufferMap.put(task.getName(), bufferTwo);
			writeBufferMap.put(task.getName(), bufferOne);
		}
    }

    private CircularBuffer getCorrectWriteBuffer() {
        return writeBufferMap.get(Thread.currentThread().getName());  // Write buffer associated with this task
    }

    private CircularBuffer getCorrectReadBuffer() {
        return readBufferMap.get(Thread.currentThread().getName());  // Read buffer associated with this task
    }
	
	@Override
	public int read(byte[] bytes, int offset, int length) {

		CircularBuffer readBuff = getCorrectReadBuffer();

		int bytesRead = 0;
		while (bytesRead == 0) {
			try {
				for (int i = offset; i < offset + length; i++) {
					if (!readBuff.empty()) {
						bytes[i] = readBuff.pull();
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

		CircularBuffer writeBuff = getCorrectWriteBuffer();

		int bytesWritten = 0;
		while (bytesWritten == 0) {
			
				try {
					for (int i = offset; i < offset + length; i++) {
						if (!writeBuff.full()) {
							writeBuff.push(bytes[i]);
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
