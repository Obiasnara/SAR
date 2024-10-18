package task4.implems;

import task4.abstracts.AcceptListenerAbstract;
import task4.abstracts.BrokerAbstract;
import task4.abstracts.ConnectListenerAbstract;
import task4.abstracts.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Broker extends BrokerAbstract {

	private String name;
	protected static final int BUFFER_SIZE = 10;
	
	protected HashMap<Integer, Request> acceptEvents = new HashMap<Integer, Request>();
	protected HashMap<Integer, ArrayList<Request>> connectEvents = new HashMap<Integer, ArrayList<Request>>();
	
	class Request {
		int port;
		String name;
		Listener l;
		
		public Request(int port, Listener l) {
			this.port = port; this.l = l;
			this.processRequest();
		}
		public Request(int port, String name, Listener l) {
			this.port = port; this.name = name; this.l = l;
			this.processRequest();
		}
		
		public void processRequest() {
			// TODO add a runnable here
		}
	}
	
	public Broker(String name) {
		this.name = name;
		// Might want to force user to do that by his own not to leak 'this' reference
		BrokerManager.getInstance().addBroker(this.name, this);
	}

	@Override
	public boolean accept(int port, AcceptListenerAbstract acl) {
		// Already accepting on the port
		if(this.acceptEvents.containsKey(port)) return false;
		
		acceptEvents.put(port, new Request(port, acl));
		return true;
	}


	@Override
	public boolean connect(String name, int port, ConnectListenerAbstract cnl) {
		if(this.name != name) {
			BrokerAbstract b = BrokerManager.getInstance().getBroker(name);
			// Signal to the user the action cannot be performed 
			if (b == null) return false;  
			b.connect(name, port, cnl);
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
