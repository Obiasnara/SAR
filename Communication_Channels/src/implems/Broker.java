package implems;

import abstracts.BrokerAbstract;
import abstracts.ChannelAbstract;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class Broker extends BrokerAbstract {

	private String name;
	protected static final int BUFFER_SIZE = 10;
	
	private final ConcurrentHashMap<Integer, LinkedBlockingQueue<RDV>> requestList = new ConcurrentHashMap<Integer, LinkedBlockingQueue<RDV>>();


	protected class RDV {
		private CircularBuffer buffIn;
		private CircularBuffer buffOut;

		private CountDownLatch latch = new CountDownLatch(2);

		private RDV() {
			this.buffIn = new CircularBuffer(BUFFER_SIZE);
			this.buffOut = new CircularBuffer(BUFFER_SIZE);
		}

		protected ChannelAbstract accept() {
			latch.countDown();

			try {
				latch.await();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}

			return new Channel(buffOut, buffIn);
		}

		protected ChannelAbstract connect() {
			latch.countDown();

			try {
				latch.await();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}

			return new Channel(buffIn, buffOut);
		}
	}


	public Broker(String name) {
		this.name = name;
		// Might want to force user to do that by his own not to leak 'this' reference
		BrokerManager.getInstance().addBroker(this.name, this);
	}

	@Override
	public ChannelAbstract accept(int port) {
		
			LinkedBlockingQueue<RDV> queue = requestList.get(port);
			if (queue == null) {
				queue = requestList.computeIfAbsent(port, k -> new LinkedBlockingQueue<RDV>());
			}
			RDV rdv = queue.poll();
			if (rdv == null) {
				rdv = new RDV();
				queue.add(rdv);
			}

			return rdv.accept();
	}


	@Override
	public ChannelAbstract connect(String name, int port) {
		if (this.name.equals(name)) {
			LinkedBlockingQueue<RDV> queue = requestList.get(port);
			if (queue == null) {
				queue = requestList.computeIfAbsent(port, k -> new LinkedBlockingQueue<RDV>());
			}
			RDV rdv = queue.poll();
			if (rdv == null) {
				rdv = new RDV();
				queue.add(rdv);
			}

			return rdv.connect();
		}

		BrokerAbstract brokerFoundOnNetwork = BrokerManager.getInstance().getBroker(name);
		if (brokerFoundOnNetwork == null) {
			return null;
		}
		return brokerFoundOnNetwork.connect(name, port);
	}

}
