package task2.implems.thread_queue;

import task1.abstracts.ChannelAbstract;
import task1.implems.Broker;
import task2.abstracts.thread_queue.QueueBrokerAbstract;
import task2.abstracts.thread_queue.QueueChannelAbstract;
import task3.implems.event_queue.errors.ConnectionRefused;

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
		} catch (ConnectionRefused e) {
			
			System.err.println("The accept was refused by the Broker");
		}
    	return new QueueChannel(chan);
    }

    public QueueChannelAbstract connect(String name, int port) {
    	ChannelAbstract chan = null;
		try {
			chan = br.connect(name, port);
		} catch (InterruptedException e) {
			System.err.println("This behaviour emerged with event_queue, a Task has 5s to connect or accept");
		} catch (ConnectionRefused e) {
			// TODO Auto-generated catch block
			System.err.println("The connect was refused by the Broker");
		}
    	return new QueueChannel(chan);
    }
    
    public String name() {
    	return name;
    }
    
}
