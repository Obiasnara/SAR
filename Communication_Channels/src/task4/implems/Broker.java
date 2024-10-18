package task4.implems;

import task4.abstracts.BrokerAbstract;
import task4.abstracts.ChannelAbstract;
import task4.abstracts.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import task3.abstracts.event_queue.QueueChannelAbstract;


public class Broker extends BrokerAbstract {

	public interface AcceptListener {
		void accepted(ChannelAbstract queue);
	}
	public interface ConnectListener {
		void refused();
		void connected(ChannelAbstract queue);
	}
	private String name;
	protected static final int BUFFER_SIZE = 10;
	
	protected HashMap<Integer, Request> acceptEvents = new HashMap<Integer, Request>();
	protected HashMap<Integer, ArrayList<Request>> connectEvents = new HashMap<Integer, ArrayList<Request>>();
	
	class Request implements Runnable {
		int port;
		String name;
		ConnectListener cl;
		AcceptListener al;
		boolean isAccept;
		
		protected static final int MAX_RETRIES = 10;
		int connectionTries;

		public Request(int port, AcceptListener l) {
			this.port = port; this.al = l;
			Task.task().post(this);
		}
		public Request(int port, String name, ConnectListener l) {
			this.port = port; this.name = name; this.cl = l; this.connectionTries = 0;
		}
		
		 @Override
	    public void run() {
			ArrayList<Request> connect_request = connectEvents.get(this.port);
			if(connect_request == null || connect_request.size() == 0) {
				Task.task().post(this);
				return;
			}
			
			// Someone is asking for a connection so we handle him
			CircularBuffer cb_in = new CircularBuffer(BUFFER_SIZE);
			CircularBuffer cb_out = new CircularBuffer(BUFFER_SIZE);
			
			AtomicBoolean disconnect_monitoring = new AtomicBoolean(false);
			
			Channel connect_channel = new Channel(cb_in, cb_out, disconnect_monitoring);
			
			ConnectListener connect_listener = connect_request.remove(0).cl;
			
			connect_listener.connected(connect_channel);
			
			// Then we handle ourselves
			cb_in = new CircularBuffer(BUFFER_SIZE);
			cb_out = new CircularBuffer(BUFFER_SIZE);
			
			disconnect_monitoring = new AtomicBoolean(false);
			
			Channel accept_channel = new Channel(cb_in, cb_out, disconnect_monitoring);
			
			this.al.accepted(accept_channel);
		}
	}
	
	public Broker(String name) {
		this.name = name;
		// Might want to force user to do that by his own not to leak 'this' reference
		BrokerManager.getInstance().addBroker(this.name, this);
	}

	@Override
	public boolean accept(int port, AcceptListener acl) {
		// Already accepting on the port
		if(this.acceptEvents.containsKey(port)) return false;
		
		acceptEvents.put(port, new Request(port, acl));
		return true;
	}


	@Override
	public boolean connect(int port, String name, ConnectListener cnl) {
		if(this.name != name) {
			BrokerAbstract b = BrokerManager.getInstance().getBroker(name);
			// Signal to the user the action cannot be performed 
			if (b == null) return false;  
			b.connect(port, name, cnl);
			return true;
		}
		
		ArrayList<Request> listOfRequests = this.connectEvents.get(port);
		if(listOfRequests == null) {
			listOfRequests = new ArrayList<Request>();
		}
		listOfRequests.add(new Request(port, name, cnl));
		this.connectEvents.put(port, listOfRequests);
		
		return true;
	}

}
