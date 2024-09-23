package implems;

import abstracts.BrokerAbstract;
import abstracts.ChannelAbstract;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Broker extends BrokerAbstract {

	private String name;
	protected static final int BUFFER_SIZE = 10;
	
	private final Map<Integer, BlockingQueue<RDV>> requestList = new ConcurrentHashMap<Integer, BlockingQueue<RDV>>();


	protected class RDV {
		private CircularBuffer buffIn;
		private CircularBuffer buffOut;
		private RDV(CircularBuffer buffIn, CircularBuffer buffOut) {
			this.buffIn = buffIn;
			this.buffOut = buffOut;
		}
	}


	public Broker(String name) {
		this.name = name;
		// Might want to force user to do that by his own not to leak 'this' reference
		BrokerManager.getInstance().addBroker(this.name, this);
	}

	@Override
	public ChannelAbstract accept(int port) {
			BlockingQueue<RDV> rdvQueue;
			synchronized (requestList) {  // Synchronize on the request list
				while (!requestList.containsKey(port)) {
					try {
						requestList.wait();  // Wait on requestList until an entry is available
					} catch (InterruptedException ex) {
					}
				}
				rdvQueue = requestList.get(port);  // Fetch the RDV queue for the port
			}
			
			try {
				RDV rdv = rdvQueue.take();  // This will block until an RDV is available
				return new Channel(rdv.buffOut, rdv.buffIn);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return null;
			}
	}


	@Override
	public ChannelAbstract connect(String name, int port) {
		if (this.name.equals(name)) {
			CircularBuffer buffIn = new CircularBuffer(BUFFER_SIZE);
			CircularBuffer buffOut = new CircularBuffer(BUFFER_SIZE);
			ChannelAbstract channel = new Channel(buffIn, buffOut);

			RDV rdv = new RDV(buffIn, buffOut);

			synchronized (requestList) {
				// I never used the computeIfAbsent method before, but it seems to be the right choice here
				requestList.computeIfAbsent(port, p -> new LinkedBlockingQueue<>()).add(rdv);
				requestList.notifyAll(); // Accept will wake up
			}
			return channel;
		}

		BrokerAbstract brokerFoundOnNetwork = BrokerManager.getInstance().getBroker(name);
		if (brokerFoundOnNetwork == null) {
			return null;
		}
		return brokerFoundOnNetwork.connect(name, port);
	}

}
