package implems;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import abstracts.BrokerAbstract;
import abstracts.ChannelAbstract;
import abstracts.TaskAbstract;

public class Broker extends BrokerAbstract {

	private String name;

	// List of accept requests (final because it should not be redefined)
	private final Map<Integer, BlockingQueue<ChannelAbstract>> acceptRequests = new ConcurrentHashMap<Integer, BlockingQueue<ChannelAbstract>>();
	BlockingQueue<ChannelAbstract> requestList = new LinkedBlockingQueue<>();



	public Broker(String name) {
		this.name = name;
		BrokerManager.getInstance().addBroker(this.name, this);
	}

	@Override
	public ChannelAbstract accept(int port) {

		try {
			ChannelAbstract channel = requestList.take();
			((Channel)channel).addBuffersForTask(Thread.currentThread());
			return channel;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return null;
		}
		
	}

	@Override
	public ChannelAbstract connect(String name, int port) {
		if (this.name.equals(name)) {
			ChannelAbstract channel = new Channel();
			
			// Link task to channel
			((Channel)channel).addBuffersForTask(Thread.currentThread());

			// Didn't know about this method seems to work for that scenario
			requestList.add(channel);

			acceptRequests.put(port, requestList); 

			return channel;
		}		
		// Here we have a local network (but could be a remote network, the same code would apply)
		BrokerAbstract brokerFoundOnNetwork = BrokerManager.getInstance().getBroker(name);
		
		if (brokerFoundOnNetwork == null) {
			return null;
		}
		return brokerFoundOnNetwork.connect(name, port);
	}
}
