package task2.implems.thread_queue;

import java.nio.ByteBuffer;

import task1.abstracts.ChannelAbstract;
import task2.abstracts.thread_queue.QueueChannelAbstract;

public class QueueChannel extends QueueChannelAbstract {

	ChannelAbstract connectedChannel;
	
	public QueueChannel(ChannelAbstract connectedChannel) {
		this.connectedChannel = connectedChannel;
	}
	
	public static int readMessageSize(ChannelAbstract channel) {
        byte[] sizeBytes = new byte[4];

        int bytesRead = 0;
        int response = 0;
        while (bytesRead < 4) {
            response = channel.read(sizeBytes, bytesRead, 4 - bytesRead);
            if (response == -1) {
                return -1;
            }
            bytesRead += response;
        }
        return ByteBuffer.wrap(sizeBytes).getInt();
    }

	@Override
	public void send(byte[] bytes, int offset, int length) {
		byte[] sizeBytes = ByteBuffer.allocate(Integer.BYTES).putInt(length).array();
        byte[] buffer = new byte[sizeBytes.length + length];
        System.arraycopy(sizeBytes, 0, buffer, 0, sizeBytes.length);
        System.arraycopy(bytes, 0, buffer, sizeBytes.length, length);
		
		int sentBytes = 0;
		while (sentBytes != buffer.length) {
			sentBytes += connectedChannel.write(buffer, sentBytes, buffer.length - sentBytes);
		}
	}

	@Override
	public byte[] receive() {
		int messageSize = readMessageSize(connectedChannel);
		
        if (messageSize <= 0) {
            return null;
        }

        byte[] buffer = new byte[messageSize];
        int bytesRead = 0;

        while (bytesRead < messageSize) {
            int response = connectedChannel.read(buffer, bytesRead, messageSize - bytesRead);

            bytesRead += response;
        }
        return buffer;
	}

	@Override
	public void close() {
		connectedChannel.disconnect();
	}

	@Override
	public boolean closed() {
		return connectedChannel.disconnected();
	}
	
}