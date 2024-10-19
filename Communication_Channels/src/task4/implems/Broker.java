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
		void disconnected();
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
		
		CircularBuffer cb_in;
		CircularBuffer cb_out;
		
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
				Task.task().post(this, "Accepting Process on port " + this.port);
				return;
			}
			
			
			// Someone is asking for a connection so we handle him
			CircularBuffer connect_cb_in = new CircularBuffer(BUFFER_SIZE);
			CircularBuffer connect_cb_out = new CircularBuffer(BUFFER_SIZE);
			
			AtomicBoolean disconnect_monitoring = new AtomicBoolean(false);
			
			Channel connect_channel = new Channel(connect_cb_in, connect_cb_out, disconnect_monitoring);
			
			ConnectListener connect_listener = connect_request.remove(0).cl;
			
			Task.task().post(() -> connect_listener.connected(connect_channel), "Connected Event");
			
			
			// Then we handle ourselves
			this.cb_in = new CircularBuffer(BUFFER_SIZE);
			this.cb_out = new CircularBuffer(BUFFER_SIZE);
			
			disconnect_monitoring = new AtomicBoolean(false);
			
			Channel accept_channel = new Channel(cb_in, cb_out, disconnect_monitoring);
			
			Task.task().post(() -> this.al.accepted(accept_channel), "Accepted Event");
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
			if (b == null) {
				Task.task().post(() -> cnl.refused(), "Refused Event");
				return false;  
			}
			
			return b.connect(port, name, cnl); 
		}
		
		ArrayList<Request> listOfRequests = this.connectEvents.get(port);
		if(listOfRequests == null) {
			listOfRequests = new ArrayList<Request>();
		}
		listOfRequests.add(new Request(port, name, cnl));
		this.connectEvents.put(port, listOfRequests);
		
		return true;
	}
	
	public boolean disconnect(int port, String name, ConnectListener cnl) {
		
		// Dispatch to the right broker
		if(this.name != name) {
			BrokerAbstract b = BrokerManager.getInstance().getBroker(name);
			// Signal to the user the action cannot be performed 
			if (b == null) {
				return false;  
			}
			
			return b.disconnect(port, name, cnl); 
		}
		
		// Check if we have an actual accept
		Request accept_request = acceptEvents.get(port);
		if (accept_request == null) return false;
		
		// Send disconnected event
		Task.task().post(() -> cnl.disconnected(), "Disconnect Event");
		// Restart the connection seek process on that specific port
		Task.task().post(accept_request, "Accepting Process on port " + accept_request.port);
		
		return true;
		
		
	}

}
