package implems.event_queue;

import abstracts.ChannelAbstract;
import abstracts.event_queue.QueueBrokerAbstract;
import abstracts.event_queue.QueueChannelAbstract;
import implems.Broker;
import implems.Channel;

public class QueueBroker extends QueueBrokerAbstract {

	String name;
	Broker br;
	
	public QueueBroker(String name) {
		this.br = new Broker(name);
		this.name = name;
	}
	
	protected class ConnectHandler implements Runnable {

		private int port;
		private String name;
	    private ChannelAbstract ch; // Replace SomeClass with the actual type of 'br'
	    private Broker br;
	    
	    public ConnectHandler(int port, String name, Broker br, ChannelAbstract ch) {
	        this.port = port;
	        this.br = br;
	        this.name = name;
	    }

	    @Override
	    public void run() {
	        try {
				this.ch = br.connect(name, port);
			} catch (InterruptedException e) {
				System.out.println("Timed out");
			} 
	    }
	    
	    public ChannelAbstract getChannel() {
	    	return this.ch;
	    }
		
	}
	
	@Override
	public boolean bind(int port, AcceptListener listener) {
		Thread th = new Thread(() -> {
			try {
				ChannelAbstract ch = br.accept(port);
				QueueChannelAbstract queueChannel = new QueueChannel(ch);
				listener.accepted(queueChannel);
			} catch (InterruptedException e) {
				System.out.println("Timed out");
			} 
        });
		th.start();
		return true;
	}

	@Override
	public boolean unbind(int port) {
		return false;
	}

	@Override
	public boolean connect(String name, int port, ConnectListener listener) {
		Thread th = new Thread(() -> {
			try {
				ChannelAbstract ch = br.connect(name, port);
				QueueChannelAbstract queueChannel = new QueueChannel(ch);
				listener.connected(queueChannel);
			} catch(InterruptedException e) {
				System.out.println("Timed out");
			}
		});
		th.start();
		return true;
	}

}
