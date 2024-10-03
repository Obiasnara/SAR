package implems.event_queue;

import abstracts.ChannelAbstract;
import abstracts.event_queue.QueueBrokerAbstract;
import abstracts.event_queue.QueueChannelAbstract;
import implems.Broker;

public class QueueBroker extends QueueBrokerAbstract {

	String name;
	Broker br;
	
	public QueueBroker(String name) {
		this.br = new Broker(name);
		this.name = name;
	}
	
	@Override
	public boolean bind(int port, AcceptListener listener) {
		ChannelAbstract chan;
		try {
			chan = br.accept(port);
		} catch (InterruptedException e) {
			System.out.println("TimedOut");
			return false;
		}
		QueueChannelAbstract queueChannel = new QueueChannel(chan);
		listener.accepted(queueChannel);
		return true; // TODO: True for now, will change this later
	}

	@Override
	public boolean unbind(int port) {
		return false;
	}

	@Override
	public boolean connect(String name, int port, ConnectListener listener) {
		ChannelAbstract chan;
		try {
			chan = br.connect(name, port);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			listener.refused();
			return false;
		}
		QueueChannelAbstract queueChannel = new QueueChannel(chan);
		listener.connected(queueChannel);
		return true;
	}

}
