package implems.event_queue;

import java.nio.ByteBuffer;

import abstracts.ChannelAbstract;
import abstracts.event_queue.QueueChannelAbstract;

public class QueueChannel extends QueueChannelAbstract {

	boolean isClosed = false;
	ChannelAbstract connectedChannel;
	Listener channelListener;
	
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
	
	public QueueChannel(ChannelAbstract connectedChannel) {
		this.connectedChannel = connectedChannel;
	}
	
	@Override
	public void setListener(Listener l) {
		// TODO Auto-generated method stub
		this.channelListener = l;
		this.startReading();
	}

	@Override
	public boolean send(Message msg) {
		byte[] sizeBytes = ByteBuffer.allocate(Integer.BYTES).putInt(msg.length).array();
        byte[] buffer = new byte[sizeBytes.length + msg.length];
        System.arraycopy(sizeBytes, 0, buffer, 0, sizeBytes.length);
        System.arraycopy(msg.bytes, 0, buffer, sizeBytes.length, msg.length);
		
		int sentBytes = 0;
		while (sentBytes != buffer.length) {
			sentBytes += connectedChannel.write(buffer, sentBytes, buffer.length - sentBytes);
		}
		channelListener.sent(msg);
		
		return true; // TODO : Change that to real write feedback
	}
	
	// Continuously read from the channel in a separate task
    public void startReading() {
        EventPump.getInstance().post(() -> {
            while (!connectedChannel.disconnected()) {
                try {
                    // Blocking call to read from the channel
                	int messageSize = readMessageSize(connectedChannel);
            		
                    if (messageSize <= 0) {
                        continue;
                    }

                    byte[] buffer = new byte[messageSize];
                    int bytesRead = 0;

                    while (bytesRead < messageSize) {
                        int response = connectedChannel.read(buffer, bytesRead, messageSize - bytesRead);

                        bytesRead += response;
                    }
                    
                    if (channelListener != null) {
                        channelListener.received(buffer);  // Notify listener when message is received
                    }
                } catch (Exception e) {
                    // Handle interruptions or errors
                    channelListener.closed();
                    break;
                }
            }
            if(channelListener != null) {
            	channelListener.closed();
            }
        });
    }

	@Override
	public void close() {
		connectedChannel.disconnect();
		channelListener.closed();
		isClosed = true;
		channelListener = null;
	}

	@Override
	public boolean closed() {
		return isClosed;
	}

	

}
