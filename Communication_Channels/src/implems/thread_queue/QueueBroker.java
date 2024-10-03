package implems.thread_queue;

import abstracts.ChannelAbstract;
import abstracts.thread_queue.QueueBrokerAbstract;
import abstracts.thread_queue.QueueChannelAbstract;
import implems.Broker;

public class QueueBroker extends QueueBrokerAbstract {
    
	String name;
	Broker br;
	
	public QueueBroker(String name) {
		this.br = new Broker(name);
		this.name = name;
	}
	
	public QueueChannelAbstract accept(int port) {
    	ChannelAbstract chan = null;
		try {
			chan = br.accept(port);
		} catch (InterruptedException e) {
			System.err.println("This behaviour emerged with event_queue, a Task has 5s to connect or accept");
		}
    	return new QueueChannel(chan);
    }

    public QueueChannelAbstract connect(String name, int port) {
    	ChannelAbstract chan = null;
		try {
			chan = br.connect(name, port);
		} catch (InterruptedException e) {
			System.err.println("This behaviour emerged with event_queue, a Task has 5s to connect or accept");
		}
    	return new QueueChannel(chan);
    }
    
    public String name() {
    	return name;
    }
    
}
