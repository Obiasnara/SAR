package implems.event_queue;

import abstracts.ChannelAbstract;
import abstracts.event_queue.QueueChannelAbstract.Listener;

public class ReaderTask implements Runnable {

	ChannelAbstract connectedChannel;
	Listener channelListener;
	
	public ReaderTask(ChannelAbstract connectedChannel, Listener channelListener) {
		this.connectedChannel = connectedChannel;
		this.channelListener = channelListener;
	}
	
	@Override
	public void run() {
		while (!connectedChannel.disconnected()) {
            try {
                // Blocking call to read from the channel
            	int messageSize = QueueChannel.readMessageSize(connectedChannel);
        		
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
	}

	

}
