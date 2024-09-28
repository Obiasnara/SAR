package implems;

import abstracts.ChannelAbstract;
import abstracts.QueueBrokerAbstract;
import abstracts.QueueChannelAbstract;

public class QueueBroker extends QueueBrokerAbstract {
    
	String name;
	Broker br;
	
	public QueueBroker(String name) {
		this.br = new Broker(name);
		this.name = name;
	}
	
	public QueueChannelAbstract accept(int port) {
    	ChannelAbstract chan = br.accept(port);
    	return new QueueChannel(chan);
    }

    public QueueChannelAbstract connect(String name, int port) {
    	ChannelAbstract chan = br.connect(name, port);
    	return new QueueChannel(chan);
    }

}
