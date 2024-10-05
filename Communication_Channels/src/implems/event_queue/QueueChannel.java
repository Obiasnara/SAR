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
		EventPump.getInstance().post(new ReaderTask(connectedChannel, l));
	}

	@Override
	public boolean send(Message msg) {
		
		// Sub optimal array handling TODO : Make it depend on msg
		byte[] sizeBytes = ByteBuffer.allocate(Integer.BYTES).putInt(msg.length).array();
        byte[] buffer = new byte[sizeBytes.length + msg.length];
        System.arraycopy(sizeBytes, 0, buffer, 0, sizeBytes.length);
        System.arraycopy(msg.bytes, 0, buffer, sizeBytes.length, msg.length);
		
        // We'll write what we can for now
		msg.offset += connectedChannel.write(buffer, msg.offset, buffer.length - msg.offset);
		
		if(msg.offset != buffer.length) return false; // Next event will write more
		
		channelListener.sent(msg);
		return true;
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
