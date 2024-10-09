package implems.event_queue;

import abstracts.ChannelAbstract;
import abstracts.event_queue.QueueChannelAbstract.Listener;

public class ReaderTask implements Runnable {

	ChannelAbstract connectedChannel;
	Listener channelListener;
	
	public ReaderTask(ChannelAbstract connectedChannel, Listener channelListener) {
		this.connectedChannel = connectedChannel;
		this.channelListener = channelListener;
		Thread t = new implems.thread_queue.Task(this);
		t.setDaemon(true);
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
                	Task.task().post(new Runnable() {	
            			@Override
            			public void run() {
            				channelListener.received(buffer);  
            			}
            		});
                }
            } catch (Exception e) {
            	Task.task().post(new Runnable() {	
        			@Override
        			public void run() {
        				channelListener.closed(); 
        			}
        		});
                break;
            }
        }
        if(channelListener != null) {
        	Task.task().post(new Runnable() {	
    			@Override
    			public void run() {
    				channelListener.closed();
    			}
    		});
        	
        }
	}

	

}
