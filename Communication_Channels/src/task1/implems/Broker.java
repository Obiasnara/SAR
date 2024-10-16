package task1.implems;

import task1.abstracts.BrokerAbstract;
import task1.abstracts.ChannelAbstract;
import task3.implems.event_queue.errors.ConnectionRefused;

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
	
	public final ConcurrentHashMap<Integer, RDV> requestList = new ConcurrentHashMap<Integer, RDV>();


	protected class RDV {
		private CircularBuffer buffIn;
		private CircularBuffer buffOut;
		private AtomicBoolean disconnected = new AtomicBoolean(false);
		private boolean acceptLatch = false;
		private boolean connectLatch = false;
		
		private RDV() {
			this.buffIn = new CircularBuffer(BUFFER_SIZE);
			this.buffOut = new CircularBuffer(BUFFER_SIZE);
		}

		protected ChannelAbstract accept() throws InterruptedException, ConnectionRefused {
			if(acceptLatch) throw new ConnectionRefused();
			acceptLatch = true;
			synchronized (this) {
				if(!connectLatch) { wait(); } else notify();
			}
			return new Channel(buffOut, buffIn, disconnected);
		}

		protected ChannelAbstract connect() throws InterruptedException, ConnectionRefused{
			if (connectLatch) throw new ConnectionRefused();
			connectLatch = true;
			synchronized (this) {				
				if(!acceptLatch) { wait(); } else notify();
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
	public ChannelAbstract accept(int port) throws ConnectionRefused, InterruptedException {
		RDV rdv;
		synchronized (requestList) {
			rdv = requestList.get(port);
			
			if(rdv == null) {
				rdv = new RDV();
				requestList.put(port, rdv);     
			}	
		}
		return rdv.accept(); 
	}


	@Override
	public ChannelAbstract connect(String name, int port) throws ConnectionRefused, InterruptedException {
		RDV rdv;
		if (this.name.equals(name)) {
			synchronized (requestList) {
				rdv = requestList.get(port);
				
				if(rdv == null) {
					rdv = new RDV();
					requestList.put(port, rdv);     
				}	
			}
			return rdv.connect();  // Return the channel after connect
		}

		// External broker logic (unchanged)
		BrokerAbstract brokerFoundOnNetwork = BrokerManager.getInstance().getBroker(name);
		if (brokerFoundOnNetwork == null) {
			throw new ConnectionRefused();
		}
		return brokerFoundOnNetwork.connect(name, port);
	}

}
