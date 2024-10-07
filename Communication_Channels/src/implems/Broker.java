package implems;

import abstracts.BrokerAbstract;
import abstracts.ChannelAbstract;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Broker extends BrokerAbstract {

	private String name;
	protected static final int BUFFER_SIZE = 10;
	
	public final ConcurrentHashMap<Integer, LinkedList<RDV>> requestList = new ConcurrentHashMap<Integer, LinkedList<RDV>>();


	protected class RDV {
		private CircularBuffer buffIn;
		private CircularBuffer buffOut;
		private AtomicBoolean disconnected = new AtomicBoolean(false);
		private CountDownLatch latch = new CountDownLatch(2);
		private static final long TIMEOUT = 5; // Timeout in seconds
		
		private RDV() {
			this.buffIn = new CircularBuffer(BUFFER_SIZE);
			this.buffOut = new CircularBuffer(BUFFER_SIZE);
		}

		protected ChannelAbstract accept() throws InterruptedException {
			latch.countDown();

			try {
				latch.await(TIMEOUT, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}

			return new Channel(buffOut, buffIn, disconnected);
		}

		protected ChannelAbstract connect() throws InterruptedException {
			latch.countDown();

			try {
				latch.await(TIMEOUT, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}

			return new Channel(buffIn, buffOut, disconnected);
		}
	}


	public Broker(String name) {
		this.name = name;
		// Might want to force user to do that by his own not to leak 'this' reference
		BrokerManager.getInstance().addBroker(this.name, this);
	}

	@Override
	public ChannelAbstract accept(int port) throws InterruptedException {
		RDV rdv;
		synchronized (requestList) {
			requestList.computeIfAbsent(port, k -> new LinkedList<>());
			LinkedList<RDV> queue = requestList.get(port);
			
			rdv = queue.peek();
			if(rdv == null) {
				rdv = new RDV();
				queue.add(rdv);     
			}
		}
		return rdv.accept(); 
	}


	@Override
	public ChannelAbstract connect(String name, int port) throws InterruptedException {
		if (this.name.equals(name)) {
			RDV rdv;
			synchronized (requestList) {
				requestList.computeIfAbsent(port, k -> new LinkedList<>());
				LinkedList<RDV> queue = requestList.get(port);
				
				rdv = queue.peek();
				if(rdv == null) {
					rdv = new RDV();
					queue.add(rdv);     
				}
			}

			return rdv.connect();  // Return the channel after connect
		}

		// External broker logic (unchanged)
		BrokerAbstract brokerFoundOnNetwork = BrokerManager.getInstance().getBroker(name);
		if (brokerFoundOnNetwork == null) {
			return null;
		}
		return brokerFoundOnNetwork.connect(name, port);
	}

}
