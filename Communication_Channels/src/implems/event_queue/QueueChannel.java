package implems.event_queue;

import abstracts.ChannelAbstract;
import abstracts.event_queue.QueueChannelAbstract;

public class QueueChannel extends QueueChannelAbstract {

	ChannelAbstract connectedChannel;
	Listener channelListener;
	
	public QueueChannel(ChannelAbstract connectedChannel) {
		this.connectedChannel = connectedChannel;
	}
	
	@Override
	public void setListener(Listener l) {
		// TODO Auto-generated method stub
		this.channelListener = l;
	}

}
